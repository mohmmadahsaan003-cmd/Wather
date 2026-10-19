package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WeatherUIState
import kotlin.math.*

@Composable
fun WeatherMetricsGrid(
    state: WeatherUIState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Row 1: Wind Compass & Humidity Gauge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            WindCompassCard(
                state = state,
                modifier = Modifier.weight(1f)
            )
            HumidityCard(
                state = state,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: UV Index & Pressure Barometer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            UVIndexCard(
                state = state,
                modifier = Modifier.weight(1f)
            )
            PressureCard(
                state = state,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 3: Solar Arc (Sunrise / Sunset) full width
        SolarArcCard(
            state = state,
            modifier = Modifier.fillMaxWidth()
        )

        // Row 4: Visibility & Precipitation Depth
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            VisibilityCard(
                state = state,
                modifier = Modifier.weight(1f)
            )
            PrecipitationCard(
                state = state,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetricCardHeader(
    icon: ImageVector,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.1.sp
        )
    }
}

@Composable
private fun WindCompassCard(
    state: WeatherUIState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .glassEffect(cornerRadius = 22.dp, alpha = 0.20f, borderAlpha = 0.28f)
            .padding(14.dp)
            .testTag("wind_metric_card")
    ) {
        MetricCardHeader(icon = Icons.Default.Air, title = "WIND")

        Text(
            text = state.formatWindSpeed(state.windSpeedKmh),
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Mini Compass Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(72.dp)) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.width / 2f - 4f

                // Outer Dial
                drawCircle(
                    color = Color.White.copy(alpha = 0.15f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )

                // Cardinal Tick marks
                for (i in 0 until 8) {
                    val angle = (i * 45f) * (PI.toFloat() / 180f)
                    val r1 = radius - 2f
                    val r2 = if (i % 2 == 0) radius - 8f else radius - 5f
                    drawLine(
                        color = Color.White.copy(alpha = if (i % 2 == 0) 0.6f else 0.3f),
                        start = Offset(center.x + cos(angle) * r1, center.y + sin(angle) * r1),
                        end = Offset(center.x + cos(angle) * r2, center.y + sin(angle) * r2),
                        strokeWidth = if (i % 2 == 0) 2.dp.toPx() else 1.dp.toPx()
                    )
                }

                // Rotating Needle
                val needleAngle = (state.windDirectionDegrees - 90.0) * (PI / 180.0)
                val needleLen = radius * 0.75f
                val tipX = center.x + (cos(needleAngle) * needleLen).toFloat()
                val tipY = center.y + (sin(needleAngle) * needleLen).toFloat()
                val backX = center.x - (cos(needleAngle) * needleLen * 0.5f).toFloat()
                val backY = center.y - (sin(needleAngle) * needleLen * 0.5f).toFloat()

                // North arrow
                drawLine(
                    color = Color(0xFFEF4444),
                    start = center,
                    end = Offset(tipX, tipY),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
                // South tail
                drawLine(
                    color = Color.White.copy(alpha = 0.7f),
                    start = center,
                    end = Offset(backX, backY),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                // Pivot Center
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = center
                )
            }
        }

        val dirLabel = getCompassDirection(state.windDirectionDegrees)
        Text(
            text = "Blowing from $dirLabel",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun HumidityCard(
    state: WeatherUIState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .glassEffect(cornerRadius = 22.dp, alpha = 0.20f, borderAlpha = 0.28f)
            .padding(14.dp)
            .testTag("humidity_metric_card")
    ) {
        MetricCardHeader(icon = Icons.Default.WaterDrop, title = "HUMIDITY")

        Text(
            text = "${state.humidityPercent}%",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Circular Humidity Gauge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(72.dp)) {
                val strokeW = 7.dp.toPx()
                val pad = strokeW / 2f
                val arcSize = Size(size.width - pad * 2, size.height - pad * 2)

                // Track Arc
                drawArc(
                    color = Color.White.copy(alpha = 0.15f),
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    topLeft = Offset(pad, pad),
                    size = arcSize,
                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                )

                // Progress Arc
                val sweep = (270f * (state.humidityPercent / 100f)).coerceIn(5f, 270f)
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFF38BDF8))
                    ),
                    startAngle = 135f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(pad, pad),
                    size = arcSize,
                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                )
            }

            Icon(
                imageVector = Icons.Default.WaterDrop,
                contentDescription = null,
                tint = Color(0xFF38BDF8),
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = "Dew point ${state.formatTempShort(state.dewPointC)}",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun UVIndexCard(
    state: WeatherUIState,
    modifier: Modifier = Modifier
) {
    val uv = state.uvIndex
    val (label, badgeColor) = when {
        uv < 3 -> Pair("Low", Color(0xFF10B981))
        uv < 6 -> Pair("Moderate", Color(0xFFF59E0B))
        uv < 8 -> Pair("High", Color(0xFFEA580C))
        uv < 11 -> Pair("Very High", Color(0xFFEF4444))
        else -> Pair("Extreme", Color(0xFF8B5CF6))
    }

    Column(
        modifier = modifier
            .glassEffect(cornerRadius = 22.dp, alpha = 0.20f, borderAlpha = 0.28f)
            .padding(14.dp)
            .testTag("uv_index_card")
    ) {
        MetricCardHeader(icon = Icons.Default.WbSunny, title = "UV INDEX")

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = String.format(java.util.Locale.US, "%.1f", uv),
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(badgeColor.copy(alpha = 0.25f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = label,
                    color = badgeColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // UV Gradient Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(3.5.dp))
                .background(Color.White.copy(alpha = 0.15f))
        ) {
            val fraction = (uv / 12.0).coerceIn(0.05, 1.0).toFloat()
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.5.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF10B981), Color(0xFFFBBF24), Color(0xFFEF4444), Color(0xFF8B5CF6))
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = if (uv >= 3) "Sun protection required" else "Minimal sun hazard",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PressureCard(
    state: WeatherUIState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .glassEffect(cornerRadius = 22.dp, alpha = 0.20f, borderAlpha = 0.28f)
            .padding(14.dp)
            .testTag("pressure_metric_card")
    ) {
        MetricCardHeader(icon = Icons.Default.Speed, title = "PRESSURE")

        Text(
            text = "${Math.round(state.pressureHpa)} hPa",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Barometer Arc
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(50.dp)) {
                val pad = 8.dp.toPx()
                val arcSize = Size(size.width - pad * 2, size.height * 1.8f)

                // Background Arc
                drawArc(
                    color = Color.White.copy(alpha = 0.15f),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(pad, pad),
                    size = arcSize,
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )

                // Pressure indicator needle/point
                val fraction = ((state.pressureHpa - 970.0) / 70.0).coerceIn(0.0, 1.0).toFloat()
                val sweep = 180f * fraction
                drawArc(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF38BDF8), Color(0xFFFBBF24))
                    ),
                    startAngle = 180f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(pad, pad),
                    size = arcSize,
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        Text(
            text = if (state.pressureHpa >= 1013) "High • Clear weather" else "Low • Precipitation risk",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SolarArcCard(
    state: WeatherUIState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .glassEffect(cornerRadius = 24.dp, alpha = 0.20f, borderAlpha = 0.28f)
            .padding(18.dp)
            .testTag("solar_arc_card")
    ) {
        MetricCardHeader(icon = Icons.Default.WbTwilight, title = "SOLAR TRAJECTORY")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Sunrise",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
                Text(
                    text = state.sunriseTime,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Sunset",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
                Text(
                    text = state.sunsetTime,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Dynamic Solar Arc Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(75.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val pad = 12.dp.toPx()
                val arcWidth = size.width - pad * 2
                val arcHeight = size.height * 1.5f
                val arcSize = Size(arcWidth, arcHeight)
                val baseHorizonY = size.height - 10f

                // Horizon dashed line
                drawLine(
                    color = Color.White.copy(alpha = 0.2f),
                    start = Offset(pad, baseHorizonY),
                    end = Offset(size.width - pad, baseHorizonY),
                    strokeWidth = 1.dp.toPx()
                )

                // Sun Daylight Arc Path
                val arcPath = Path()
                arcPath.moveTo(pad, baseHorizonY)
                arcPath.cubicTo(
                    pad + arcWidth * 0.25f, baseHorizonY - arcHeight * 0.85f,
                    pad + arcWidth * 0.75f, baseHorizonY - arcHeight * 0.85f,
                    pad + arcWidth, baseHorizonY
                )

                drawPath(
                    path = arcPath,
                    color = Color.White.copy(alpha = 0.25f),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )

                // Sun Orb position along trajectory
                val p = state.daylightProgress.coerceIn(0f, 1f)
                val t = p
                // Cubic Bezier evaluation
                val p0 = Offset(pad, baseHorizonY)
                val p1 = Offset(pad + arcWidth * 0.25f, baseHorizonY - arcHeight * 0.85f)
                val p2 = Offset(pad + arcWidth * 0.75f, baseHorizonY - arcHeight * 0.85f)
                val p3 = Offset(pad + arcWidth, baseHorizonY)

                val oneMinusT = 1f - t
                val sunX = oneMinusT.pow(3) * p0.x +
                        3 * oneMinusT.pow(2) * t * p1.x +
                        3 * oneMinusT * t.pow(2) * p2.x +
                        t.pow(3) * p3.x
                val sunY = oneMinusT.pow(3) * p0.y +
                        3 * oneMinusT.pow(2) * t * p1.y +
                        3 * oneMinusT * t.pow(2) * p2.y +
                        t.pow(3) * p3.y

                // Sun Glow
                drawCircle(
                    color = Color(0x66FBBF24),
                    radius = 12.dp.toPx(),
                    center = Offset(sunX, sunY)
                )
                // Sun Core
                drawCircle(
                    color = Color(0xFFFDE047),
                    radius = 6.dp.toPx(),
                    center = Offset(sunX, sunY)
                )
            }
        }
    }
}

@Composable
private fun VisibilityCard(
    state: WeatherUIState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .glassEffect(cornerRadius = 22.dp, alpha = 0.20f, borderAlpha = 0.28f)
            .padding(14.dp)
            .testTag("visibility_metric_card")
    ) {
        MetricCardHeader(icon = Icons.Default.Visibility, title = "VISIBILITY")

        val displayVis = if (state.isFahrenheit) {
            String.format(java.util.Locale.US, "%.1f mi", state.visibilityKm * 0.621371)
        } else {
            String.format(java.util.Locale.US, "%.1f km", state.visibilityKm)
        }

        Text(
            text = displayVis,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = when {
                state.visibilityKm >= 10 -> "Perfect optical clarity"
                state.visibilityKm >= 5 -> "Moderate haze"
                else -> "Dense atmospheric obstruction"
            },
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PrecipitationCard(
    state: WeatherUIState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .glassEffect(cornerRadius = 22.dp, alpha = 0.20f, borderAlpha = 0.28f)
            .padding(14.dp)
            .testTag("precipitation_metric_card")
    ) {
        MetricCardHeader(icon = Icons.Default.Grain, title = "PRECIPITATION")

        Text(
            text = "${String.format(java.util.Locale.US, "%.1f", state.precipitationMm)} mm",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = if (state.precipitationMm > 0) "Accumulation in past 24h" else "No rainfall detected",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun getCompassDirection(degrees: Double): String {
    val directions = listOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW")
    val index = Math.round(((degrees % 360) / 22.5)).toInt() % 16
    return directions[index]
}
