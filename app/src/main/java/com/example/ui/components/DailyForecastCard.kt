package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
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
import com.example.data.model.DailyForecastItem
import com.example.data.model.WeatherUIState
import kotlin.math.max
import kotlin.math.min

@Composable
fun DailyForecastCard(
    state: WeatherUIState,
    modifier: Modifier = Modifier
) {
    if (state.dailyForecast.isEmpty()) return

    val weekMin = state.dailyForecast.minOfOrNull { it.minTempC } ?: 0.0
    val weekMax = state.dailyForecast.maxOfOrNull { it.maxTempC } ?: 30.0
    val weekRange = max(1.0, weekMax - weekMin)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .glassEffect(cornerRadius = 24.dp, alpha = 0.20f, borderAlpha = 0.28f)
            .padding(16.dp)
            .testTag("daily_forecast_card")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "7-DAY FORECAST",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
        }

        state.dailyForecast.forEachIndexed { index, item ->
            DailyRowItem(
                item = item,
                state = state,
                weekMin = weekMin,
                weekRange = weekRange
            )
            if (index < state.dailyForecast.size - 1) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .padding(vertical = 4.dp)
                        .background(Color.White.copy(alpha = 0.1f))
                )
            }
        }
    }
}

@Composable
private fun DailyRowItem(
    item: DailyForecastItem,
    state: WeatherUIState,
    weekMin: Double,
    weekRange: Double
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Day Name
        Text(
            text = item.dayName,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(62.dp)
        )

        // Weather Icon + Precipitation %
        Row(
            modifier = Modifier.width(55.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = getConditionIcon(item.condition),
                contentDescription = null,
                tint = getConditionTint(item.condition),
                modifier = Modifier.size(20.dp)
            )
            if (item.precipitationChance >= 15) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${item.precipitationChance}%",
                    color = Color(0xFF38BDF8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Min Temp
        Text(
            text = state.formatTempShort(item.minTempC),
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(36.dp)
        )

        // Horizontal Temperature Range Bar
        val startFraction = ((item.minTempC - weekMin) / weekRange).coerceIn(0.0, 1.0).toFloat()
        val endFraction = ((item.maxTempC - weekMin) / weekRange).coerceIn(0.0, 1.0).toFloat()
        val barWidthFraction = max(0.08f, endFraction - startFraction)

        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.White.copy(alpha = 0.15f))
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                if (startFraction > 0.01f) {
                    Spacer(modifier = Modifier.weight(startFraction))
                }
                Box(
                    modifier = Modifier
                        .weight(barWidthFraction)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF38BDF8), Color(0xFFFBBF24), Color(0xFFEF4444))
                            )
                        )
                )
                val remaining = (1f - (startFraction + barWidthFraction)).coerceAtLeast(0f)
                if (remaining > 0.01f) {
                    Spacer(modifier = Modifier.weight(remaining))
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Max Temp
        Text(
            text = state.formatTempShort(item.maxTempC),
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(36.dp)
        )
    }
}
