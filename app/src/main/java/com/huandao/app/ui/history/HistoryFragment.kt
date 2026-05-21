package com.huandao.app.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.huandao.app.R
import com.huandao.app.databinding.FragmentHistoryBinding
import com.huandao.app.databinding.ItemCoolPoolCardBinding
import com.huandao.app.domain.model.CoolPoolItem
import com.huandao.app.ui.components.CoolPoolCardView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 历史记录页（已决策条目列表，US-03 归档查看）。
 *
 * 支持按决策结果筛选：全部 / 已购买 / 已放下。
 */
@AndroidEntryPoint
class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistoryViewModel by viewModels()
    private lateinit var adapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFilterChips()
        setupRecyclerView()
        observeViewModel()
    }

    /**
     * 筛选 ChipGroup：「全部」「已购买」「已放下」
     */
    private fun setupFilterChips() {
        val filters = listOf(
            Triple(null, R.string.history_filter_all, true),
            Triple("decided_buy", R.string.history_filter_buy, false),
            Triple("decided_pass", R.string.history_filter_pass, false),
        )

        filters.forEach { (filterValue, labelRes, default) ->
            val chip = Chip(requireContext()).apply {
                text = getString(labelRes)
                isCheckable = true
                isChecked = default
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        viewModel.filterByDecision(filterValue)
                        for (i in 0 until binding.chipGroupFilter.childCount) {
                            val other = binding.chipGroupFilter.getChildAt(i) as? Chip
                            if (other != this && other != null) {
                                other.isChecked = false
                            }
                        }
                    }
                }
            }
            binding.chipGroupFilter.addView(chip)
        }
    }

    private fun setupRecyclerView() {
        adapter = HistoryAdapter()
        binding.recyclerViewHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewHistory.adapter = adapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.decidedItems.collectLatest { items ->
                    adapter.submitList(items)
                    binding.emptyHistory.isVisible = items.isEmpty()
                    binding.recyclerViewHistory.isVisible = items.isNotEmpty()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/** 历史记录 ViewHolder */
private class HistoryViewHolder(val binding: ItemCoolPoolCardBinding) :
    RecyclerView.ViewHolder(binding.root)

/** 历史记录 Adapter */
private class HistoryAdapter : RecyclerView.Adapter<HistoryViewHolder>() {
    private var items: List<CoolPoolItem> = emptyList()

    fun submitList(newItems: List<CoolPoolItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemCoolPoolCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        CoolPoolCardView.bind(holder.binding, items[position])
    }

    override fun getItemCount(): Int = items.size
}
