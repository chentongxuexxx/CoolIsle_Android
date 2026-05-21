package com.huandao.app.data.repository

import com.huandao.app.data.db.dao.CoolPoolItemDao
import com.huandao.app.data.db.dao.EmotionTagDao
import com.huandao.app.data.db.entity.CoolPoolItemEntity
import com.huandao.app.data.db.entity.CoolPoolItemTagCrossRef
import com.huandao.app.data.model.CoolPeriod
import com.huandao.app.domain.model.CoolPoolItemWithTags
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 冷静池条目数据仓库。
 * 封装 CoolPoolItemDao 和 EmotionTagDao 的组合操作，管理状态转换逻辑。
 */
@Singleton
class CoolPoolRepository @Inject constructor(
    private val itemDao: CoolPoolItemDao,
    private val tagDao: EmotionTagDao,
) {

    /** 获取所有缓冲中/已到期的条目（Flow 实时更新） */
    fun getCoolingItems(): Flow<List<CoolPoolItemWithTags>> = itemDao.getAllCooling()

    /** 获取所有已决策且未归档的条目 */
    fun getDecidedItems(): Flow<List<CoolPoolItemWithTags>> = itemDao.getAllDecided()

    /** 按 ID 获取单条条目 */
    suspend fun getById(id: String): CoolPoolItemWithTags? = itemDao.getById(id)

    /**
     * 添加新的冷静池条目。
     *
     * @param entity 条目实体（id 由调用方通过 UUID 生成）
     * @param tagIds 关联标签 ID 列表
     * @return 新条目的 ID
     */
    suspend fun addItem(entity: CoolPoolItemEntity, tagIds: List<String>): String {
        // 1. 插入条目
        itemDao.insert(entity)

        // 2. 插入 N:M 关联
        if (tagIds.isNotEmpty()) {
            val refs = tagIds.map { tagId ->
                CoolPoolItemTagCrossRef(itemId = entity.id, tagId = tagId)
            }
            itemDao.insertCrossRefs(refs)
        }

        return entity.id
    }

    /**
     * 决策操作：更新条目状态为 decided_buy 或 decided_pass。
     *
     * @param itemId 条目 ID
     * @param status 目标状态（decided_buy / decided_pass）
     * @param reason 决策理由（可选）
     */
    suspend fun decideItem(itemId: String, status: String, reason: String? = null) {
        val existing = itemDao.getById(itemId) ?: return
        val updated = existing.item.copy(
            status = status,
            decidedAt = System.currentTimeMillis(),
            decisionReason = reason,
        )
        itemDao.update(updated)
    }

    /**
     * 重置冷静期：将条目状态重置为 cooling，重新计算 expiresAt。
     *
     * @param itemId 条目 ID
     * @param newHours 新的冷静期小时数
     */
    suspend fun resetCooling(itemId: String, newHours: Int) {
        val existing = itemDao.getById(itemId) ?: return
        val now = System.currentTimeMillis()
        val updated = existing.item.copy(
            status = CoolPoolItemEntity.STATUS_COOLING,
            coolPeriodHours = newHours,
            expiresAt = now + newHours * 3600_000L,
            decidedAt = null,
            decisionReason = null,
        )
        itemDao.update(updated)
    }

    /**
     * 删除条目（级联删除关联的 cross_ref 记录由 Room ForeignKey CASCADE 自动处理）。
     */
    suspend fun deleteItem(itemId: String) {
        val existing = itemDao.getById(itemId) ?: return
        itemDao.delete(existing.item)
    }

    /** 获取所有已到期的缓冲中条目（供 Worker 使用） */
    suspend fun getExpiredCoolingItems(): List<CoolPoolItemEntity> {
        return itemDao.getExpiredCoolingItems(System.currentTimeMillis())
    }

    /** 批量更新条目状态（Worker 到期标记） */
    suspend fun batchUpdateStatus(ids: List<String>, status: String) {
        itemDao.batchUpdateStatus(ids, status)
    }

    /**
     * 归档已完成条目：将 decided_at 超过 7 天且未归档的条目标记 archived_at。
     */
    suspend fun archiveCompletedItems() {
        val cutoff = System.currentTimeMillis() - 7 * 24 * 3600_000L
        val archivable = itemDao.getArchivableItems(cutoff)
        val now = System.currentTimeMillis()
        archivable.forEach { item ->
            itemDao.archiveItem(item.id, now)
        }
    }

    /** 按状态统计数量 */
    suspend fun getCountByStatus(status: String): Int {
        return itemDao.getCountByStatus(status)
    }

    /**
     * 生成新的 UUID v4 条目 ID。
     */
    fun generateId(): String = UUID.randomUUID().toString()
}
