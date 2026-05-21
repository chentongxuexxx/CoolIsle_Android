package com.huandao.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.huandao.app.data.db.entity.CoolPoolItemEntity
import com.huandao.app.data.repository.CoolPoolRepository
import com.huandao.app.notification.NotificationHelper
import com.huandao.app.util.Constants
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * 冷静期到期检查 Worker（US-05 通知部分）。
 *
 * 每 15 分钟扫描一次：
 * - 查找 status='cooling' 且 expires_at <= now 的条目
 * - 更新 status='expired'
 * - 发送温和提醒通知（免打扰时段 22:00-08:00 跳过）
 */
@HiltWorker
class CoolingExpiryWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: CoolPoolRepository,
    private val notificationHelper: NotificationHelper,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        // 免打扰时段跳过
        if (notificationHelper.isInQuietHours()) {
            return Result.success()
        }

        // 扫描到期条目
        val expiredItems = repository.getExpiredCoolingItems()

        if (expiredItems.isEmpty()) {
            return Result.success()
        }

        // 批量更新状态
        val expiredIds = expiredItems.map { it.id }
        repository.batchUpdateStatus(expiredIds, CoolPoolItemEntity.STATUS_EXPIRED)

        // 发送通知（每条目一条）
        for (item in expiredItems) {
            notificationHelper.sendExpiryNotification(item.title, item.id)
        }

        return Result.success()
    }

    companion object {
        private const val WORK_NAME = Constants.WORK_EXPIRY_CHECK
        private const val INTERVAL_MINUTES = 15L

        /**
         * 调度定期到期检查任务。
         */
        fun schedule(workManager: WorkManager) {
            val request = PeriodicWorkRequestBuilder<CoolingExpiryWorker>(
                INTERVAL_MINUTES, TimeUnit.MINUTES
            )
                .setInitialDelay(1, TimeUnit.MINUTES)
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
