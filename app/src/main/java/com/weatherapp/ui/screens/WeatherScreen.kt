package com.weatherapp.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weatherapp.domain.model.CitySearchResult
import com.weatherapp.domain.model.WeatherData
import com.weatherapp.ui.state.SearchUiState
import com.weatherapp.ui.state.WeatherUiState
import com.weatherapp.ui.viewmodel.WeatherViewModel

private val PrimaryOrange = Color(0xFFEB6E4B)
private val PrimaryDark = Color(0xFF48484A)
private val BackgroundLight = Color(0xFFF5F5F5)
private val CardBackground = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF2D3748)
private val TextSecondary = Color(0xFF718096)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel,
    onLocationPermissionRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val weatherState by viewModel.weatherState.collectAsState()
    val searchState by viewModel.searchState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val hasLocationPermission by viewModel.locationPermissionGranted.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryDark)
                .padding(top = 48.dp, bottom = 20.dp, start = 20.dp, end = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = PrimaryOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Weather",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Refresh button when weather is loaded
                if (weatherState is WeatherUiState.Success) {
                    IconButton(
                        onClick = {
                            val weather = (weatherState as WeatherUiState.Success).weather
                            viewModel.loadWeatherForCity(weather.cityName)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Search Card
            SearchCard(
                searchQuery = searchQuery,
                onQueryChange = viewModel::updateSearchQuery,
                onClearSearch = viewModel::clearSearch,
                hasLocationPermission = hasLocationPermission,
                onLocationClick = {
                    if (hasLocationPermission) {
                        viewModel.loadWeatherForCurrentLocation()
                    } else {
                        onLocationPermissionRequest()
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search Results (overlay when searching)
            when (val state = searchState) {
                is SearchUiState.Searching -> {
                    SearchingIndicator()
                }
                is SearchUiState.Results -> {
                    SearchResultsList(
                        cities = state.cities,
                        onCitySelected = { city ->
                            viewModel.selectCity(city)
                        }
                    )
                }
                is SearchUiState.Error -> {
                    ErrorCard(message = state.message)
                }
                is SearchUiState.Idle -> {
                    // Show weather content when not searching
                    WeatherContent(
                        weatherState = weatherState,
                        onRetry = { viewModel.loadWeatherForCurrentLocation() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchCard(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    hasLocationPermission: Boolean,
    onLocationClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Search TextField
            TextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                placeholder = {
                    Text("Search for a city...", color = TextSecondary)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextSecondary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = onClearSearch) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = TextSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = BackgroundLight,
                    unfocusedContainerColor = BackgroundLight,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Location Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryOrange)
                    .clickable { onLocationClick() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (hasLocationPermission) "Use Current Location" else "Enable Location",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = PrimaryOrange,
            modifier = Modifier.size(40.dp)
        )
    }
}

@Composable
private fun SearchResultsList(
    cities: List<CitySearchResult>,
    onCitySelected: (CitySearchResult) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column {
            Text(
                text = "Search Results",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.padding(16.dp, 12.dp, 16.dp, 8.dp)
            )
            cities.forEach { city ->
                CityResultItem(city = city, onClick = { onCitySelected(city) })
            }
        }
    }
}

@Composable
private fun CityResultItem(
    city: CitySearchResult,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(PrimaryOrange.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = PrimaryOrange,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = city.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = buildString {
                    city.state?.let { append("$it, ") }
                    append(city.country)
                },
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDC2626).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "!",
                    color = Color(0xFFDC2626),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                color = Color(0xFFDC2626),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun WeatherContent(
    weatherState: WeatherUiState,
    onRetry: () -> Unit
) {
    when (weatherState) {
        is WeatherUiState.Initial -> {
            WelcomeContent()
        }
        is WeatherUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(48.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = PrimaryOrange,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        is WeatherUiState.Success -> {
            WeatherDisplay(
                weather = weatherState.weather,
                icon = weatherState.weatherIcon
            )
        }
        is WeatherUiState.Error -> {
            WeatherErrorContent(
                message = weatherState.message,
                onRetry = onRetry
            )
        }
    }
}

@Composable
private fun WelcomeContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(PrimaryOrange.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.WbSunny,
                contentDescription = null,
                tint = PrimaryOrange,
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Welcome",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Search for a city or use your\ncurrent location to get weather",
            fontSize = 15.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun WeatherDisplay(
    weather: WeatherData,
    icon: Bitmap?
) {
    Column {
        // Main Weather Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = Color.Black.copy(alpha = 0.1f)
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // City Name
                Text(
                    text = "${weather.cityName}, ${weather.country}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                Text(
                    text = getCurrentDate(),
                    fontSize = 14.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Weather Icon and Temperature
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Icon from OpenWeatherMap
                    Box(
                        modifier = Modifier.size(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (icon != null) {
                            Image(
                                bitmap = icon.asImageBitmap(),
                                contentDescription = weather.weatherDescription,
                                modifier = Modifier.size(80.dp)
                            )
                        } else {
                            CircularProgressIndicator(
                                color = PrimaryOrange,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Temperature
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = "${weather.temperature.toInt()}",
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Light,
                            color = TextPrimary
                        )
                        Text(
                            text = "°C",
                            fontSize = 24.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                // Weather Description
                Text(
                    text = weather.weatherDescription.replaceFirstChar { it.uppercase() },
                    fontSize = 18.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // High / Low
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "H: ${weather.tempMax.toInt()}°",
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "L: ${weather.tempMin.toInt()}°",
                        color = Color(0xFF3B82F6),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                icon = Icons.Default.WaterDrop,
                value = "${weather.humidity}%",
                label = "Humidity",
                iconTint = Color(0xFF3B82F6),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Default.Air,
                value = "${weather.windSpeed}",
                label = "Wind m/s",
                iconTint = Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Default.Visibility,
                value = "${weather.visibility / 1000}",
                label = "Vis. km",
                iconTint = Color(0xFF8B5CF6),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Details",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DetailItem("Feels Like", "${weather.feelsLike.toInt()}°C")
                    DetailItem("Pressure", "${weather.pressure} hPa")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DetailItem("Cloudiness", "${weather.cloudiness}%")
                    DetailItem("Condition", weather.weatherCondition)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sun Times
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SunTimeItem(
                    icon = Icons.Default.WbSunny,
                    label = "Sunrise",
                    time = formatUnixTime(weather.sunrise),
                    iconTint = Color(0xFFF59E0B)
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(60.dp)
                        .background(Color(0xFFE5E7EB))
                )
                SunTimeItem(
                    icon = Icons.Default.WbTwilight,
                    label = "Sunset",
                    time = formatUnixTime(weather.sunset),
                    iconTint = Color(0xFFF97316)
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    value: String,
    label: String,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 12.sp, color = TextSecondary)
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}

@Composable
private fun SunTimeItem(
    icon: ImageVector,
    label: String,
    time: String,
    iconTint: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, fontSize = 12.sp, color = TextSecondary)
        Text(
            text = time,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}

@Composable
private fun WeatherErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFFFEE2E2)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "!",
                color = Color(0xFFDC2626),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(PrimaryOrange)
                .clickable { onRetry() }
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Try Again",
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun formatUnixTime(timestamp: Long): String {
    if (timestamp == 0L) return "N/A"
    val date = java.util.Date(timestamp * 1000)
    val format = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
    return format.format(date)
}

private fun getCurrentDate(): String {
    val date = java.util.Date()
    val format = java.text.SimpleDateFormat("EEEE, MMM d", java.util.Locale.getDefault())
    return format.format(date)
}
