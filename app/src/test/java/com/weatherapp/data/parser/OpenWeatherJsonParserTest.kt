package com.weatherapp.data.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OpenWeatherJsonParserTest {

    private lateinit var parser: OpenWeatherJsonParser

    @Before
    fun setup() {
        parser = OpenWeatherJsonParser()
    }

    @Test
    fun `parseWeatherResponse extracts all fields correctly`() {
        val json = """
            {
                "coord": {"lon": -122.08, "lat": 37.39},
                "weather": [{"id": 800, "main": "Clear", "description": "clear sky", "icon": "01d"}],
                "main": {
                    "temp": 20.5,
                    "feels_like": 19.8,
                    "temp_min": 18.0,
                    "temp_max": 23.0,
                    "pressure": 1013,
                    "humidity": 65
                },
                "visibility": 10000,
                "wind": {"speed": 3.5},
                "clouds": {"all": 0},
                "sys": {"country": "US", "sunrise": 1609459200, "sunset": 1609495200},
                "name": "Mountain View"
            }
        """.trimIndent()

        val result = parser.parseWeatherResponse(json)

        assertEquals("Mountain View", result.cityName)
        assertEquals("US", result.country)
        assertEquals(20.5, result.temperature, 0.01)
        assertEquals(19.8, result.feelsLike, 0.01)
        assertEquals(18.0, result.tempMin, 0.01)
        assertEquals(23.0, result.tempMax, 0.01)
        assertEquals(1013, result.pressure)
        assertEquals(65, result.humidity)
        assertEquals(3.5, result.windSpeed, 0.01)
        assertEquals("Clear", result.weatherCondition)
        assertEquals("clear sky", result.weatherDescription)
        assertEquals("01d", result.iconCode)
        assertEquals(10000, result.visibility)
        assertEquals(0, result.cloudiness)
        assertEquals(1609459200L, result.sunrise)
        assertEquals(1609495200L, result.sunset)
        assertEquals(37.39, result.latitude, 0.01)
        assertEquals(-122.08, result.longitude, 0.01)
    }

    @Test
    fun `parseWeatherResponse handles missing optional fields`() {
        val json = """
            {
                "coord": {"lon": -74.01, "lat": 40.71},
                "weather": [{"id": 500, "main": "Rain", "description": "light rain", "icon": "10d"}],
                "main": {
                    "temp": 15.0,
                    "feels_like": 14.0,
                    "temp_min": 13.0,
                    "temp_max": 17.0,
                    "pressure": 1008,
                    "humidity": 80
                },
                "sys": {"country": "US", "sunrise": 1609459200, "sunset": 1609495200},
                "name": "New York"
            }
        """.trimIndent()

        val result = parser.parseWeatherResponse(json)

        assertEquals("New York", result.cityName)
        assertEquals(0.0, result.windSpeed, 0.01)
        assertEquals(0, result.visibility)
        assertEquals(0, result.cloudiness)
    }

    @Test
    fun `parseCitySearchResponse handles multiple results`() {
        val json = """
            [
                {"name": "New York", "state": "NY", "country": "US", "lat": 40.71, "lon": -74.01},
                {"name": "New York", "state": "TX", "country": "US", "lat": 32.16, "lon": -95.30}
            ]
        """.trimIndent()

        val results = parser.parseCitySearchResponse(json)

        assertEquals(2, results.size)
        assertEquals("New York", results[0].name)
        assertEquals("NY", results[0].state)
        assertEquals("US", results[0].country)
        assertEquals(40.71, results[0].latitude, 0.01)
        assertEquals(-74.01, results[0].longitude, 0.01)
        assertEquals("TX", results[1].state)
    }

    @Test
    fun `parseCitySearchResponse handles empty array`() {
        val json = "[]"

        val results = parser.parseCitySearchResponse(json)

        assertTrue(results.isEmpty())
    }

    @Test
    fun `parseCitySearchResponse handles missing state field`() {
        val json = """
            [
                {"name": "Washington", "country": "US", "lat": 38.89, "lon": -77.03}
            ]
        """.trimIndent()

        val results = parser.parseCitySearchResponse(json)

        assertEquals(1, results.size)
        assertEquals("Washington", results[0].name)
        assertNull(results[0].state)
        assertEquals("US", results[0].country)
    }

    @Test
    fun `CitySearchResult displayName formats correctly with state`() {
        val json = """
            [
                {"name": "Los Angeles", "state": "CA", "country": "US", "lat": 34.05, "lon": -118.24}
            ]
        """.trimIndent()

        val results = parser.parseCitySearchResponse(json)

        assertEquals("Los Angeles, CA, US", results[0].displayName)
    }

    @Test
    fun `CitySearchResult displayName formats correctly without state`() {
        val json = """
            [
                {"name": "Washington", "country": "US", "lat": 38.89, "lon": -77.03}
            ]
        """.trimIndent()

        val results = parser.parseCitySearchResponse(json)

        assertEquals("Washington, US", results[0].displayName)
    }
}
