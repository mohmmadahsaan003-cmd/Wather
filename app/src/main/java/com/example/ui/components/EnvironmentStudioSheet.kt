package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WeatherConditionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnvironmentStudioSheet(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    activeCondition: WeatherConditionType,
    isSimulationMode: Boolean,
    precipitationIntensity: Float,
    windSpeedKmh: Double,
    onSelectCondition: (WeatherConditionType) -> Unit,
    onIntensityChange: (Float) -> Unit,
    onWindSpeedChange: (Double) -> Unit,
    onResetToLive: () -> Unit
) {
    if (!isOpen) return

    val presets = listOf(
        Pair(WeatherConditionType.THUNDERSTORM, "Thunderstorm"),
        Pair(WeatherConditionType.HEAVY_RAIN, "Heavy Rain"),
        Pair(WeatherConditionType.CLEAR_DAY, "Golden Sun"),
        Pair(WeatherConditionType.HEAVY_SNOW, "Snow Blizzard"),
        Pair(WeatherConditionType.AURORA_NIGHT, "Polar Aurora"),
        Pair(WeatherConditionType.CLEAR_NIGHT, "Cosmic Night"),
        Pair(WeatherConditionType.FOG, "Mist & Fog"),
        Pair(WeatherConditionType.PARTLY_CLOUDY_DAY, "Partly Cloudy")
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        contentColor = Color.White,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.4f))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "3D Environment Studio",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Real-time precipitation & atmospheric engine",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }

                if (isSimulationMode) {
                    TextButton(
                        onClick = onResetToLive,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF38BDF8))
                    ) {
                        Text("Reset to Live", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "ENVIRONMENT PRESETS",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Presets Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.heightIn(max = 240.dp)
            ) {
                items(presets) { (type, label) ->
                    val isSelected = isSimulationMode && activeCondition == type

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isSelected) Color(0xFF38BDF8).copy(alpha = 0.25f)
                                else Color.White.copy(alpha = 0.08f)
                            )
                            .border(
                                width = 1.5.dp,
                                color = if (isSelected) Color(0xFF38BDF8) else Color.Transparent,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                onSelectCondition(type)
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = getConditionIcon(type),
                            contentDescription = null,
                            tint = getConditionTint(type),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Precipitation Intensity Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PRECIPITATION INTENSITY",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp
                )
                Text(
                    text = "${String.format(java.util.Locale.US, "%.1f", precipitationIntensity)}x",
                    color = Color(0xFF38BDF8),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Slider(
                value = precipitationIntensity,
                onValueChange = onIntensityChange,
                valueRange = 0.2f..2.5f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF38BDF8),
                    activeTrackColor = Color(0xFF38BDF8),
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                ),
                modifier = Modifier.testTag("intensity_slider")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Wind Speed Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WIND VELOCITY",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp
                )
                Text(
                    text = "${Math.round(windSpeedKmh)} km/h",
                    color = Color(0xFF38BDF8),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Slider(
                value = windSpeedKmh.toFloat(),
                onValueChange = { onWindSpeedChange(it.toDouble()) },
                valueRange = 0f..65f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF38BDF8),
                    activeTrackColor = Color(0xFF38BDF8),
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                ),
                modifier = Modifier.testTag("wind_slider")
            )
        }
    }
}
