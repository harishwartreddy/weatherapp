package com.weatherapp.data.cache

import android.graphics.Bitmap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageCache @Inject constructor(
    private val memoryCache: MemoryCache,
    private val diskCache: DiskCache
) {
    // Two-level cache: memory (fast) -> disk (persistent)
    suspend fun get(key: String): Bitmap? {
        memoryCache.get(key)?.let { return it }

        // Promote from disk to memory on cache hit
        diskCache.get(key)?.let { bitmap ->
            memoryCache.put(key, bitmap)
            return bitmap
        }

        return null
    }

    suspend fun put(key: String, bitmap: Bitmap) {
        memoryCache.put(key, bitmap)
        diskCache.put(key, bitmap)
    }

    fun clearMemory() {
        memoryCache.clear()
    }

    fun clearAll() {
        memoryCache.clear()
        diskCache.clear()
    }
}
