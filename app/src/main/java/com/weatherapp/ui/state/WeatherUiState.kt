package com.weatherapp.ui.state

import android.graphics.Bitmap
import com.weatherapp.domain.model.WeatherData

sealed interface WeatherUiState {
    data object Initial : WeatherUiState
    data object Loading : WeatherUiState
    data class Success(
        val weather: WeatherData,
        val weatherIcon: Bitmap? = null
    ) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}
