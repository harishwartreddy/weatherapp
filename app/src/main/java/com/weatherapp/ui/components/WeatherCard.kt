package com.weatherapp.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.weatherapp.R
import com.weatherapp.domain.model.WeatherData
import com.weatherapp.util.TemperatureFormatter

@Composable
fun WeatherCard(
    weather: WeatherData,
    icon: Bitmap?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${weather.cityName}, ${weather.country}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            WeatherIcon(
                bitmap = icon,
                contentDescription = weather.weatherDescription,
                size = 100.dp
            )

            Text(
                text = TemperatureFormatter.formatCelsius(weather.temperature),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = weather.weatherDescription.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            WeatherDetailsGrid(weather = weather)
        }
    }
}

@Composable
private fun WeatherDetailsGrid(weather: WeatherData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        WeatherDetailItem(
            label = stringResource(R.string.feels_like),
            value = TemperatureFormatter.formatCelsius(weather.feelsLike)
        )
        WeatherDetailItem(
            label = stringResource(R.string.humidity),
            value = "${weather.humidity}%"
        )
        WeatherDetailItem(
            label = stringResource(R.string.wind),
            value = "${weather.windSpeed} m/s"
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        WeatherDetailItem(
            label = stringResource(R.string.pressure),
            value = "${weather.pressure} hPa"
        )
        WeatherDetailItem(
            label = stringResource(R.string.visibility),
            value = "${weather.visibility / 1000} km"
        )
        WeatherDetailItem(
            label = stringResource(R.string.clouds),
            value = "${weather.cloudiness}%"
        )
    }
}

@Composable
private fun WeatherDetailItem(
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}
