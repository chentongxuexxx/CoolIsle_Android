package com.huandao.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.huandao.app.data.db.entity.CoolPoolItemEntity
import com.huandao.app.data.db.entity.CoolPoolItemTagCrossRef
import com.huandao.app.domain.model.CoolPoolItemWithTags
import kotlinx.coroutines.flow.Flow

/**
 * 冷静池条目 DAO 接口。
 * 写入方法为 suspend，查询返回 Flow 的方法非 suspend。
 */
@Dao
interface CoolPoolItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CoolPoolItemEntity)

    @Update
    suspend fun update(item: CoolPoolItemEntity)

    @Delete
    suspend fun delete(item: CoolPoolItemEntity)

    // ── 关联表操作 ──

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRefs(refs: List<CoolPoolItemTagCrossRef>)

    @Query("DELETE FROM cool_pool_item_tag_cross_ref WHERE item_id = :itemId")
    suspend fun deleteCrossRefsByItemId(itemId: String)

    // ── 查询 ──

    /** 按 ID 获取单条条目（含关联标签） */
    @Transaction
    @Query("SELECT * FROM cool_pool_items WHERE id = :id")
    suspend fun getById(id: String): CoolPoolItemWithTags?

    /** 获取所有缓冲中/已到期的条目（按创建时间倒序） */
    @Transaction
    @Query("SELECT * FROM cool_pool_items WHERE status IN ('cooling', 'expired') ORDER BY created_at DESC")
    fun getAllCooling(): Flow<List<CoolPoolItemWithTags>>

    /** 获取所有已决策且未归档的条目（按决策时间倒序） */
    @Transaction
    @Query("SELECT * FROM cool_pool_items WHERE status IN ('decided_buy', 'decided_pass') AND archived_at IS NULL ORDER BY decided_at DESC")
    fun getAllDecided(): Flow<List<CoolPoolItemWithTags>>

    /** 获取已到期的缓冲中条目（供 Worker 使用） */
    @Query("SELECT * FROM cool_pool_items WHERE status = 'cooling' AND expires_at <= :now")
    suspend fun getExpiredCoolingItems(now: Long): List<CoolPoolItemEntity>

    /** 获取可归档条目：已决策超过 7 天 且 未归档 */
    @Query("SELECT * FROM cool_pool_items WHERE status IN ('decided_buy', 'decided_pass') AND decided_at IS NOT NULL AND decided_at <= :cutoff AND archived_at IS NULL")
    suspend fun getArchivableItems(cutoff: Long): List<CoolPoolItemEntity>

    /** 按状态统计数量 */
    @Query("SELECT COUNT(*) FROM cool_pool_items WHERE status = :status")
    suspend fun getCountByStatus(status: String): Int

    /** 归档条目（标记 archived_at） */
    @Query("UPDATE cool_pool_items SET archived_at = :archivedAt WHERE id = :id")
    suspend fun archiveItem(id: String, archivedAt: Long)

    /** 批量更新状态（Worker 用于到期标记） */
    @Query("UPDATE cool_pool_items SET status = :status WHERE id IN (:ids)")
    suspend fun batchUpdateStatus(ids: List<String>, status: String)
}
