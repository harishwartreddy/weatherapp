package com.weatherapp.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun WeatherIcon(
    bitmap: Bitmap?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = contentDescription,
                modifier = Modifier.size(size)
            )
        } else {
            CircularProgressIndicator(
                modifier = Modifier.size(size / 2)
            )
        }
    }
}
