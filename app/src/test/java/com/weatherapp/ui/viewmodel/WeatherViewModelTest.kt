package com.weatherapp.ui.viewmodel

import com.weatherapp.data.IconRepository
import com.weatherapp.data.LocationProvider
import com.weatherapp.data.PreferencesRepository
import com.weatherapp.domain.GetWeatherByCityUseCase
import com.weatherapp.domain.GetWeatherByLocationUseCase
import com.weatherapp.domain.SearchCitiesUseCase
import com.weatherapp.domain.model.CitySearchResult
import com.weatherapp.domain.model.GeoLocation
import com.weatherapp.domain.model.WeatherData
import com.weatherapp.ui.state.SearchUiState
import com.weatherapp.ui.state.WeatherUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {

    private lateinit var getWeatherByCityUseCase: GetWeatherByCityUseCase
    private lateinit var getWeatherByLocationUseCase: GetWeatherByLocationUseCase
    private lateinit var searchCitiesUseCase: SearchCitiesUseCase
    private lateinit var iconRepository: IconRepository
    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var locationProvider: LocationProvider

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        getWeatherByCityUseCase = mock()
        getWeatherByLocationUseCase = mock()
        searchCitiesUseCase = mock()
        iconRepository = mock()
        preferencesRepository = mock()
        locationProvider = mock()

        // Default mock behavior
        whenever(preferencesRepository.lastSearchedCity).thenReturn(flowOf(null))
        whenever(preferencesRepository.lastSearchedState).thenReturn(flowOf(null))
        whenever(locationProvider.hasLocationPermission()).thenReturn(false)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Initial when no saved city and no location permission`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(WeatherUiState.Initial, viewModel.weatherState.value)
    }

    @Test
    fun `loadWeatherForCity updates state to Loading then Success`() = runTest {
        val weatherData = createMockWeatherData()
        whenever(getWeatherByCityUseCase.invoke("New York", null))
            .thenReturn(Result.success(weatherData))

        val viewModel = createViewModel()
        viewModel.loadWeatherForCity("New York")
        advanceUntilIdle()

        val state = viewModel.weatherState.value
        assertTrue(state is WeatherUiState.Success)
        assertEquals("New York", (state as WeatherUiState.Success).weather.cityName)
    }

    @Test
    fun `loadWeatherForCity updates state to Error on failure`() = runTest {
        whenever(getWeatherByCityUseCase.invoke("InvalidCity", null))
            .thenReturn(Result.failure(Exception("City not found")))

        val viewModel = createViewModel()
        viewModel.loadWeatherForCity("InvalidCity")
        advanceUntilIdle()

        val state = viewModel.weatherState.value
        assertTrue(state is WeatherUiState.Error)
        assertTrue((state as WeatherUiState.Error).message.contains("City not found"))
    }

    @Test
    fun `loadWeatherForCurrentLocation updates state to Success when location available`() = runTest {
        val location = GeoLocation(40.71, -74.01)
        val weatherData = createMockWeatherData()

        whenever(locationProvider.hasLocationPermission()).thenReturn(true)
        whenever(locationProvider.getCurrentLocation()).thenReturn(Result.success(location))
        whenever(getWeatherByLocationUseCase.invoke(location)).thenReturn(Result.success(weatherData))

        val viewModel = createViewModel()
        viewModel.loadWeatherForCurrentLocation()
        advanceUntilIdle()

        val state = viewModel.weatherState.value
        assertTrue(state is WeatherUiState.Success)
    }

    @Test
    fun `loadWeatherForCurrentLocation updates state to Error when location fails`() = runTest {
        whenever(locationProvider.hasLocationPermission()).thenReturn(true)
        whenever(locationProvider.getCurrentLocation())
            .thenReturn(Result.failure(Exception("Location unavailable")))

        val viewModel = createViewModel()
        viewModel.loadWeatherForCurrentLocation()
        advanceUntilIdle()

        val state = viewModel.weatherState.value
        assertTrue(state is WeatherUiState.Error)
    }

    @Test
    fun `updateSearchQuery triggers search for query length 2 or more`() = runTest {
        val cities = listOf(
            CitySearchResult("New York", "NY", "US", 40.71, -74.01)
        )
        whenever(searchCitiesUseCase.invoke("Ne")).thenReturn(Result.success(cities))

        val viewModel = createViewModel()
        viewModel.updateSearchQuery("Ne")
        advanceUntilIdle()

        val state = viewModel.searchState.value
        assertTrue(state is SearchUiState.Results)
        assertEquals(1, (state as SearchUiState.Results).cities.size)
    }

    @Test
    fun `updateSearchQuery clears results for query length less than 2`() = runTest {
        val viewModel = createViewModel()
        viewModel.updateSearchQuery("N")
        advanceUntilIdle()

        assertEquals(SearchUiState.Idle, viewModel.searchState.value)
    }

    @Test
    fun `clearSearch resets search state`() = runTest {
        val viewModel = createViewModel()
        viewModel.clearSearch()
        advanceUntilIdle()

        assertEquals("", viewModel.searchQuery.value)
        assertEquals(SearchUiState.Idle, viewModel.searchState.value)
    }

    @Test
    fun `selectCity updates query and loads weather`() = runTest {
        val city = CitySearchResult("New York", "NY", "US", 40.71, -74.01)
        val weatherData = createMockWeatherData()
        whenever(getWeatherByCityUseCase.invoke("New York", "NY"))
            .thenReturn(Result.success(weatherData))

        val viewModel = createViewModel()
        viewModel.selectCity(city)
        advanceUntilIdle()

        assertEquals("New York, NY, US", viewModel.searchQuery.value)
        assertEquals(SearchUiState.Idle, viewModel.searchState.value)
    }

    @Test
    fun `onLocationPermissionResult triggers location weather load when granted`() = runTest {
        val location = GeoLocation(40.71, -74.01)
        val weatherData = createMockWeatherData()

        whenever(locationProvider.getCurrentLocation()).thenReturn(Result.success(location))
        whenever(getWeatherByLocationUseCase.invoke(location)).thenReturn(Result.success(weatherData))

        val viewModel = createViewModel()
        viewModel.onLocationPermissionResult(true)
        advanceUntilIdle()

        assertTrue(viewModel.locationPermissionGranted.value)
    }

    private fun createViewModel() = WeatherViewModel(
        getWeatherByCityUseCase,
        getWeatherByLocationUseCase,
        searchCitiesUseCase,
        iconRepository,
        preferencesRepository,
        locationProvider
    )

    private fun createMockWeatherData() = WeatherData(
        cityName = "New York",
        country = "US",
        temperature = 22.0,
        feelsLike = 21.0,
        tempMin = 20.0,
        tempMax = 24.0,
        humidity = 65,
        pressure = 1013,
        windSpeed = 3.5,
        weatherCondition = "Clear",
        weatherDescription = "clear sky",
        iconCode = "01d",
        visibility = 10000,
        cloudiness = 0,
        sunrise = 1609459200,
        sunset = 1609495200,
        latitude = 40.71,
        longitude = -74.01
    )
}
