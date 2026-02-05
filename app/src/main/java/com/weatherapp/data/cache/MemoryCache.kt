package com.weatherapp.data.cache

import android.graphics.Bitmap
import android.util.LruCache
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryCache @Inject constructor() {

    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8

    private val cache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }

    fun get(key: String): Bitmap? {
        return cache.get(key)
    }

    fun put(key: String, bitmap: Bitmap) {
        if (get(key) == null) {
            cache.put(key, bitmap)
        }
    }

    fun clear() {
        cache.evictAll()
    }
}
