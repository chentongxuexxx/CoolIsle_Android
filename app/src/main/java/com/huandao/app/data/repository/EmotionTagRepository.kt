package com.huandao.app.data.repository

import com.huandao.app.data.db.dao.EmotionTagDao
import com.huandao.app.data.db.entity.EmotionTagEntity
import com.huandao.app.util.Constants
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 情绪标签数据仓库。
 * 管理预设标签和自定义标签的 CRUD，校验自定义标签上限。
 */
@Singleton
class EmotionTagRepository @Inject constructor(
    private val tagDao: EmotionTagDao,
) {

    /** 获取所有标签（Flow 实时更新） */
    fun getAllTags(): Flow<List<EmotionTagEntity>> = tagDao.getAll()

    /** 获取预设标签列表（同步） */
    suspend fun getPredefinedTags(): List<EmotionTagEntity> = tagDao.getPredefined()

    /** 获取自定义标签列表（Flow） */
    fun getCustomTags(): Flow<List<EmotionTagEntity>> = tagDao.getCustomTags()

    /**
     * 添加自定义标签。
     *
     * @param name 标签名称（2-6 字）
     * @param emoji 对应 emoji（可选）
     * @return 新标签实体，若超出上限则返回 null
     */
    suspend fun addCustomTag(name: String, emoji: String? = null): EmotionTagEntity? {
        if (!canAddCustomTag()) return null

        val tag = EmotionTagEntity(
            id = "tag_custom_${UUID.randomUUID().toString().take(8)}",
            name = name.trim(),
            emoji = emoji,
            isPredefined = false,
            colorHex = "#E8913A",
            createdAt = System.currentTimeMillis(),
        )
        tagDao.insertCustom(tag)
        return tag
    }

    /**
     * 检查是否还能添加自定义标签。
     * @return true 如果当前自定义标签数 < 20
     */
    suspend fun canAddCustomTag(): Boolean {
        return tagDao.getCustomTagCount() < Constants.MAX_CUSTOM_TAGS
    }

    /**
     * 删除自定义标签（预设标签不可删除）。
     * Room ForeignKey CASCADE 会自动清除关联的 cross_ref 记录。
     */
    suspend fun deleteCustomTag(id: String) {
        val tags = tagDao.getByIds(listOf(id))
        val tag = tags.firstOrNull() ?: return
        if (!tag.isPredefined) {
            tagDao.deleteCustom(tag)
        }
    }

    /** 按 ID 列表批量获取标签 */
    suspend fun getByIds(ids: List<String>): List<EmotionTagEntity> {
        return tagDao.getByIds(ids)
    }
}
