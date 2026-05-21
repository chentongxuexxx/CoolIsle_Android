package com.huandao.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 情绪标签实体（Room 表名：emotion_tags）。
 *
 * - isPredefined = true 表示系统预设标签（8 个），不可删除
 * - isPredefined = false 表示用户自定义标签，上限 20 个
 */
@Entity(tableName = "emotion_tags")
data class EmotionTagEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "emoji")
    val emoji: String? = null,

    @ColumnInfo(name = "is_predefined")
    val isPredefined: Boolean = false,

    @ColumnInfo(name = "color_hex")
    val colorHex: String? = "#E8913A",

    @ColumnInfo(name = "created_at")
    val createdAt: Long? = null,
)
