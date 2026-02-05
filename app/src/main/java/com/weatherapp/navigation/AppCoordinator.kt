package com.weatherapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.weatherapp.ui.screens.WeatherScreen
import com.weatherapp.ui.viewmodel.WeatherViewModel

@Composable
fun AppCoordinator(
    viewModel: WeatherViewModel,
    navController: NavHostController = rememberNavController(),
    onRequestLocationPermission: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Weather.route
    ) {
        composable(Routes.Weather.route) {
            WeatherScreen(
                viewModel = viewModel,
                onLocationPermissionRequest = onRequestLocationPermission
            )
        }
    }
}
