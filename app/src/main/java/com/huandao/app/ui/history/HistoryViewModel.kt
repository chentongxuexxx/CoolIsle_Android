package com.huandao.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huandao.app.data.repository.CoolPoolRepository
import com.huandao.app.domain.model.CoolPoolItem
import com.huandao.app.domain.model.CoolPoolItemWithTags
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * 历史记录状态管理。
 *
 * 管理已决策条目的列表展示和筛选。
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: CoolPoolRepository,
) : ViewModel() {

    private val filterDecision = MutableStateFlow<String?>(null)

    /** 原始已决策条目 Flow */
    private val rawDecidedItems: StateFlow<List<CoolPoolItemWithTags>> =
        repository.getDecidedItems()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** 筛选后的条目列表 */
    val decidedItems: StateFlow<List<CoolPoolItem>> = rawDecidedItems
        .combine(filterDecision) { items, filter ->
            val filtered = if (filter != null) {
                items.filter { it.item.status == filter }
            } else {
                items
            }
            filtered.map { CoolPoolItem.fromEntityWithTags(it) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * 按决策结果筛选。
     *
     * @param decision null 表示「全部」，否则为 "decided_buy" 或 "decided_pass"
     */
    fun filterByDecision(decision: String?) {
        filterDecision.value = decision
    }
}
