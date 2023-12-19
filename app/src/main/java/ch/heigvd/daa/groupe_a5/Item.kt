package ch.heigvd.daa.groupe_a5

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

data class Item(
    val id: Int,
    val url: URL,
    var timeOfDownload: Long = 0,
) {

    private val cacheSubdir = "image_cache"

    suspend fun isImageCached(context: Context): Boolean = withContext(Dispatchers.IO) {
        // false if more than 5 minutes have passed since the download (inaccurate)
        if (System.currentTimeMillis() - timeOfDownload > 5 * 60 * 1000) {
            return@withContext false
        }

        val cacheDir = File(context.cacheDir, cacheSubdir)
        val cacheFile = File(cacheDir, "$id.jpg")

        return@withContext cacheFile.exists()
    }
    suspend fun downloadImage(context: Context): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val bytes = url.readBytes()
            timeOfDownload = System.currentTimeMillis()
            cacheImage(context, bytes)
            return@withContext bytes
        } catch (e: Exception) {
            Log.w("Item", "Failed to load image $url", e)
            null
        }
    }

    private suspend fun cacheImage(context: Context, bytes: ByteArray) = withContext(Dispatchers.IO) {
        val cacheDir = File(context.cacheDir, cacheSubdir)
        if (!cacheDir.exists()) {
            cacheDir.mkdir()
        }
        val cacheFile = File(cacheDir, "$id.jpg")
        cacheFile.writeBytes(bytes)
    }

    suspend fun getCachedImage(context: Context): ByteArray? = withContext(Dispatchers.IO) {
        val cacheDir = File(context.cacheDir, cacheSubdir)
        val cacheFile = File(cacheDir, "$id.jpg")
        if (!cacheFile.exists()) {
            return@withContext null
        }
        return@withContext cacheFile.readBytes()
    }

    suspend fun decodeImage(bytes: ByteArray?): Bitmap? = withContext(Dispatchers.Default) {
        try {
            BitmapFactory.decodeByteArray(bytes, 0, bytes?.size ?: 0)
        } catch (e: Exception) {
            Log.w("Item", "Failed to decode image $url", e)
            null
        }
    }

}