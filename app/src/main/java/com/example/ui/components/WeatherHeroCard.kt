package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WeatherConditionType
import com.example.data.model.WeatherUIState

@Composable
fun WeatherHeroCard(
    state: WeatherUIState,
    isGyroActive: Boolean = true,
    onOpenSearch: () -> Unit,
    onOpenStudio: () -> Unit,
    onToggleUnit: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeCondition = state.activeCondition

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // City Selector Button
            Row(
                modifier = Modifier
                    .glassPill(alpha = 0.28f, borderAlpha = 0.35f)
                    .clickable(onClick = onOpenSearch)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("city_selector_button"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Change Location",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = state.city.name,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }

            // Right Action Controls: Unit Switcher + 3D Studio Button
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Unit Switcher (°C / °F)
                Box(
                    modifier = Modifier
                        .glassPill(alpha = 0.28f, borderAlpha = 0.35f)
                        .clickable(onClick = onToggleUnit)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("unit_toggle_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (state.isFahrenheit) "°F" else "°C",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 3D Environment Studio Button
                Row(
                    modifier = Modifier
                        .glassPill(
                            alpha = if (state.isSimulationMode) 0.5f else 0.28f,
                            borderAlpha = if (state.isSimulationMode) 0.8f else 0.35f
                        )
                        .clickable(onClick = onOpenStudio)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("studio_button"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "3D Environment Studio",
                        tint = if (state.isSimulationMode) Color(0xFFFBBF24) else Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (state.isSimulationMode) "3D Preset" else "3D FX",
                        color = if (state.isSimulationMode) Color(0xFFFBBF24) else Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Refresh Button
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .size(38.dp)
                        .glassPill(alpha = 0.28f, borderAlpha = 0.35f)
                        .testTag("refresh_button")
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Weather",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Active Simulation Banner if simulator enabled
        AnimatedVisibility(
            visible = state.isSimulationMode,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF59E0B).copy(alpha = 0.25f))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFFFDE68A),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "3D Environment Simulator Active",
                    color = Color(0xFFFDE68A),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Large Temperature Display
        Text(
            text = state.formatTempShort(state.currentTempC),
            color = Color.White,
            fontSize = 86.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = (-2).sp
        )

        // Weather Condition Badge
        Row(
            modifier = Modifier
                .glassPill(alpha = 0.35f, borderAlpha = 0.45f)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = getConditionIcon(activeCondition),
                contentDescription = null,
                tint = getConditionTint(activeCondition),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = activeCondition.displayName,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // High / Low and Feels-Like
        val todayMax = state.dailyForecast.firstOrNull()?.maxTempC ?: (state.currentTempC + 3.0)
        val todayMin = state.dailyForecast.firstOrNull()?.minTempC ?: (state.currentTempC - 4.0)

        Text(
            text = "H: ${state.formatTemp(todayMax)}  •  L: ${state.formatTemp(todayMin)}  •  Feels ${state.formatTemp(state.feelsLikeTempC)}",
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 3D Gyro Parallax Interactive Gesture Hint
        Row(
            modifier = Modifier
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Sensors,
                contentDescription = null,
                tint = if (isGyroActive) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = if (isGyroActive) "Gyro Parallax Active • Tilt phone to shift 3D scene" else "3D Parallax • Drag to orbit scene",
                color = if (isGyroActive) Color(0xFFBAE6FD) else Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

fun getConditionIcon(type: WeatherConditionType): ImageVector {
    return when (type) {
        WeatherConditionType.CLEAR_DAY -> Icons.Default.WbSunny
        WeatherConditionType.CLEAR_NIGHT -> Icons.Default.Nightlight
        WeatherConditionType.PARTLY_CLOUDY_DAY -> Icons.Default.CloudQueue
        WeatherConditionType.PARTLY_CLOUDY_NIGHT -> Icons.Default.NightsStay
        WeatherConditionType.OVERCAST, WeatherConditionType.FOG -> Icons.Default.Cloud
        WeatherConditionType.LIGHT_RAIN, WeatherConditionType.HEAVY_RAIN -> Icons.Default.WaterDrop
        WeatherConditionType.THUNDERSTORM -> Icons.Default.FlashOn
        WeatherConditionType.LIGHT_SNOW, WeatherConditionType.HEAVY_SNOW -> Icons.Default.AcUnit
        WeatherConditionType.AURORA_NIGHT -> Icons.Default.AutoAwesome
    }
}

fun getConditionTint(type: WeatherConditionType): Color {
    return when (type) {
        WeatherConditionType.CLEAR_DAY -> Color(0xFFFDE047)
        WeatherConditionType.CLEAR_NIGHT -> Color(0xFFE2E8F0)
        WeatherConditionType.THUNDERSTORM -> Color(0xFF67E8F9)
        WeatherConditionType.LIGHT_RAIN, WeatherConditionType.HEAVY_RAIN -> Color(0xFF38BDF8)
        WeatherConditionType.LIGHT_SNOW, WeatherConditionType.HEAVY_SNOW -> Color(0xFFBAE6FD)
        WeatherConditionType.AURORA_NIGHT -> Color(0xFF34D399)
        else -> Color.White
    }
}
