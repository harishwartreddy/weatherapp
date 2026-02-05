package com.weatherapp.data.parser

import com.weatherapp.domain.model.CitySearchResult
import com.weatherapp.domain.model.WeatherData
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

class OpenWeatherJsonParser @Inject constructor() {

    fun parseWeatherResponse(jsonString: String): WeatherData {
        val json = JSONObject(jsonString)

        val coord = json.getJSONObject("coord")
        val weather = json.getJSONArray("weather").getJSONObject(0)
        val main = json.getJSONObject("main")
        val wind = json.optJSONObject("wind") ?: JSONObject()
        val clouds = json.optJSONObject("clouds") ?: JSONObject()
        val sys = json.getJSONObject("sys")

        return WeatherData(
            cityName = json.getString("name"),
            country = sys.optString("country", ""),
            temperature = main.getDouble("temp"),
            feelsLike = main.getDouble("feels_like"),
            tempMin = main.getDouble("temp_min"),
            tempMax = main.getDouble("temp_max"),
            humidity = main.getInt("humidity"),
            pressure = main.getInt("pressure"),
            windSpeed = wind.optDouble("speed", 0.0),
            weatherCondition = weather.getString("main"),
            weatherDescription = weather.getString("description"),
            iconCode = weather.getString("icon"),
            visibility = json.optInt("visibility", 0),
            cloudiness = clouds.optInt("all", 0),
            sunrise = sys.optLong("sunrise", 0L),
            sunset = sys.optLong("sunset", 0L),
            latitude = coord.getDouble("lat"),
            longitude = coord.getDouble("lon")
        )
    }

    fun parseCitySearchResponse(jsonString: String): List<CitySearchResult> {
        val jsonArray = JSONArray(jsonString)
        val results = mutableListOf<CitySearchResult>()

        for (i in 0 until jsonArray.length()) {
            val city = jsonArray.getJSONObject(i)
            results.add(
                CitySearchResult(
                    name = city.getString("name"),
                    state = city.optString("state").takeIf { it.isNotEmpty() },
                    country = city.getString("country"),
                    latitude = city.getDouble("lat"),
                    longitude = city.getDouble("lon")
                )
            )
        }

        return results
    }
}
