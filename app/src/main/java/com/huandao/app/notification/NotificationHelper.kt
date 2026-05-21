package com.huandao.app.notification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.huandao.app.MainActivity
import com.huandao.app.R
import com.huandao.app.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 通知管理助手。
 *
 * 负责：
 * - 通知渠道注册（冷静期到期 / 周报 / 成就）
 * - 温和通知构建（无感叹号、无催促语气）
 * - 免打扰时段检查
 */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    /**
     * 创建所有通知渠道（在 Application.onCreate 中调用）。
     */
    fun createAllChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 冷静期到期提醒渠道
            val expiryChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_EXPIRY,
                context.getString(R.string.notif_channel_expiry_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = context.getString(R.string.notif_channel_expiry_desc)
                setSound(null, null) // 静默振动，不播放声音
                enableVibration(true)
            }

            // 周报渠道
            val weeklyChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_WEEKLY,
                context.getString(R.string.notif_channel_weekly_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = context.getString(R.string.notif_channel_weekly_desc)
                setSound(null, null)
                enableVibration(false)
            }

            // 成就渠道
            val achievementChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ACHIEVEMENT,
                context.getString(R.string.notif_channel_achievement_name),
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = context.getString(R.string.notif_channel_achievement_desc)
                setSound(null, null)
                enableVibration(false)
            }

            notificationManager.createNotificationChannels(
                listOf(expiryChannel, weeklyChannel, achievementChannel)
            )
        }
    }

    /**
     * 发送冷静期到期通知。
     *
     * 文案规范（PRD）：
     * - 标题：「关于「商品名」」
     * - 正文：「你现在怎么想？」
     * - 无感叹号、无催促语气
     *
     * @param itemTitle 商品名
     * @param itemId 条目 ID（用于点击跳转）
     */
    @SuppressLint("MissingPermission")
    fun sendExpiryNotification(itemTitle: String, itemId: String) {
        // 免打扰检查
        if (isInQuietHours()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            // TODO: V2 添加 DeepLink 直接跳转到决策页
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            itemId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_EXPIRY)
            .setSmallIcon(R.drawable.ic_cool_pool)
            .setContentTitle(context.getString(R.string.notif_expiry_title, itemTitle))
            .setContentText(context.getString(R.string.notif_expiry_body))
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(context.getString(R.string.notif_expiry_body)))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(itemId.hashCode(), notification)
    }

    /**
     * 检查当前是否在免打扰时段（22:00-08:00）。
     */
    fun isInQuietHours(): Boolean {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return hour >= Constants.QUIET_HOURS_START || hour < Constants.QUIET_HOURS_END
    }
}
