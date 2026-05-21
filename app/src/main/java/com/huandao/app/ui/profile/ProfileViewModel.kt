package com.huandao.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huandao.app.data.db.entity.CoolPoolItemEntity
import com.huandao.app.data.repository.CoolPoolRepository
import com.huandao.app.data.repository.EmotionTagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

/**
 * 个人中心状态管理（P1 占位 + 数据导出）。
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val coolPoolRepository: CoolPoolRepository,
    private val tagRepository: EmotionTagRepository,
) : ViewModel() {

    private val _coolingCount = MutableStateFlow(0)
    val coolingCount: StateFlow<Int> = _coolingCount.asStateFlow()

    private val _decidedCount = MutableStateFlow(0)
    val decidedCount: StateFlow<Int> = _decidedCount.asStateFlow()

    init {
        loadCounts()
    }

    private fun loadCounts() {
        viewModelScope.launch {
            _coolingCount.value = coolPoolRepository.getCountByStatus(CoolPoolItemEntity.STATUS_COOLING)
            _decidedCount.value =
                coolPoolRepository.getCountByStatus(CoolPoolItemEntity.STATUS_DECIDED_BUY) +
                coolPoolRepository.getCountByStatus(CoolPoolItemEntity.STATUS_DECIDED_PASS)
        }
    }

    /**
     * 将所有冷静池数据导出为 JSON 字符串。
     * 包含 items、tags、cross_ref 的完整结构化数据。
     */
    suspend fun exportAllDataAsJson(): String {
        val root = JSONObject()
        root.put("app", "缓岛")
        root.put("version", "1.0.0")
        root.put("exported_at", System.currentTimeMillis())

        // 获取所有条目（缓冲中 + 已决策）
        val coolingItems = coolPoolRepository.getCoolingItems().first()
        val decidedItems = coolPoolRepository.getDecidedItems().first()
        val allTags = tagRepository.getAllTags().first()

        // 构建 items 数组
        val itemsArray = JSONArray()
        (coolingItems + decidedItems).forEach { itemWithTags ->
            val item = itemWithTags.item
            val obj = JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("price", item.price ?: JSONObject.NULL)
                put("category", item.category ?: JSONObject.NULL)
                put("cool_period_hours", item.coolPeriodHours)
                put("created_at", item.createdAt)
                put("expires_at", item.expiresAt)
                put("status", item.status)
                put("decided_at", item.decidedAt ?: JSONObject.NULL)
                put("decision_reason", item.decisionReason ?: JSONObject.NULL)
                put("archived_at", item.archivedAt ?: JSONObject.NULL)

                // 关联标签
                val tagsArray = JSONArray()
                itemWithTags.tags.forEach { tag ->
                    tagsArray.put(JSONObject().apply {
                        put("id", tag.id)
                        put("name", tag.name)
                        put("emoji", tag.emoji ?: JSONObject.NULL)
                        put("is_predefined", tag.isPredefined)
                    })
                }
                put("tags", tagsArray)
            }
            itemsArray.put(obj)
        }
        root.put("items", itemsArray)
        root.put("total_items", itemsArray.length())

        // 标签引用
        val tagsArray = JSONArray()
        allTags.forEach { tag ->
            tagsArray.put(JSONObject().apply {
                put("id", tag.id)
                put("name", tag.name)
                put("emoji", tag.emoji ?: JSONObject.NULL)
                put("is_predefined", tag.isPredefined)
            })
        }
        root.put("all_tags", tagsArray)

        return root.toString(2)
    }

    /** 刷新计数 */
    fun refresh() {
        loadCounts()
    }
}
