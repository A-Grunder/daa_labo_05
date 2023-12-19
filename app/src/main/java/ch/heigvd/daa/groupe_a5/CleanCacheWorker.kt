package ch.heigvd.daa.groupe_a5

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.io.File

class CleanCacheWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        cleanCache(applicationContext)
        notifyCacheCleared()
        return Result.success()
    }

    private fun notifyCacheCleared() {
        val activity = applicationContext as MainActivity
        activity.onCacheCleared()
    }

    companion object {
        fun cleanCache(context: Context) {
            val cacheDir = File(context.cacheDir, "image_cache")
            cacheDir.listFiles()?.forEach { file ->
                file.delete()
            }
        }
    }
}