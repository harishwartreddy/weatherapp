package com.weatherapp.navigation

sealed class Routes(val route: String) {
    data object Weather : Routes("weather")
}
