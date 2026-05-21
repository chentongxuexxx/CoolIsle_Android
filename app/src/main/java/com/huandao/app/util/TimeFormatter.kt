package com.huandao.app.util

import java.util.concurrent.TimeUnit

/**
 * 友好时间展示工具。
 *
 * 将毫秒级时间戳差值转换为人类可读的「已缓冲 X 小时/天」文案。
 */
object TimeFormatter {

    /**
     * 格式化缓冲时长。
     *
     * @param createdAt 创建时间戳（毫秒）
     * @param now 当前时间戳（毫秒），默认 System.currentTimeMillis()
     * @return 如「已缓冲 3 小时」「已缓冲 2 天」
     */
    fun formatDuration(createdAt: Long, now: Long = System.currentTimeMillis()): String {
        val diffMs = now - createdAt
        if (diffMs < 0) return "刚刚放入"

        val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMs)
        val hours = TimeUnit.MILLISECONDS.toHours(diffMs)
        val days = TimeUnit.MILLISECONDS.toDays(diffMs)

        return when {
            minutes < 1 -> "刚刚放入"
            minutes < 60 -> "已缓冲 ${minutes} 分钟"
            hours < 24 -> "已缓冲 ${hours} 小时"
            else -> "已缓冲 ${days} 天"
        }
    }

    /**
     * 格式化剩余缓冲时间。
     *
     * @param expiresAt 到期时间戳（毫秒）
     * @param now 当前时间戳（毫秒）
     * @return 如「剩余 5 小时」「剩余 1 天」
     */
    fun formatRemaining(expiresAt: Long, now: Long = System.currentTimeMillis()): String {
        val diffMs = expiresAt - now
        if (diffMs <= 0) return "已到期"

        val hours = TimeUnit.MILLISECONDS.toHours(diffMs)
        val days = TimeUnit.MILLISECONDS.toDays(diffMs)

        return when {
            hours < 1 -> "即将到期"
            hours < 24 -> "剩余 ${hours} 小时"
            else -> "剩余 ${days} 天"
        }
    }
}
