package com.weatherapp.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.weatherapp.data.api.OpenWeatherApi
import com.weatherapp.data.cache.ImageCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IconRepository @Inject constructor(
    private val imageCache: ImageCache
) {
    suspend fun getWeatherIcon(iconCode: String): Result<Bitmap> {
        val cacheKey = "weather_icon_$iconCode"

        // Check cache first to avoid network calls
        imageCache.get(cacheKey)?.let {
            return Result.success(it)
        }

        return withContext(Dispatchers.IO) {
            try {
                val iconUrl = "${OpenWeatherApi.ICON_BASE_URL}$iconCode@2x.png"
                val url = URL(iconUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.apply {
                    connectTimeout = 10000
                    readTimeout = 10000
                    doInput = true
                }

                connection.connect()

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream.close()
                    connection.disconnect()

                    if (bitmap != null) {
                        imageCache.put(cacheKey, bitmap)
                        Result.success(bitmap)
                    } else {
                        Result.failure(Exception("Failed to decode image"))
                    }
                } else {
                    connection.disconnect()
                    Result.failure(Exception("HTTP ${connection.responseCode}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
