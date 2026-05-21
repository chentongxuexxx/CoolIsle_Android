package com.huandao.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.huandao.app.data.db.entity.EmotionTagEntity
import kotlinx.coroutines.flow.Flow

/**
 * 情绪标签 DAO 接口。
 */
@Dao
interface EmotionTagDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tags: List<EmotionTagEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustom(tag: EmotionTagEntity)

    /** 获取全部标签（预设在前、自定义在后，按创建时间升序） */
    @Query("SELECT * FROM emotion_tags ORDER BY is_predefined DESC, created_at ASC")
    fun getAll(): Flow<List<EmotionTagEntity>>

    /** 获取系统预设标签 */
    @Query("SELECT * FROM emotion_tags WHERE is_predefined = 1")
    suspend fun getPredefined(): List<EmotionTagEntity>

    /** 获取用户自定义标签（按创建时间升序） */
    @Query("SELECT * FROM emotion_tags WHERE is_predefined = 0 ORDER BY created_at ASC")
    fun getCustomTags(): Flow<List<EmotionTagEntity>>

    /** 获取自定义标签总数 */
    @Query("SELECT COUNT(*) FROM emotion_tags WHERE is_predefined = 0")
    suspend fun getCustomTagCount(): Int

    /** 删除自定义标签 */
    @Delete
    suspend fun deleteCustom(tag: EmotionTagEntity)

    /** 按 ID 列表批量获取标签 */
    @Query("SELECT * FROM emotion_tags WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<EmotionTagEntity>
}
