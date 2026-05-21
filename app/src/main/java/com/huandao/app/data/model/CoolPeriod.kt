package com.huandao.app.data.model

/**
 * 冷静期时长枚举。
 *
 * @property hours 冷静期小时数
 * @property label 中文标签（用于 UI 显示）
 */
enum class CoolPeriod(val hours: Int, val label: String) {
    /** 24 小时 */
    H24(24, "24 小时"),
    /** 72 小时（默认） */
    H72(72, "72 小时"),
    /** 7 天 */
    D7(168, "7 天");

    companion object {
        /** 默认冷静期 */
        val DEFAULT: CoolPeriod = H72

        /** 按小时数查找对应的枚举值，未匹配时返回默认值 */
        fun fromHours(hours: Int): CoolPeriod {
            return entries.find { it.hours == hours } ?: DEFAULT
        }
    }
}
