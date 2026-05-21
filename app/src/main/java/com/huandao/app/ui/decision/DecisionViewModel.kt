package com.huandao.app.ui.decision

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huandao.app.data.repository.CoolPoolRepository
import com.huandao.app.domain.model.CoolPoolItemWithTags
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 决策引导页状态管理（US-07）。
 *
 * 负责加载条目详情、执行决策（购买/放弃）和重置冷静期。
 */
@HiltViewModel
class DecisionViewModel @Inject constructor(
    private val repository: CoolPoolRepository,
) : ViewModel() {

    private val _item = MutableStateFlow<CoolPoolItemWithTags?>(null)
    val item: StateFlow<CoolPoolItemWithTags?> = _item.asStateFlow()

    /**
     * 加载条目详情（含关联标签）。
     */
    fun loadItem(itemId: String) {
        viewModelScope.launch {
            _item.value = repository.getById(itemId)
        }
    }

    /**
     * 做出决策。
     *
     * @param decision "decided_buy" 或 "decided_pass"
     * @param reason 决策理由（可选）
     */
    fun decide(decision: String, reason: String? = null) {
        val current = _item.value ?: return
        viewModelScope.launch {
            repository.decideItem(current.item.id, decision, reason)
        }
    }

    /**
     * 重置冷静期：再等等，回到缓冲状态。
     *
     * @param newHours 新的冷静期小时数（默认 72）
     */
    fun resetCooling(newHours: Int = 72) {
        val current = _item.value ?: return
        viewModelScope.launch {
            repository.resetCooling(current.item.id, newHours)
        }
    }
}
