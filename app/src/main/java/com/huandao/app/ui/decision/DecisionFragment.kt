package com.huandao.app.ui.decision

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
import com.huandao.app.R
import com.huandao.app.data.db.entity.CoolPoolItemEntity
import com.huandao.app.databinding.FragmentDecisionBinding
import com.huandao.app.util.PriceConverter
import com.huandao.app.util.TimeFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 决策引导页（全屏，US-07）。
 *
 * 冷静期到期后展示条目详情 + 三个等权按钮。
 * 用户的最终决策权完全在自己手中，无评判色彩。
 */
@AndroidEntryPoint
class DecisionFragment : Fragment() {

    private var _binding: FragmentDecisionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DecisionViewModel by viewModels()

    /** 从导航参数中提取 itemId */
    private val itemId: String by lazy {
        arguments?.getString("itemId") ?: throw IllegalStateException("itemId argument required")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDecisionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupButtons()
        observeViewModel()

        // 加载条目详情
        viewModel.loadItem(itemId)
    }

    /**
     * 三个等权按钮：我想买 / 没那么想 / 再等等。
     */
    private fun setupButtons() {
        binding.btnDecideBuy.setOnClickListener {
            viewModel.decide(
                CoolPoolItemEntity.STATUS_DECIDED_BUY,
                binding.etReason.text?.toString()?.takeIf { it.isNotBlank() }
            )
            Toast.makeText(requireContext(), R.string.pool_decided_toast, Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }

        binding.btnDecidePass.setOnClickListener {
            viewModel.decide(
                CoolPoolItemEntity.STATUS_DECIDED_PASS,
                binding.etReason.text?.toString()?.takeIf { it.isNotBlank() }
            )
            Toast.makeText(requireContext(), R.string.pool_pass_toast, Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }

        binding.btnDecideWait.setOnClickListener {
            viewModel.resetCooling(72)
            Toast.makeText(requireContext(), R.string.decision_reset_toast, Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.item.collectLatest { itemWithTags ->
                    if (itemWithTags != null) {
                        binding.groupContent.isVisible = true
                        bindItem(itemWithTags)
                    } else {
                        binding.groupContent.isVisible = false
                    }
                }
            }
        }
    }

    /**
     * 绑定条目摘要信息到视图。
     */
    private fun bindItem(item: com.huandao.app.domain.model.CoolPoolItemWithTags) {
        val entity = item.item

        // 商品名
        binding.tvDecisionTitle.text = entity.title

        // 金额
        if (entity.price != null && entity.price > 0) {
            binding.tvDecisionPrice.isVisible = true
            val priceText = if (entity.price == entity.price.toLong().toDouble()) {
                "¥${entity.price.toLong()}"
            } else {
                "¥${"%.2f".format(entity.price)}"
            }
            binding.tvDecisionPrice.text = priceText
        } else {
            binding.tvDecisionPrice.isVisible = false
        }

        // 已缓冲时长
        binding.tvDecisionDuration.text = TimeFormatter.formatDuration(entity.createdAt)

        // 换算
        val conversion = PriceConverter.convertSingle(entity.price)
        if (conversion != null) {
            binding.tvDecisionConversion.isVisible = true
            binding.tvDecisionConversion.text = conversion
        } else {
            binding.tvDecisionConversion.isVisible = false
        }

        // 情绪标签
        if (item.tags.isNotEmpty()) {
            binding.tvDecisionTags.isVisible = true
            binding.tvDecisionTags.text = item.tags.joinToString(" · ") { tag ->
                if (!tag.emoji.isNullOrBlank()) "${tag.emoji}${tag.name}" else tag.name
            }
        } else {
            binding.tvDecisionTags.isVisible = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
