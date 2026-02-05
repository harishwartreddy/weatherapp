package com.weatherapp.domain

import com.weatherapp.data.OpenWeatherRepository
import com.weatherapp.domain.model.CitySearchResult
import javax.inject.Inject

class SearchCitiesUseCase @Inject constructor(
    private val repository: OpenWeatherRepository
) {
    suspend operator fun invoke(query: String): Result<List<CitySearchResult>> {
        return repository.searchCities(query, countryCode = "US", limit = 5)
    }
}
