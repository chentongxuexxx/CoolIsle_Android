package com.huandao.app.ui.pool

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huandao.app.data.db.entity.CoolPoolItemEntity
import com.huandao.app.data.db.entity.EmotionTagEntity
import com.huandao.app.data.repository.CoolPoolRepository
import com.huandao.app.data.repository.EmotionTagRepository
import com.huandao.app.util.Constants
import com.huandao.app.util.PriceConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 添加条目面板状态管理。
 *
 * 管理表单字段绑定、实时换算、校验和提交流程。
 */
@HiltViewModel
class AddItemViewModel @Inject constructor(
    private val coolPoolRepository: CoolPoolRepository,
    private val tagRepository: EmotionTagRepository,
) : ViewModel() {

    // ── 表单字段 ──
    val title = MutableStateFlow("")
    val priceText = MutableStateFlow("")
    val category = MutableStateFlow<String?>(null)
    val selectedTagIds = MutableStateFlow<List<String>>(emptyList())
    val coolPeriodHours = MutableStateFlow(Constants.DEFAULT_COOL_PERIOD_HOURS)

    // ── 标签数据 ──
    val allTags: StateFlow<List<EmotionTagEntity>> = tagRepository.getAllTags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── 换算结果 ──
    val conversionResults: StateFlow<List<String>> = priceText
        .combine(MutableStateFlow(Unit)) { text, _ ->
            val price = text.toDoubleOrNull()
            PriceConverter.convert(price)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── 提交按钮是否可用 ──
    val canSubmit: StateFlow<Boolean> = title
        .combine(MutableStateFlow(Unit)) { t, _ -> t.isNotBlank() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // ── 品类列表 ──
    val categories: List<String> = Constants.CATEGORIES

    // ── 冷静期选项 ──
    val coolPeriodOptions: List<CoolPeriodOption> = Constants.COOL_PERIOD_OPTIONS.map {
        CoolPeriodOption(it.hours, it.label)
    }

    /**
     * 切换标签选中状态。
     * 最多选中 [Constants.MAX_TAGS_PER_ITEM] 个。
     */
    fun toggleTag(tagId: String) {
        val current = selectedTagIds.value.toMutableList()
        if (current.contains(tagId)) {
            current.remove(tagId)
        } else {
            if (current.size < Constants.MAX_TAGS_PER_ITEM) {
                current.add(tagId)
            }
        }
        selectedTagIds.value = current
    }

    /**
     * 添加自定义标签。
     *
     * @param name 标签名称
     * @param emoji 对应 emoji
     * @return 新标签实体，若超出上限或名称为空则返回 null
     */
    suspend fun addCustomTag(name: String, emoji: String? = null): EmotionTagEntity? {
        if (name.isBlank()) return null
        if (name.length < Constants.MIN_TAG_NAME_LENGTH ||
            name.length > Constants.MAX_TAG_NAME_LENGTH) return null
        return tagRepository.addCustomTag(name, emoji)
    }

    /**
     * 检查是否还能添加自定义标签。
     */
    suspend fun canAddCustomTag(): Boolean = tagRepository.canAddCustomTag()

    /**
     * 提交条目到冷静池。
     *
     * @return true 如果添加成功，false 如果校验失败
     */
    fun addItem(): Boolean {
        val itemTitle = title.value.trim()
        if (itemTitle.isBlank()) return false
        if (itemTitle.length > Constants.MAX_TITLE_LENGTH) return false

        val price = priceText.value.toDoubleOrNull()
        val itemId = coolPoolRepository.generateId()
        val now = System.currentTimeMillis()
        val expiresAt = now + coolPeriodHours.value * 3600_000L

        val entity = CoolPoolItemEntity(
            id = itemId,
            title = itemTitle,
            price = price,
            category = category.value,
            coolPeriodHours = coolPeriodHours.value,
            createdAt = now,
            expiresAt = expiresAt,
            status = CoolPoolItemEntity.STATUS_COOLING,
        )

        viewModelScope.launch {
            coolPoolRepository.addItem(entity, selectedTagIds.value)
        }

        return true
    }

    /**
     * 重置表单（关闭面板后调用）。
     */
    fun resetForm() {
        title.value = ""
        priceText.value = ""
        category.value = null
        selectedTagIds.value = emptyList()
        coolPeriodHours.value = Constants.DEFAULT_COOL_PERIOD_HOURS
    }
}

/**
 * 冷静期选项 UI 模型。
 */
data class CoolPeriodOption(
    val hours: Int,
    val label: String,
)
