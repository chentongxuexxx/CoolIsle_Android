package com.huandao.app.ui.theme

import androidx.annotation.ColorLong

/**
 * 缓岛全局颜色常量，供代码中引用（XML 颜色定义见 res/values/colors.xml）。
 *
 * 设计系统约定：
 * - 主色 #E8913A（暖橙），深色变体 #D48030
 * - 背景 #FFF8F0（暖白）
 * - 文本 #2C2C2A（近黑）、#888780（灰）
 */
object HuandaoColors {

    // ── 品牌色 ──
    const val PRIMARY = 0xFFE8913A
    const val PRIMARY_DARK = 0xFFD48030
    const val BACKGROUND = 0xFFFFF8F0
    const val TEXT_PRIMARY = 0xFF2C2C2A
    const val TEXT_SECONDARY = 0xFF888780

    // ── 状态色标 ──
    /** 缓冲中 — 橙色竖线 */
    const val STATUS_COOLING = 0xFFE8913A
    /** 已到期 — 绿色竖线 */
    const val STATUS_EXPIRED = 0xFF4CAF50
    /** 已决策 — 灰色竖线 */
    const val STATUS_DECIDED = 0xFF9E9E9E

    // ── 情绪标签 ──
    /** 标签背景 */
    const val TAG_BACKGROUND = 0xFFFFF0E0
    /** 标签文字 */
    const val TAG_TEXT = 0xFFE8913A
    /** 标签选中态背景 */
    const val TAG_SELECTED_BG = 0xFFE8913A
    /** 标签选中态文字 */
    const val TAG_SELECTED_TEXT = 0xFFFFFFFF

    // ── 其他 ──
    /** 按下/选中反馈 */
    const val RIPPLE = 0x1AE8913A
    /** 换算文字 */
    const val CONVERSION_TEXT = 0xFF999999
    /** 缓冲时长文字 */
    const val DURATION_TEXT = 0xFF999999
    /** 分隔线 */
    const val DIVIDER = 0xFFEEEEEE
    /** 白色 */
    const val WHITE = 0xFFFFFFFF
}
