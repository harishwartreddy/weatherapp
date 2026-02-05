package com.weatherapp.domain.model

data class CitySearchResult(
    val name: String,
    val state: String?,
    val country: String,
    val latitude: Double,
    val longitude: Double
) {
    val displayName: String
        get() = buildString {
            append(name)
            state?.let { append(", $it") }
            append(", $country")
        }
}
