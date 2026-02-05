package com.weatherapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.weatherapp.data.IconRepository
import com.weatherapp.data.LocationProvider
import com.weatherapp.data.PreferencesRepository
import com.weatherapp.domain.GetWeatherByCityUseCase
import com.weatherapp.domain.GetWeatherByLocationUseCase
import com.weatherapp.domain.SearchCitiesUseCase
import javax.inject.Inject

class WeatherViewModelFactory @Inject constructor(
    private val getWeatherByCityUseCase: GetWeatherByCityUseCase,
    private val getWeatherByLocationUseCase: GetWeatherByLocationUseCase,
    private val searchCitiesUseCase: SearchCitiesUseCase,
    private val iconRepository: IconRepository,
    private val preferencesRepository: PreferencesRepository,
    private val locationProvider: LocationProvider
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
            return WeatherViewModel(
                getWeatherByCityUseCase,
                getWeatherByLocationUseCase,
                searchCitiesUseCase,
                iconRepository,
                preferencesRepository,
                locationProvider
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
