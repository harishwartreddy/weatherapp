package com.weatherapp.data

import com.weatherapp.BuildConfig
import com.weatherapp.data.api.OpenWeatherApi
import com.weatherapp.data.parser.OpenWeatherJsonParser
import com.weatherapp.domain.model.CitySearchResult
import com.weatherapp.domain.model.WeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenWeatherRepository @Inject constructor(
    private val api: OpenWeatherApi,
    private val parser: OpenWeatherJsonParser
) {
    private val apiKey = BuildConfig.OPENWEATHER_API_KEY

    suspend fun getWeatherByCoordinates(lat: Double, lon: Double): Result<WeatherData> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getWeatherByCoordinates(lat, lon, apiKey)
                if (response.isSuccessful) {
                    val body = response.body() ?: return@withContext Result.failure(
                        Exception("Empty response body")
                    )
                    val weatherData = parser.parseWeatherResponse(body)
                    Result.success(weatherData)
                } else {
                    Result.failure(
                        Exception("API Error: ${response.code()} - ${response.message()}")
                    )
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getWeatherByCity(
        cityName: String,
        stateCode: String? = null,
        countryCode: String = "US"
    ): Result<WeatherData> {
        return withContext(Dispatchers.IO) {
            try {
                val query = buildString {
                    append(cityName)
                    stateCode?.let { append(",$it") }
                    append(",$countryCode")
                }

                // Using Geocoding API per OpenWeatherMap recommendation (city name search is deprecated)
                val geoResponse = api.searchCities(query, 1, apiKey)
                if (!geoResponse.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("Geocoding failed: ${geoResponse.code()}")
                    )
                }

                val cities = parser.parseCitySearchResponse(geoResponse.body() ?: "[]")
                if (cities.isEmpty()) {
                    return@withContext Result.failure(Exception("City not found"))
                }

                // Get weather by coordinates from geocoded location
                val city = cities.first()
                getWeatherByCoordinates(city.latitude, city.longitude)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun searchCities(
        query: String,
        countryCode: String = "US",
        limit: Int = 5
    ): Result<List<CitySearchResult>> {
        return withContext(Dispatchers.IO) {
            try {
                // Request extra results since we filter by country client-side
                val response = api.searchCities(query, limit * 2, apiKey)
                if (response.isSuccessful) {
                    val responseBody = response.body() ?: "[]"
                    val cities = parser.parseCitySearchResponse(responseBody)

                    // Filter to US cities only per requirements
                    val filteredCities = cities.filter {
                        it.country.equals(countryCode, ignoreCase = true)
                    }.take(limit)

                    // Fallback: retry with country code in query if no US results
                    if (filteredCities.isEmpty()) {
                        val searchWithCountry = "$query,$countryCode"
                        val countryResponse = api.searchCities(searchWithCountry, limit, apiKey)
                        if (countryResponse.isSuccessful) {
                            val countryCities = parser.parseCitySearchResponse(
                                countryResponse.body() ?: "[]"
                            )
                            return@withContext Result.success(countryCities.take(limit))
                        }
                    }

                    Result.success(filteredCities)
                } else {
                    val errorCode = response.code()
                    val errorMessage = when (errorCode) {
                        401 -> "Invalid API key"
                        404 -> "City not found"
                        429 -> "Too many requests"
                        else -> "Search failed: HTTP $errorCode"
                    }
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
