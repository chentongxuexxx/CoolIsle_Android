package com.huandao.app.util

import com.huandao.app.util.Constants.PRICE_ANCHORS
import kotlin.math.roundToInt

/**
 * 实物锚点换算引擎。
 *
 * 将金额换算为常见消费品的等价数量，提供中性信息锚点。
 * 所有换算结果以「——供参考」结尾。
 *
 * 锚点列表（PRD 定义）：
 * - 一杯奶茶 15 元
 * - 一顿工作餐 35 元
 * - 一张电影票 60 元
 * - 一本好书 50 元
 * - 一斤车厘子 80 元
 * - 一顿火锅 150 元
 * - 一张健身月卡 300 元
 */
object PriceConverter {

    /**
     * 将金额换算为实物锚点等价结果。
     *
     * @param price 金额（元），为 null 或 <= 0 时返回空列表
     * @return 换算结果字符串列表，每条以「——供参考」结尾
     */
    fun convert(price: Double?): List<String> {
        if (price == null || price <= 0.0) return emptyList()

        val results = mutableListOf<String>()

        // 按金额量级选择合适的锚点展示（最多 3 个）
        val suitable = selectSuitableAnchors(price)
        for (anchor in suitable) {
            val count = (price / anchor.price * 10.0).roundToInt() / 10.0
            if (count >= 0.1) {
                val displayCount = if (count == count.toLong().toDouble()) {
                    "%.0f".format(count)
                } else {
                    "%.1f".format(count)
                }
                results.add("≈ ${displayCount} ${anchor.name}——供参考")
            }
        }

        return results
    }

    /**
     * 根据金额量级智能选择最合适的锚点。
     * - 小额（< 80）：奶茶 + 工作餐
     * - 中额（80-300）：工作餐 + 电影票 + 好书
     * - 大额（> 300）：电影票 + 火锅 + 健身月卡
     */
    private fun selectSuitableAnchors(price: Double): List<Constants.PriceAnchor> {
        return when {
            price < 80 -> PRICE_ANCHORS.filter { it.price <= 80 }.take(2)
            price <= 300 -> PRICE_ANCHORS.filter { it.price in 35.0..150.0 }.take(3)
            else -> PRICE_ANCHORS.filter { it.price >= 60.0 }.take(3)
        }.ifEmpty { PRICE_ANCHORS.take(1) }
    }

    /**
     * 换算单个结果（用于卡片内简要展示）。
     * 自动选择最贴近的一个锚点。
     */
    fun convertSingle(price: Double?): String? {
        if (price == null || price <= 0.0) return null
        val anchor = selectBestAnchor(price)
        val count = (price / anchor.price * 10.0).roundToInt() / 10.0
        val displayCount = if (count == count.toLong().toDouble()) {
            "%.0f".format(count)
        } else {
            "%.1f".format(count)
        }
        return "≈ ${displayCount} ${anchor.name}——供参考"
    }

    /** 选择除法结果最接近整数的锚点（更有体感） */
    private fun selectBestAnchor(price: Double): Constants.PriceAnchor {
        return PRICE_ANCHORS.minByOrNull { anchor ->
            val ratio = price / anchor.price
            val fraction = kotlin.math.abs(ratio - ratio.roundToInt())
            fraction
        } ?: PRICE_ANCHORS.first()
    }
}
