package com.weatherapp.data.cache

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiskCache @Inject constructor(
    private val context: Context
) {
    private val cacheDir: File by lazy {
        File(context.cacheDir, "weather_icons").apply {
            if (!exists()) mkdirs()
        }
    }

    private val maxCacheSize = 10 * 1024 * 1024L // 10MB limit

    suspend fun get(key: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val file = getFileForKey(key)
            if (file.exists()) {
                file.setLastModified(System.currentTimeMillis()) // update for LRU tracking
                BitmapFactory.decodeFile(file.absolutePath)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun put(key: String, bitmap: Bitmap) = withContext(Dispatchers.IO) {
        try {
            trimCacheIfNeeded()
            val file = getFileForKey(key)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        } catch (e: Exception) {
            // Cache write failure is non-critical
        }
    }

    private fun getFileForKey(key: String): File {
        val hashedKey = hashKey(key)
        return File(cacheDir, "$hashedKey.png")
    }

    // MD5 hash for safe filenames (avoids special chars in cache keys)
    private fun hashKey(key: String): String {
        val digest = MessageDigest.getInstance("MD5")
        val bytes = digest.digest(key.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // LRU eviction: delete oldest files when cache exceeds limit
    private fun trimCacheIfNeeded() {
        val files = cacheDir.listFiles() ?: return
        var totalSize = files.sumOf { it.length() }

        if (totalSize > maxCacheSize) {
            files.sortedBy { it.lastModified() }
                .forEach { file ->
                    if (totalSize <= maxCacheSize * 0.8) return // trim to 80% to avoid frequent eviction
                    totalSize -= file.length()
                    file.delete()
                }
        }
    }

    fun clear() {
        cacheDir.listFiles()?.forEach { it.delete() }
    }
}
