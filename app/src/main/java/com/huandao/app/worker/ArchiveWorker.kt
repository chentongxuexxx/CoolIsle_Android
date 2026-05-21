package com.huandao.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.huandao.app.data.repository.CoolPoolRepository
import com.huandao.app.util.Constants
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * 7 天归档 Worker（US-03 归档部分）。
 *
 * 每天执行一次：
 * - 查找 decided_at > 7 天且 archived_at IS NULL 的条目
 * - 标记 archived_at = now
 */
@HiltWorker
class ArchiveWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: CoolPoolRepository,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        repository.archiveCompletedItems()
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = Constants.WORK_ARCHIVE

        /**
         * 调度每日归档任务。
         */
        fun schedule(workManager: WorkManager) {
            val request = PeriodicWorkRequestBuilder<ArchiveWorker>(
                24, TimeUnit.HOURS
            )
                .setInitialDelay(1, TimeUnit.HOURS)
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
