package com.weatherapp.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherApi {

    companion object {
        const val BASE_URL = "https://api.openweathermap.org/"
        const val ICON_BASE_URL = "https://openweathermap.org/img/wn/"
    }

    @GET("data/2.5/weather")
    suspend fun getWeatherByCoordinates(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): Response<String>

    @GET("geo/1.0/direct")
    suspend fun searchCities(
        @Query("q") query: String,
        @Query("limit") limit: Int,
        @Query("appid") apiKey: String
    ): Response<String>

    @GET("geo/1.0/reverse")
    suspend fun reverseGeocode(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("limit") limit: Int = 1,
        @Query("appid") apiKey: String
    ): Response<String>
}
