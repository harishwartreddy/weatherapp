package com.weatherapp.domain.model

data class WeatherData(
    val cityName: String,
    val country: String,
    val temperature: Double,
    val feelsLike: Double,
    val tempMin: Double,
    val tempMax: Double,
    val humidity: Int,
    val pressure: Int,
    val windSpeed: Double,
    val weatherCondition: String,
    val weatherDescription: String,
    val iconCode: String,
    val visibility: Int,
    val cloudiness: Int,
    val sunrise: Long,
    val sunset: Long,
    val latitude: Double,
    val longitude: Double
)
