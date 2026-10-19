package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HourlyForecastItem
import com.example.data.model.WeatherUIState

@Composable
fun HourlyForecastRow(
    state: WeatherUIState,
    modifier: Modifier = Modifier
) {
    if (state.hourlyForecast.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .glassEffect(cornerRadius = 24.dp, alpha = 0.20f, borderAlpha = 0.28f)
            .padding(vertical = 16.dp)
            .testTag("hourly_forecast_section")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "HOURLY FORECAST",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.hourlyForecast) { item ->
                HourlyItemCard(item = item, state = state)
            }
        }
    }
}

@Composable
private fun HourlyItemCard(
    item: HourlyForecastItem,
    state: WeatherUIState
) {
    val isCurrent = item.isCurrentHour

    val bgBrush = if (isCurrent) {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.35f),
                Color.White.copy(alpha = 0.22f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.15f),
                Color.White.copy(alpha = 0.08f)
            )
        )
    }

    val borderBrush = if (isCurrent) {
        Brush.verticalGradient(
            colors = listOf(Color.White.copy(alpha = 0.6f), Color(0xFF38BDF8).copy(alpha = 0.5f))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color.White.copy(alpha = 0.25f), Color.White.copy(alpha = 0.1f))
        )
    }

    Column(
        modifier = Modifier
            .width(68.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(bgBrush)
            .border(1.dp, borderBrush, RoundedCornerShape(18.dp))
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Time Label
        Text(
            text = item.timeLabel,
            color = if (isCurrent) Color.White else Color.White.copy(alpha = 0.85f),
            fontSize = 12.sp,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Weather Icon
        Icon(
            imageVector = getConditionIcon(item.condition),
            contentDescription = null,
            tint = getConditionTint(item.condition),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Temperature
        Text(
            text = state.formatTempShort(item.temperatureC),
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Precipitation Probability
        if (item.precipitationChance > 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "${item.precipitationChance}%",
                    color = Color(0xFF38BDF8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}
