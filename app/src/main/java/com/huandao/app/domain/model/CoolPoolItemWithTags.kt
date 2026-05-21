package com.huandao.app.domain.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.huandao.app.data.db.entity.CoolPoolItemEntity
import com.huandao.app.data.db.entity.CoolPoolItemTagCrossRef
import com.huandao.app.data.db.entity.EmotionTagEntity

/**
 * 条目 + 关联标签的聚合查询结果模型。
 *
 * Room 自动通过 @Relation + @Junction 执行多对多关联查询。
 * - item: 嵌入的冷静池条目
 * - tags: 通过 cross_ref 表关联的情绪标签列表
 */
data class CoolPoolItemWithTags(
    @Embedded
    val item: CoolPoolItemEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = CoolPoolItemTagCrossRef::class,
            parentColumn = "item_id",
            entityColumn = "tag_id",
        )
    )
    val tags: List<EmotionTagEntity> ,
)
