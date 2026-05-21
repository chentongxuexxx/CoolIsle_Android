package com.huandao.app.ui.pool

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.huandao.app.R
import com.huandao.app.data.db.entity.EmotionTagEntity
import com.huandao.app.databinding.BottomSheetAddItemBinding
import com.huandao.app.ui.components.EmotionTagChipView
import com.huandao.app.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 添加条目 BottomSheet 面板。
 *
 * 功能：
 * - 商品名输入（必填，最多 50 字）
 * - 金额输入（可选，数字键盘）
 * - 品类选择（ChipGroup）
 * - 情绪标签选择（水平滚动，最多 3 个，可选）
 * - 冷静期选择（ChipGroup，默认 72h）
 * - 实时换算展示
 * - 主按钮「先放一放」
 */
@AndroidEntryPoint
class AddItemBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAddItemBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddItemViewModel by viewModels()

    companion object {
        const val TAG = "AddItemBottomSheet"

        fun newInstance(): AddItemBottomSheet = AddItemBottomSheet()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = BottomSheetAddItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTitleInput()
        setupPriceInput()
        setupCategoryChips()
        setupCoolPeriodChips()
        setupSubmitButton()
        observeViewModel()
    }

    /**
     * 商品名输入：限制 50 字。
     */
    private fun setupTitleInput() {
        binding.etTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.title.value = s?.toString() ?: ""
            }
        })
    }

    /**
     * 金额输入：数字键盘，实时触发换算。
     */
    private fun setupPriceInput() {
        binding.etPrice.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.priceText.value = s?.toString() ?: ""
            }
        })
    }

    /**
     * 品类 ChipGroup：单选模式。
     */
    private fun setupCategoryChips() {
        val categories = viewModel.categories
        categories.forEach { cat ->
            val chip = Chip(requireContext()).apply {
                text = cat
                isCheckable = true
                isClickable = true
                setOnCheckedChangeListener { _, isChecked ->
                    viewModel.category.value = if (isChecked) cat else null
                }
            }
            binding.chipGroupCategory.addView(chip)
        }
    }

    /**
     * 冷静期 ChipGroup：默认选中 72h。
     */
    private fun setupCoolPeriodChips() {
        val options = viewModel.coolPeriodOptions
        options.forEachIndexed { index, option ->
            val chip = Chip(requireContext()).apply {
                text = option.label
                isCheckable = true
                isClickable = true
                isChecked = option.hours == Constants.DEFAULT_COOL_PERIOD_HOURS

                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        viewModel.coolPeriodHours.value = option.hours
                        // 取消同组其他选中
                        for (i in 0 until binding.chipGroupPeriod.childCount) {
                            if (i != index) {
                                (binding.chipGroupPeriod.getChildAt(i) as? Chip)?.isChecked = false
                            }
                        }
                    }
                }
            }
            binding.chipGroupPeriod.addView(chip)
        }
    }

    /**
     * 提交按钮「先放一放」。
     */
    private fun setupSubmitButton() {
        binding.btnSubmit.setOnClickListener {
            val success = viewModel.addItem()
            if (success) {
                Toast.makeText(requireContext(), "已放入冷静池", Toast.LENGTH_SHORT).show()
                viewModel.resetForm()
                dismiss()
            } else {
                Toast.makeText(requireContext(), R.string.add_title_required, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 观察 ViewModel 状态。
     */
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                // 标签列表 → 动态构建标签 Chip
                launch {
                    viewModel.allTags.collectLatest { tags ->
                        populateTagChips(tags)
                    }
                }

                // 换算结果展示
                launch {
                    viewModel.conversionResults.collectLatest { results ->
                        if (results.isEmpty()) {
                            binding.conversionGroup.isVisible = false
                        } else {
                            binding.conversionGroup.isVisible = true
                            binding.tvConversionResults.text = results.joinToString("\n")
                        }
                    }
                }

                // 提交按钮状态
                launch {
                    viewModel.canSubmit.collectLatest { canSubmit ->
                        binding.btnSubmit.isEnabled = canSubmit
                        binding.btnSubmit.alpha = if (canSubmit) 1.0f else 0.5f
                    }
                }
            }
        }
    }

    /**
     * 动态填充情绪标签 Chip。
     */
    private fun populateTagChips(tags: List<EmotionTagEntity>) {
        binding.chipGroupEmotion.removeAllViews()

        tags.forEach { tag ->
            val label = if (!tag.emoji.isNullOrBlank()) "${tag.emoji} ${tag.name}" else tag.name
            val chip = EmotionTagChipView.create(
                requireContext(),
                label,
                isSelected = viewModel.selectedTagIds.value.contains(tag.id),
            ) { isChecked ->
                viewModel.toggleTag(tag.id)
            }
            binding.chipGroupEmotion.addView(chip)
        }

        // 自定义标签入口 Chip（始终在最后）
        val customChip = Chip(requireContext()).apply {
            text = getString(R.string.add_custom_tag_hint)
            isCheckable = false
            isClickable = true
            setOnClickListener {
                showCustomTagDialog()
            }
            chipIcon = resources.getDrawable(android.R.drawable.ic_input_add, null)
            chipIconSize = 18f
        }
        binding.chipGroupEmotion.addView(customChip)
    }

    /**
     * 弹出自定义标签输入对话框。
     */
    private fun showCustomTagDialog() {
        val editText = com.google.android.material.textfield.TextInputEditText(requireContext()).apply {
            hint = getString(R.string.add_custom_tag_hint)
            setSingleLine()
            filters = arrayOf(android.text.InputFilter.LengthFilter(Constants.MAX_TAG_NAME_LENGTH))
        }

        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_custom_tag_hint)
            .setView(editText)
            .setPositiveButton(R.string.add_custom_tag_add) { _, _ ->
                val name = editText.text?.toString()?.trim() ?: ""
                if (name.length >= Constants.MIN_TAG_NAME_LENGTH) {
                    lifecycleScope.launch {
                        val newTag = viewModel.addCustomTag(name)
                        if (newTag != null) {
                            viewModel.toggleTag(newTag.id)
                        } else {
                            Toast.makeText(requireContext(), R.string.add_custom_tag_limit, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
