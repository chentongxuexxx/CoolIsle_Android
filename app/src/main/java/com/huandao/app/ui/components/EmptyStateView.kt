package com.huandao.app.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.huandao.app.databinding.ViewEmptyStateBinding

/**
 * 空状态引导视图。
 *
 * 在列表为空时展示：MD3 图标 + 温暖标题 + 副标题。
 * XML 属性：
 * - app:emptyTitle / app:emptySubtitle（通过 styleable 定义）
 */
class EmptyStateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewEmptyStateBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    init {
        // 读取自定义属性
        attrs?.let {
            val ta = context.obtainStyledAttributes(it, R.styleable.EmptyStateView, 0, 0)
            try {
                val title = ta.getString(R.styleable.EmptyStateView_emptyTitle)
                val subtitle = ta.getString(R.styleable.EmptyStateView_emptySubtitle)
                if (title != null) setTitle(title)
                if (subtitle != null) setSubtitle(subtitle)
            } finally {
                ta.recycle()
            }
        }
    }

    /** 设置标题文案 */
    fun setTitle(text: String) {
        binding.emptyTitle.text = text
    }

    /** 设置副标题文案 */
    fun setSubtitle(text: String) {
        binding.emptySubtitle.text = text
    }

    /** 设置图标资源 */
    fun setIcon(resId: Int) {
        binding.emptyIcon.setImageResource(resId)
    }
}
