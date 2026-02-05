package com.weatherapp.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weatherapp.data.IconRepository
import com.weatherapp.data.LocationProvider
import com.weatherapp.data.PreferencesRepository
import com.weatherapp.domain.GetWeatherByCityUseCase
import com.weatherapp.domain.GetWeatherByLocationUseCase
import com.weatherapp.domain.SearchCitiesUseCase
import com.weatherapp.domain.model.CitySearchResult
import com.weatherapp.ui.state.SearchUiState
import com.weatherapp.ui.state.WeatherUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

class WeatherViewModel @Inject constructor(
    private val getWeatherByCityUseCase: GetWeatherByCityUseCase,
    private val getWeatherByLocationUseCase: GetWeatherByLocationUseCase,
    private val searchCitiesUseCase: SearchCitiesUseCase,
    private val iconRepository: IconRepository,
    private val preferencesRepository: PreferencesRepository,
    private val locationProvider: LocationProvider
) : ViewModel() {

    private val _weatherState = MutableStateFlow<WeatherUiState>(WeatherUiState.Initial)
    val weatherState: StateFlow<WeatherUiState> = _weatherState.asStateFlow()

    private val _searchState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _locationPermissionGranted = MutableStateFlow(false)
    val locationPermissionGranted: StateFlow<Boolean> = _locationPermissionGranted.asStateFlow()

    private var searchJob: Job? = null

    init {
        checkLocationPermission()
        loadInitialWeather()
    }

    private fun checkLocationPermission() {
        _locationPermissionGranted.value = locationProvider.hasLocationPermission()
    }

    fun onLocationPermissionResult(granted: Boolean) {
        _locationPermissionGranted.value = granted
        if (granted && _weatherState.value is WeatherUiState.Initial) {
            loadWeatherForCurrentLocation()
        }
    }

    private fun loadInitialWeather() {
        viewModelScope.launch {
            val lastCity = preferencesRepository.lastSearchedCity.first()
            val lastState = preferencesRepository.lastSearchedState.first()

            // Priority: saved city > current location > show search screen
            if (lastCity != null) {
                loadWeatherForCity(lastCity, lastState)
            } else if (locationProvider.hasLocationPermission()) {
                loadWeatherForCurrentLocation()
            }
        }
    }

    fun loadWeatherForCurrentLocation() {
        viewModelScope.launch {
            _weatherState.value = WeatherUiState.Loading

            locationProvider.getCurrentLocation()
                .onSuccess { location ->
                    getWeatherByLocationUseCase(location)
                        .onSuccess { weather ->
                            loadWeatherIcon(weather.iconCode) { icon ->
                                _weatherState.value = WeatherUiState.Success(weather, icon)
                            }
                            preferencesRepository.saveLastSearchedCity(weather.cityName, null)
                        }
                        .onFailure { error ->
                            _weatherState.value = WeatherUiState.Error(
                                getErrorMessage(error)
                            )
                        }
                }
                .onFailure { error ->
                    _weatherState.value = WeatherUiState.Error(
                        "Unable to get location: ${error.message}"
                    )
                }
        }
    }

    fun loadWeatherForCity(cityName: String, stateCode: String? = null) {
        viewModelScope.launch {
            _weatherState.value = WeatherUiState.Loading

            getWeatherByCityUseCase(cityName, stateCode)
                .onSuccess { weather ->
                    loadWeatherIcon(weather.iconCode) { icon ->
                        _weatherState.value = WeatherUiState.Success(weather, icon)
                    }
                    preferencesRepository.saveLastSearchedCity(cityName, stateCode)
                }
                .onFailure { error ->
                    _weatherState.value = WeatherUiState.Error(getErrorMessage(error))
                }
        }
    }

    fun selectCity(city: CitySearchResult) {
        _searchQuery.value = city.displayName
        _searchState.value = SearchUiState.Idle
        loadWeatherForCity(city.name, city.state)
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()

        if (query.length < 2) { // min 2 chars to avoid too many results
            _searchState.value = SearchUiState.Idle
            return
        }

        searchJob = viewModelScope.launch {
            delay(300) // debounce to avoid API spam while typing
            searchCities(query)
        }
    }

    private suspend fun searchCities(query: String) {
        _searchState.value = SearchUiState.Searching

        searchCitiesUseCase(query)
            .onSuccess { cities ->
                _searchState.value = if (cities.isEmpty()) {
                    SearchUiState.Error("No cities found matching '$query'")
                } else {
                    SearchUiState.Results(cities)
                }
            }
            .onFailure { error ->
                _searchState.value = SearchUiState.Error(getErrorMessage(error))
            }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchState.value = SearchUiState.Idle
    }

    private fun loadWeatherIcon(iconCode: String, onLoaded: (Bitmap?) -> Unit) {
        viewModelScope.launch {
            iconRepository.getWeatherIcon(iconCode)
                .onSuccess { bitmap -> onLoaded(bitmap) }
                .onFailure { onLoaded(null) }
        }
    }

    private fun getErrorMessage(error: Throwable): String {
        return when {
            error.message?.contains("City not found") == true ->
                "City not found. Please check the spelling and try again."
            error.message?.contains("HTTP 401") == true ->
                "API key error. Please check your configuration."
            error.message?.contains("Unable to resolve host") == true ||
            error.message?.contains("timeout") == true ->
                "Network error. Please check your internet connection."
            error.message?.contains("HTTP 429") == true ->
                "Too many requests. Please wait a moment and try again."
            error.message?.contains("HTTP 5") == true ->
                "Server error. Please try again later."
            else -> error.message ?: "An unexpected error occurred"
        }
    }
}
