package com.weatherapp.domain

import com.weatherapp.data.OpenWeatherRepository
import com.weatherapp.domain.model.WeatherData
import javax.inject.Inject

class GetWeatherByCityUseCase @Inject constructor(
    private val repository: OpenWeatherRepository
) {
    suspend operator fun invoke(cityName: String, stateCode: String? = null): Result<WeatherData> {
        return repository.getWeatherByCity(cityName, stateCode, "US")
    }
}
