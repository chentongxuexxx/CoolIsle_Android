package com.huandao.app.ui.pool

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.huandao.app.R
import com.huandao.app.data.db.entity.CoolPoolItemEntity
import com.huandao.app.databinding.FragmentCoolPoolBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 冷静池列表页（首页 Tab 1）。
 *
 * 功能：
 * - RecyclerView 卡片流展示缓冲中/已到期的条目
 * - FAB 打开添加面板
 * - ItemTouchHelper 左滑快捷决策（我决定买了 / 不再考虑）
 * - 点击已到期条目跳转决策引导页
 * - 顶部历史记录入口
 * - 空状态引导视图
 */
@AndroidEntryPoint
class CoolPoolFragment : Fragment() {

    private var _binding: FragmentCoolPoolBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CoolPoolViewModel by viewModels()
    private lateinit var adapter: CoolPoolAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCoolPoolBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeToDecide()
        setupFab()
        setupHeader()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        // 前台扫描到期条目（双保险 UQ-03）
        viewModel.scanExpiredItems()
    }

    private fun setupRecyclerView() {
        adapter = CoolPoolAdapter { item ->
            if (item.status == CoolPoolItemEntity.STATUS_EXPIRED) {
                // 点击已到期条目 → 决策引导页
                val bundle = Bundle().apply {
                    putString("itemId", item.id)
                }
                findNavController().navigate(R.id.nav_decision, bundle)
            }
        }

        binding.recyclerView.adapter = adapter
    }

    /**
     * 顶部栏：标题 + 历史记录入口。
     */
    private fun setupHeader() {
        binding.btnHistory.setOnClickListener {
            findNavController().navigate(R.id.action_cool_pool_to_history)
        }
    }

    /**
     * 左滑手势：ItemTouchHelper 实现两个等权按钮。
     * 左滑露出：「我决定买了」、「不再考虑」。
     * 两者权重相等，无评判色彩。
     */
    private fun setupSwipeToDecide() {
        val touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) return

                val item = adapter.currentList[position]

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(item.title)
                    .setMessage(R.string.decision_title)
                    .setPositiveButton(R.string.pool_swipe_buy) { _, _ ->
                        viewModel.decideItemQuickly(
                            item.id,
                            CoolPoolItemEntity.STATUS_DECIDED_BUY
                        )
                        Toast.makeText(requireContext(), R.string.pool_decided_toast, Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton(R.string.pool_swipe_pass) { _, _ ->
                        viewModel.decideItemQuickly(
                            item.id,
                            CoolPoolItemEntity.STATUS_DECIDED_PASS
                        )
                        Toast.makeText(requireContext(), R.string.pool_pass_toast, Toast.LENGTH_SHORT).show()
                    }
                    .setNeutralButton(R.string.cancel) { _, _ ->
                        adapter.notifyItemChanged(position)
                    }
                    .setOnCancelListener {
                        adapter.notifyItemChanged(position)
                    }
                    .show()
            }

            override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float = 0.3f
        })

        touchHelper.attachToRecyclerView(binding.recyclerView)
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            val bottomSheet = AddItemBottomSheet.newInstance()
            bottomSheet.show(parentFragmentManager, AddItemBottomSheet.TAG)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                launch {
                    viewModel.coolingItems.collectLatest { items ->
                        adapter.submitList(items)
                    }
                }

                launch {
                    viewModel.isEmpty.collectLatest { empty ->
                        binding.emptyState.isVisible = empty
                        binding.recyclerView.isVisible = !empty
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
