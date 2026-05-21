package com.huandao.app.domain.model

import com.huandao.app.data.db.entity.EmotionTagEntity

/**
 * UI 层使用的领域模型（非 Room 绑定）。
 * 用于列表适配器和 View 绑定的轻量数据类。
 *
 * @property id 条目 ID
 * @property title 商品名称
 * @property price 预估金额
 * @property category 品类
 * @property coolPeriodHours 冷静期小时数
 * @property createdAt 创建时间戳
 * @property expiresAt 到期时间戳
 * @property status 状态
 * @property decidedAt 决策时间戳
 * @property decisionReason 决策理由
 * @property archivedAt 归档时间戳
 * @property tags 关联标签列表
 */
data class CoolPoolItem(
    val id: String,
    val title: String,
    val price: Double? = null,
    val category: String? = null,
    val coolPeriodHours: Int = 72,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long,
    val status: String = "cooling",
    val decidedAt: Long? = null,
    val decisionReason: String? = null,
    val archivedAt: Long? = null,
    val tags: List<EmotionTagEntity> = emptyList(),
) {
    /**
     * 从 Room 聚合模型转换。
     */
    companion object {
        fun fromEntityWithTags(entity: CoolPoolItemWithTags): CoolPoolItem {
            return CoolPoolItem(
                id = entity.item.id,
                title = entity.item.title,
                price = entity.item.price,
                category = entity.item.category,
                coolPeriodHours = entity.item.coolPeriodHours,
                createdAt = entity.item.createdAt,
                expiresAt = entity.item.expiresAt,
                status = entity.item.status,
                decidedAt = entity.item.decidedAt,
                decisionReason = entity.item.decisionReason,
                archivedAt = entity.item.archivedAt,
                tags = entity.tags,
            )
        }
    }
}
