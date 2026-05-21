package com.huandao.app.ui.pool

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huandao.app.data.db.entity.CoolPoolItemEntity
import com.huandao.app.data.repository.CoolPoolRepository
import com.huandao.app.domain.model.CoolPoolItem
import com.huandao.app.domain.model.CoolPoolItemWithTags
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 冷静池列表状态管理。
 *
 * 管理缓冲中条目列表的加载、删除和快捷决策。
 */
@HiltViewModel
class CoolPoolViewModel @Inject constructor(
    private val repository: CoolPoolRepository,
) : ViewModel() {

    /** 原始缓冲条目 Flow（来自 Room） */
    private val rawCoolingItems: StateFlow<List<CoolPoolItemWithTags>> =
        repository.getCoolingItems()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** UI 层领域模型列表 */
    val coolingItems: StateFlow<List<CoolPoolItem>> = rawCoolingItems
        .combine(MutableStateFlow(Unit)) { items, _ ->
            items.map { CoolPoolItem.fromEntityWithTags(it) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** 列表是否为空 */
    val isEmpty: StateFlow<Boolean> = coolingItems
        .combine(MutableStateFlow(Unit)) { items, _ -> items.isEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    /**
     * 删除条目。
     */
    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            repository.deleteItem(itemId)
        }
    }

    /**
     * 快捷决策：从列表直接左滑决策。
     *
     * @param itemId 条目 ID
     * @param decision "decided_buy" 或 "decided_pass"
     */
    fun decideItemQuickly(itemId: String, decision: String) {
        viewModelScope.launch {
            when (decision) {
                CoolPoolItemEntity.STATUS_DECIDED_BUY,
                CoolPoolItemEntity.STATUS_DECIDED_PASS -> {
                    repository.decideItem(itemId, decision)
                }
            }
        }
    }

    /**
     * 前台扫描到期条目（双保险机制 UQ-03）。
     * 在列表页 onResume 时调用。
     */
    fun scanExpiredItems() {
        viewModelScope.launch {
            val expired = repository.getExpiredCoolingItems()
            if (expired.isNotEmpty()) {
                repository.batchUpdateStatus(
                    expired.map { it.id },
                    CoolPoolItemEntity.STATUS_EXPIRED
                )
            }
        }
    }
}
