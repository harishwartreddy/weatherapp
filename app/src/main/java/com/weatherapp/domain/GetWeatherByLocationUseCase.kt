package com.weatherapp.domain

import com.weatherapp.data.OpenWeatherRepository
import com.weatherapp.domain.model.GeoLocation
import com.weatherapp.domain.model.WeatherData
import javax.inject.Inject

class GetWeatherByLocationUseCase @Inject constructor(
    private val repository: OpenWeatherRepository
) {
    suspend operator fun invoke(location: GeoLocation): Result<WeatherData> {
        return repository.getWeatherByCoordinates(location.latitude, location.longitude)
    }
}
