package com.weatherapp.ui.state

import com.weatherapp.domain.model.CitySearchResult

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Searching : SearchUiState
    data class Results(val cities: List<CitySearchResult>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}
