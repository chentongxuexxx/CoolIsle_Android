package com.huandao.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 冷静池条目实体（Room 表名：cool_pool_items）。
 *
 * 字段映射：
 * - id: UUID v4 主键，创建时生成
 * - status: cooling / expired / decided_buy / decided_pass
 * - 所有时间戳为 Long 型毫秒级 Unix 时间戳
 */
@Entity(
    tableName = "cool_pool_items",
    indices = [
        Index(value = ["status"], name = "idx_items_status"),
        Index(value = ["expires_at"], name = "idx_items_expires"),
        Index(value = ["created_at"], name = "idx_items_created"),
    ]
)
data class CoolPoolItemEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "price")
    val price: Double? = null,

    @ColumnInfo(name = "category")
    val category: String? = null,

    @ColumnInfo(name = "cool_period_hours")
    val coolPeriodHours: Int = 72,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "expires_at")
    val expiresAt: Long,

    @ColumnInfo(name = "status")
    val status: String = STATUS_COOLING,

    @ColumnInfo(name = "decided_at")
    val decidedAt: Long? = null,

    @ColumnInfo(name = "decision_reason")
    val decisionReason: String? = null,

    @ColumnInfo(name = "source_url")
    val sourceUrl: String? = null,

    @ColumnInfo(name = "archived_at")
    val archivedAt: Long? = null,
) {
    companion object {
        const val STATUS_COOLING = "cooling"
        const val STATUS_EXPIRED = "expired"
        const val STATUS_DECIDED_BUY = "decided_buy"
        const val STATUS_DECIDED_PASS = "decided_pass"
    }
}
