package com.huandao.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * 冷静池条目 ↔ 情绪标签的 N:M 关联表。
 *
 * 主键为 (item_id, tag_id) 复合主键。
 * 外键级联删除：条目删除时关联记录自动清除；标签删除时关联记录自动清除。
 */
@Entity(
    tableName = "cool_pool_item_tag_cross_ref",
    primaryKeys = ["item_id", "tag_id"],
    foreignKeys = [
        ForeignKey(
            entity = CoolPoolItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["item_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = EmotionTagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tag_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["tag_id"], name = "idx_cross_ref_tag"),
    ]
)
data class CoolPoolItemTagCrossRef(
    @ColumnInfo(name = "item_id")
    val itemId: String,

    @ColumnInfo(name = "tag_id")
    val tagId: String,
)
