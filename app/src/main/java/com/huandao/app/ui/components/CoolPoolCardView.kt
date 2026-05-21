package com.huandao.app.ui.components

import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import com.huandao.app.R
import com.huandao.app.data.db.entity.EmotionTagEntity
import com.huandao.app.databinding.ItemCoolPoolCardBinding
import com.huandao.app.domain.model.CoolPoolItem
import com.huandao.app.util.PriceConverter
import com.huandao.app.util.TimeFormatter

/**
 * 冷静池卡片绑定工具。
 *
 * 负责将 [CoolPoolItem] 数据绑定到 [ItemCoolPoolCardBinding] 视图。
 * 卡片布局规格（PRD §4.4）：
 * - 左侧 2dp 竖线：状态色标
 * - 商品名 16sp #2C2C2A
 * - 金额 18sp #E8913A 加粗
 * - 情绪标签 #FFF0E0 底 #E8913A 字
 * - 已缓冲时长 12sp #999 右下角
 * - 换算结果 14sp #999，「——供参考」结尾
 */
object CoolPoolCardView {

    fun bind(binding: ItemCoolPoolCardBinding, item: CoolPoolItem, onClick: (() -> Unit)? = null) {
        // 左侧色标竖线
        val statusColor = when (item.status) {
            "cooling" -> binding.root.context.getColor(R.color.cooling_orange)
            "expired" -> binding.root.context.getColor(R.color.expired_green)
            else -> binding.root.context.getColor(R.color.decided_gray)
        }
        binding.statusStripe.setBackgroundColor(statusColor)

        // 商品名
        binding.cardTitle.text = item.title

        // 金额（可选）
        if (item.price != null && item.price > 0) {
            binding.cardPrice.isVisible = true
            binding.cardPrice.text = formatPrice(item.price)
        } else {
            binding.cardPrice.isVisible = false
        }

        // 品类（可选）
        if (!item.category.isNullOrBlank()) {
            binding.cardCategory.isVisible = true
            binding.cardCategory.text = item.category
        } else {
            binding.cardCategory.isVisible = false
        }

        // 情绪标签
        bindTags(binding, item.tags)

        // 缓冲时长
        binding.cardDuration.text = TimeFormatter.formatDuration(item.createdAt)

        // 换算结果
        val conversion = PriceConverter.convertSingle(item.price)
        if (conversion != null) {
            binding.cardConversion.isVisible = true
            binding.cardConversion.text = conversion
        } else {
            binding.cardConversion.isVisible = false
        }

        // 点击事件
        binding.root.setOnClickListener { onClick?.invoke() }
    }

    /**
     * 渲染标签行：最多显示 3 个标签 Chip。
     */
    private fun bindTags(binding: ItemCoolPoolCardBinding, tags: List<EmotionTagEntity>) {
        val tagViews = listOf<TextView>(
            binding.cardTag1,
            binding.cardTag2,
            binding.cardTag3,
        )

        tags.take(3).forEachIndexed { index, tag ->
            tagViews[index].apply {
                isVisible = true
                text = buildTagText(tag)
            }
        }

        // 隐藏多余的标签位
        for (i in tags.size until 3) {
            tagViews[i].isVisible = false
        }
    }

    /** 格式化标签文字：emoji + 名称 */
    private fun buildTagText(tag: EmotionTagEntity): String {
        return if (!tag.emoji.isNullOrBlank()) {
            "${tag.emoji} ${tag.name}"
        } else {
            tag.name
        }
    }

    /** 格式化金额：整数不显示小数位 */
    private fun formatPrice(price: Double): String {
        return if (price == price.toLong().toDouble()) {
            "¥${price.toLong()}"
        } else {
            "¥${"%.2f".format(price)}"
        }
    }
}
