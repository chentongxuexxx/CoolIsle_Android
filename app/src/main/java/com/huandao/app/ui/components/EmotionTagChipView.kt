package com.huandao.app.ui.components

import android.content.Context
import android.view.View
import com.google.android.material.chip.Chip
import com.huandao.app.R

/**
 * 情绪标签 Chip 组件工具类。
 *
 * 负责配置 Material Chip 的选中/未选中双态样式：
 * - 未选中: #FFF0E0 底 + #E8913A 字
 * - 选中: #E8913A 底 + #FFFFFF 字
 */
object EmotionTagChipView {

    /**
     * 将指定 Chip 配置为情绪标签样式。
     *
     * @param chip Material Chip 实例
     * @param text 标签文字
     * @param onToggle 选中/取消选中回调
     */
    fun bind(
        chip: Chip,
        text: String,
        isSelected: Boolean = false,
        onToggle: ((Boolean) -> Unit)? = null,
    ) {
        chip.text = text
        chip.isChecked = isSelected
        chip.isCheckable = true

        // 应用双态样式
        applyChipStyle(chip)

        chip.setOnCheckedChangeListener { _, checked ->
            applyChipStyle(chip)
            onToggle?.invoke(checked)
        }

        // 初始样式
        applyChipStyle(chip)
    }

    /**
     * 根据选中状态动态切换 Chip 样式。
     */
    private fun applyChipStyle(chip: Chip) {
        if (chip.isChecked) {
            chip.chipBackgroundColor = android.content.res.ColorStateList.valueOf(
                chip.context.getColor(R.color.tag_selected_bg)
            )
            chip.setTextColor(chip.context.getColor(R.color.tag_selected_text))
        } else {
            chip.chipBackgroundColor = android.content.res.ColorStateList.valueOf(
                chip.context.getColor(R.color.tag_bg)
            )
            chip.setTextColor(chip.context.getColor(R.color.tag_text))
        }
    }

    /**
     * 创建并配置一个新的情绪标签 Chip。
     */
    fun create(
        context: Context,
        text: String,
        isSelected: Boolean = false,
        onToggle: ((Boolean) -> Unit)? = null,
    ): Chip {
        return Chip(context).also { chip ->
            bind(chip, text, isSelected, onToggle)
            chip.isCloseIconVisible = false
            chip.isClickable = true
            chip.isFocusable = true
        }
    }
}
