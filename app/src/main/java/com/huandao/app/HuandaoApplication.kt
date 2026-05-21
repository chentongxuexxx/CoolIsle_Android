package com.huandao.app

import android.app.Application
import androidx.work.WorkManager
import com.huandao.app.notification.NotificationHelper
import com.huandao.app.worker.ArchiveWorker
import com.huandao.app.worker.CoolingExpiryWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * 缓岛 Application 入口。
 * 负责 Hilt DI 容器初始化、通知渠道注册、WorkManager 调度。
 */
@HiltAndroidApp
class HuandaoApplication : Application() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onCreate() {
        super.onCreate()
        // 创建所有通知渠道（冷静期到期 / 周报 / 成就）
        notificationHelper.createAllChannels()

        // 调度后台任务
        scheduleWorkers()
    }

    /**
     * 调度定期后台任务：
     * - CoolingExpiryWorker：每 15 分钟检查到期条目
     * - ArchiveWorker：每天归档已决策超过 7 天的条目
     */
    private fun scheduleWorkers() {
        val workManager = WorkManager.getInstance(this)
        CoolingExpiryWorker.schedule(workManager)
        ArchiveWorker.schedule(workManager)
    }
}
