package com.example.ui.engine3d

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import com.example.data.model.WeatherConditionType
import kotlin.math.*

@Composable
fun WeatherEnvironment3D(
    condition: WeatherConditionType,
    windSpeedKmh: Double,
    precipitationIntensity: Float,
    isDay: Boolean,
    tiltState: TiltState,
    modifier: Modifier = Modifier
) {
    val engine = remember { PrecipitationEngine() }

    // 60 FPS continuous animation clock
    var lastNano by remember { mutableLongStateOf(0L) }
    var animTime by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { frameTimeNanos ->
                if (lastNano != 0L) {
                    val dt = ((frameTimeNanos - lastNano) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                    animTime += dt
                    engine.update(
                        dt = dt,
                        windKmh = windSpeedKmh.toFloat(),
                        intensity = precipitationIntensity,
                        isRain = condition.isPrecipitating,
                        isSnow = condition.hasSnow,
                        hasLightning = condition.hasLightning,
                        isNight = condition.isNight,
                        tiltX = tiltState.tiltX
                    )
                    // Only decay drag offsets, preserving continuous sensor gyro updates!
                    tiltState.decayDrag()
                }
                lastNano = frameTimeNanos
            }
        }
    }

    // Touch interaction for supplementary drag
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        tiltState.addDrag(dragAmount.x, dragAmount.y)
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            engine.initIfNeeded(size.width, size.height)

            // Amplified parallax vectors driven by sensor gyro & accelerometer
            val px = tiltState.tiltX * 85f
            val py = tiltState.tiltY * 65f

            // 1. Dynamic Sky Dome (Depth: Infinite Far)
            drawSkyDome(condition, animTime, engine.skyFlashAlpha, px, py)

            // 2. Celestial Stars & Shooting Stars (Depth: 0.12)
            if (condition.isNight) {
                drawCelestialStars(engine, animTime, px * 0.12f, py * 0.08f)
                if (condition == WeatherConditionType.AURORA_NIGHT) {
                    drawAuroraBorealis(animTime, px * 0.22f, py * 0.14f)
                }
            }

            // 3. Multi-Layer Parallax Mountains & Horizon (Depths: 0.25, 0.50, 0.75)
            drawParallaxMountains(condition, px, py)

            // 4. Background Precipitation Layer (Far particles, Depth: z < 0.55)
            drawParticles(engine, condition, px, py, tiltState.tiltX, isForeground = false)

            // 5. 3D Floating Weather Centerpiece / Celestial Orb (Depth: Focal Plane, with 3D tilt rotation)
            draw3DWeatherCenterpiece(condition, animTime, px, py, tiltState.tiltX, tiltState.tiltY)

            // 6. Foreground Precipitation Layer (Close camera particles, Depth: z >= 0.55)
            drawParticles(engine, condition, px, py, tiltState.tiltX, isForeground = true)

            // 7. Dynamic Lightning Strike (Thunderstorm)
            if (condition.hasLightning && engine.lightningBolt.active) {
                drawLightning(engine.lightningBolt)
            }

            // 8. Ground Splash Ripples (Rain hitting horizon)
            if (condition.isPrecipitating) {
                drawSplashes(engine, px * 0.75f, py * 0.52f)
            }
        }
    }
}

private fun DrawScope.drawSkyDome(
    condition: WeatherConditionType,
    time: Float,
    flashAlpha: Float,
    px: Float,
    py: Float
) {
    val colors = when (condition) {
        WeatherConditionType.CLEAR_DAY -> listOf(
            Color(0xFF0284C7),
            Color(0xFF38BDF8),
            Color(0xFFBAE6FD),
            Color(0xFFFDE68A)
        )
        WeatherConditionType.CLEAR_NIGHT -> listOf(
            Color(0xFF030712),
            Color(0xFF0F172A),
            Color(0xFF1E1B4B),
            Color(0xFF2E1065)
        )
        WeatherConditionType.PARTLY_CLOUDY_DAY -> listOf(
            Color(0xFF0369A1),
            Color(0xFF0EA5E9),
            Color(0xFF7DD3FC),
            Color(0xFFE0F2FE)
        )
        WeatherConditionType.PARTLY_CLOUDY_NIGHT -> listOf(
            Color(0xFF020617),
            Color(0xFF0B1220),
            Color(0xFF1E293B),
            Color(0xFF334155)
        )
        WeatherConditionType.OVERCAST -> listOf(
            Color(0xFF1E293B),
            Color(0xFF334155),
            Color(0xFF475569),
            Color(0xFF64748B)
        )
        WeatherConditionType.FOG -> listOf(
            Color(0xFF334155),
            Color(0xFF475569),
            Color(0xFF64748B),
            Color(0xFF94A3B8)
        )
        WeatherConditionType.LIGHT_RAIN -> listOf(
            Color(0xFF0F172A),
            Color(0xFF1E293B),
            Color(0xFF334155),
            Color(0xFF475569)
        )
        WeatherConditionType.HEAVY_RAIN -> listOf(
            Color(0xFF090D16),
            Color(0xFF0F172A),
            Color(0xFF1E293B),
            Color(0xFF253346)
        )
        WeatherConditionType.THUNDERSTORM -> listOf(
            Color(0xFF070A11),
            Color(0xFF0C1322),
            Color(0xFF141E33),
            Color(0xFF1E293B)
        )
        WeatherConditionType.LIGHT_SNOW -> listOf(
            Color(0xFF0F172A),
            Color(0xFF1E293B),
            Color(0xFF384E6E),
            Color(0xFF64748B)
        )
        WeatherConditionType.HEAVY_SNOW -> listOf(
            Color(0xFF1E293B),
            Color(0xFF334155),
            Color(0xFF475569),
            Color(0xFF78909C)
        )
        WeatherConditionType.AURORA_NIGHT -> listOf(
            Color(0xFF020617),
            Color(0xFF051622),
            Color(0xFF062828),
            Color(0xFF0F172A)
        )
    }

    // Sky gradient shifts subtly with device tilt
    drawRect(
        brush = Brush.verticalGradient(
            colors = colors,
            startY = py * 0.2f,
            endY = size.height + py * 0.2f
        )
    )

    // Lightning Flash Illumination Overlay
    if (flashAlpha > 0f) {
        drawRect(
            color = Color.White.copy(alpha = (flashAlpha * 0.75f).coerceIn(0f, 1f))
        )
    }
}

private fun DrawScope.drawCelestialStars(
    engine: PrecipitationEngine,
    time: Float,
    px: Float,
    py: Float
) {
    for (s in engine.stars) {
        val twinkle = (sin(time * s.twinkleSpeed + s.phase) + 1f) * 0.5f
        val alpha = (s.baseAlpha * twinkle).coerceIn(0.1f, 1f)
        val x = (s.x + px) % size.width
        val y = s.y + py
        drawCircle(
            color = Color.White.copy(alpha = alpha),
            radius = s.size,
            center = Offset(if (x < 0) x + size.width else x, y)
        )
    }

    val ss = engine.shootingStar
    if (ss.active) {
        val curX = ss.startX + (ss.endX - ss.startX) * ss.progress + px
        val curY = ss.startY + (ss.endY - ss.startY) * ss.progress + py
        val tailProgress = (ss.progress - 0.2f).coerceAtLeast(0f)
        val tailX = ss.startX + (ss.endX - ss.startX) * tailProgress + px
        val tailY = ss.startY + (ss.endY - ss.startY) * tailProgress + py

        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(Color.White.copy(alpha = 0f), Color(0xFF67E8F9), Color.White),
                start = Offset(tailX, tailY),
                end = Offset(curX, curY)
            ),
            start = Offset(tailX, tailY),
            end = Offset(curX, curY),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawAuroraBorealis(time: Float, px: Float, py: Float) {
    val h = size.height
    val w = size.width

    for (layer in 0..2) {
        val wavePath = Path()
        val baseY = h * (0.16f + layer * 0.08f) + py
        wavePath.moveTo(0f, baseY)

        val steps = 30
        for (i in 0..steps) {
            val x = (w / steps) * i
            val wave1 = sin(i * 0.25f + time * 0.8f + layer) * 55f
            val wave2 = cos(i * 0.4f - time * 0.5f) * 30f
            val y = baseY + wave1 + wave2
            wavePath.lineTo(x + px, y)
        }
        wavePath.lineTo(w, baseY + 180f)
        wavePath.lineTo(0f, baseY + 180f)
        wavePath.close()

        val auroraColors = if (layer == 0) {
            listOf(
                Color(0x0010B981),
                Color(0x9910B981),
                Color(0x6606B6D4),
                Color(0x008B5CF6)
            )
        } else {
            listOf(
                Color(0x0006B6D4),
                Color(0x888B5CF6),
                Color(0x55EC4899),
                Color(0x003B82F6)
            )
        }

        drawPath(
            path = wavePath,
            brush = Brush.verticalGradient(
                colors = auroraColors,
                startY = baseY - 40f,
                endY = baseY + 180f
            )
        )
    }
}

private fun DrawScope.drawParallaxMountains(
    condition: WeatherConditionType,
    px: Float,
    py: Float
) {
    val w = size.width
    val h = size.height

    // 1. Far mountain range (Depth: 0.25)
    val farPath = Path()
    val farY = h * 0.70f + py * 0.25f
    val farOffsetX = px * 0.25f
    farPath.moveTo(-60f, h)
    farPath.lineTo(-60f, farY)
    farPath.cubicTo(w * 0.2f + farOffsetX, farY - 130f, w * 0.4f + farOffsetX, farY + 60f, w * 0.6f + farOffsetX, farY - 100f)
    farPath.cubicTo(w * 0.75f + farOffsetX, farY - 180f, w * 0.9f + farOffsetX, farY + 20f, w + 60f, farY - 60f)
    farPath.lineTo(w + 60f, h)
    farPath.close()

    val farColor = when (condition) {
        WeatherConditionType.CLEAR_DAY -> Color(0x330284C7)
        WeatherConditionType.CLEAR_NIGHT, WeatherConditionType.AURORA_NIGHT -> Color(0x331E1B4B)
        WeatherConditionType.LIGHT_SNOW, WeatherConditionType.HEAVY_SNOW -> Color(0x4464748B)
        else -> Color(0x33334155)
    }
    drawPath(farPath, farColor)

    // 2. Mid mountain range (Depth: 0.50)
    val midPath = Path()
    val midY = h * 0.78f + py * 0.50f
    val midOffsetX = px * 0.50f
    midPath.moveTo(-60f, h)
    midPath.lineTo(-60f, midY)
    midPath.cubicTo(w * 0.15f + midOffsetX, midY - 95f, w * 0.35f + midOffsetX, midY - 30f, w * 0.55f + midOffsetX, midY - 115f)
    midPath.cubicTo(w * 0.75f + midOffsetX, midY - 65f, w * 0.85f + midOffsetX, midY - 135f, w + 60f, midY - 85f)
    midPath.lineTo(w + 60f, h)
    midPath.close()

    val midColor = when (condition) {
        WeatherConditionType.CLEAR_DAY -> Color(0x550369A1)
        WeatherConditionType.CLEAR_NIGHT, WeatherConditionType.AURORA_NIGHT -> Color(0x550F172A)
        WeatherConditionType.LIGHT_SNOW, WeatherConditionType.HEAVY_SNOW -> Color(0x66334155)
        else -> Color(0x551E293B)
    }
    drawPath(midPath, midColor)

    // 3. Near rolling foothills (Depth: 0.75)
    val nearPath = Path()
    val nearY = h * 0.85f + py * 0.75f
    val nearOffsetX = px * 0.75f
    nearPath.moveTo(-60f, h)
    nearPath.lineTo(-60f, nearY)
    nearPath.cubicTo(w * 0.25f + nearOffsetX, nearY - 55f, w * 0.5f + nearOffsetX, nearY + 25f, w * 0.75f + nearOffsetX, nearY - 45f)
    nearPath.cubicTo(w * 0.88f + nearOffsetX, nearY - 70f, w * 0.95f + nearOffsetX, nearY - 15f, w + 60f, nearY - 35f)
    nearPath.lineTo(w + 60f, h)
    nearPath.close()

    val nearColor = when (condition) {
        WeatherConditionType.CLEAR_DAY -> Color(0x660C4A6E)
        WeatherConditionType.CLEAR_NIGHT, WeatherConditionType.AURORA_NIGHT -> Color(0x66020617)
        WeatherConditionType.LIGHT_SNOW, WeatherConditionType.HEAVY_SNOW -> Color(0x771E293B)
        else -> Color(0x660F172A)
    }
    drawPath(nearPath, nearColor)
}

private fun DrawScope.draw3DWeatherCenterpiece(
    condition: WeatherConditionType,
    time: Float,
    px: Float,
    py: Float,
    tiltX: Float,
    tiltY: Float
) {
    // Focal plane parallax: shifts with high responsiveness
    val centerX = size.width * 0.5f + px * 0.90f
    val centerY = size.height * 0.30f + py * 0.65f + sin(time * 1.5f) * 12f
    val radius = min(size.width * 0.22f, 110f)

    // Specular light source offset reacts to gyro tilt in 3D
    val lightOffsetX = centerX - radius * (0.35f - tiltX * 0.22f)
    val lightOffsetY = centerY - radius * (0.35f - tiltY * 0.22f)

    when {
        // SUNNY / CLEAR DAY
        condition == WeatherConditionType.CLEAR_DAY -> {
            // Corona Rays
            for (i in 0 until 12) {
                val angle = (i * 30f + time * 15f + tiltX * 10f) * (PI.toFloat() / 180f)
                val pulse = 1f + sin(time * 3f + i) * 0.15f
                val rayLen = radius * (1.35f + pulse * 0.35f)
                val startDist = radius * 1.05f
                drawLine(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFDE047), Color(0x00F59E0B)),
                        center = Offset(centerX, centerY),
                        radius = rayLen
                    ),
                    start = Offset(centerX + cos(angle) * startDist, centerY + sin(angle) * startDist),
                    end = Offset(centerX + cos(angle) * rayLen, centerY + sin(angle) * rayLen),
                    strokeWidth = 6f,
                    cap = StrokeCap.Round
                )
            }

            // Outer Aura Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x99FDE047), Color(0x44F59E0B), Color.Transparent),
                    center = Offset(centerX, centerY),
                    radius = radius * 1.7f
                ),
                radius = radius * 1.7f,
                center = Offset(centerX, centerY)
            )

            // 3D Shaded Sun Sphere with Gyro-Responsive Specular Light
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFFBEB),
                        Color(0xFFFEF08A),
                        Color(0xFFFBBF24),
                        Color(0xFFEA580C)
                    ),
                    center = Offset(lightOffsetX, lightOffsetY),
                    radius = radius * 1.25f
                ),
                radius = radius,
                center = Offset(centerX, centerY)
            )

            // 3D Orbiting Ring with Gyro Rotation & Perspective Pitch
            val ringPitch = (radius * 0.76f * (1f + tiltY * 0.35f)).coerceAtLeast(8f)
            rotate(degrees = 25f + tiltX * 30f, pivot = Offset(centerX, centerY)) {
                drawOval(
                    brush = Brush.sweepGradient(
                        colors = listOf(Color(0x00FDE047), Color(0xAAFBBF24), Color(0x00FDE047)),
                        center = Offset(centerX, centerY)
                    ),
                    topLeft = Offset(centerX - radius * 1.45f, centerY - ringPitch * 0.5f),
                    size = Size(radius * 2.9f, ringPitch),
                    style = Stroke(width = 4f)
                )
            }
        }

        // NIGHT / MOON / AURORA
        condition.isNight -> {
            // Celestial Lunar Aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x55E2E8F0), Color(0x2294A3B8), Color.Transparent),
                    center = Offset(centerX, centerY),
                    radius = radius * 1.8f
                ),
                radius = radius * 1.8f,
                center = Offset(centerX, centerY)
            )

            // 3D Shaded Moon Sphere
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFE2E8F0),
                        Color(0xFF94A3B8),
                        Color(0xFF334155)
                    ),
                    center = Offset(lightOffsetX, lightOffsetY),
                    radius = radius * 1.2f
                ),
                radius = radius,
                center = Offset(centerX, centerY)
            )

            // Moon Craters with parallax offset
            val craterOffsets = listOf(
                Pair(Offset(-radius * 0.25f + tiltX * 8f, -radius * 0.15f + tiltY * 6f), radius * 0.18f),
                Pair(Offset(radius * 0.15f + tiltX * 8f, radius * 0.25f + tiltY * 6f), radius * 0.14f),
                Pair(Offset(radius * 0.35f + tiltX * 8f, -radius * 0.2f + tiltY * 6f), radius * 0.10f),
                Pair(Offset(-radius * 0.1f + tiltX * 8f, radius * 0.4f + tiltY * 6f), radius * 0.12f)
            )
            for ((offset, cRad) in craterOffsets) {
                drawCircle(
                    color = Color(0x22475569),
                    radius = cRad,
                    center = Offset(centerX + offset.x, centerY + offset.y)
                )
            }

            // Orbiting Stardust Ring with Gyro Tilt
            val ringPitch = (radius * 0.7f * (1f + tiltY * 0.35f)).coerceAtLeast(8f)
            rotate(degrees = -20f + tiltX * 30f, pivot = Offset(centerX, centerY)) {
                drawOval(
                    brush = Brush.sweepGradient(
                        colors = listOf(Color(0x00CBD5E1), Color(0x88E2E8F0), Color(0x00CBD5E1)),
                        center = Offset(centerX, centerY)
                    ),
                    topLeft = Offset(centerX - radius * 1.4f, centerY - ringPitch * 0.5f),
                    size = Size(radius * 2.8f, ringPitch),
                    style = Stroke(width = 3f)
                )
            }
        }

        // SNOW
        condition.hasSnow -> {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x66BAE6FD), Color(0x2238BDF8), Color.Transparent),
                    center = Offset(centerX, centerY),
                    radius = radius * 1.6f
                ),
                radius = radius * 1.6f,
                center = Offset(centerX, centerY)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFF0F9FF),
                        Color(0xFFE0F2FE),
                        Color(0xFF7DD3FC),
                        Color(0xFF0369A1)
                    ),
                    center = Offset(lightOffsetX, lightOffsetY),
                    radius = radius * 1.15f
                ),
                radius = radius,
                center = Offset(centerX, centerY)
            )

            // Crystalline Prism Spokes with 3D tilt rotation
            for (i in 0 until 6) {
                val angle = (i * 60f + time * 20f + tiltX * 25f) * (PI.toFloat() / 180f)
                val len = radius * 0.9f
                drawLine(
                    color = Color(0x88FFFFFF),
                    start = Offset(centerX, centerY),
                    end = Offset(centerX + cos(angle) * len, centerY + sin(angle) * len),
                    strokeWidth = 2.5f
                )
            }
        }

        // RAIN / THUNDERSTORM / CLOUDY
        else -> {
            val orbColors = if (condition.hasLightning) {
                listOf(Color(0xFF38BDF8), Color(0xFF6366F1), Color(0xFF1E1B4B), Color(0xFF0F172A))
            } else if (condition.isPrecipitating) {
                listOf(Color(0xFFBAE6FD), Color(0xFF0284C7), Color(0xFF1E293B), Color(0xFF0F172A))
            } else {
                listOf(Color(0xFFF1F5F9), Color(0xFF94A3B8), Color(0xFF475569), Color(0xFF1E293B))
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x4438BDF8), Color.Transparent),
                    center = Offset(centerX, centerY),
                    radius = radius * 1.5f
                ),
                radius = radius * 1.5f,
                center = Offset(centerX, centerY)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = orbColors,
                    center = Offset(lightOffsetX, lightOffsetY),
                    radius = radius * 1.25f
                ),
                radius = radius,
                center = Offset(centerX, centerY)
            )

            // Volumetric Cloud Ribbons orbiting with 3D perspective
            for (band in 0..1) {
                val yShift = if (band == 0) -radius * 0.2f else radius * 0.25f
                val rot = (time * 12f * if (band == 0) 1 else -1) + tiltX * 30f
                val bandHeight = (radius * 0.4f * (1f + tiltY * 0.3f)).coerceAtLeast(6f)
                rotate(degrees = rot, pivot = Offset(centerX, centerY)) {
                    drawOval(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0x00FFFFFF), Color(0x77E2E8F0), Color(0x00FFFFFF)),
                            start = Offset(centerX - radius, centerY + yShift),
                            end = Offset(centerX + radius, centerY + yShift)
                        ),
                        topLeft = Offset(centerX - radius * 1.15f, centerY + yShift - bandHeight * 0.5f),
                        size = Size(radius * 2.3f, bandHeight),
                        style = Stroke(width = 6f)
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawParticles(
    engine: PrecipitationEngine,
    condition: WeatherConditionType,
    px: Float,
    py: Float,
    tiltX: Float,
    isForeground: Boolean
) {
    val zThreshold = 0.55f

    // 1. Rain
    if (condition.isPrecipitating) {
        val rainColor = Color(0xFFBAE6FD)
        // Slant angle responds dynamically to device gyro tilt!
        val slantX = tiltX * 18f
        for (drop in engine.rainDrops) {
            val passFilter = if (isForeground) drop.z >= zThreshold else drop.z < zThreshold
            if (!passFilter) continue

            // Foreground drops shift faster than background drops
            val depthScale = if (isForeground) 2.2f else 0.65f
            val parallaxDropX = drop.x + px * drop.z * depthScale
            val parallaxDropY = drop.y + py * drop.z * (depthScale * 0.5f)

            val strokeW = if (isForeground) (2.0f + drop.z * 2.2f) else 1.5f
            drawLine(
                color = rainColor.copy(alpha = drop.alpha),
                start = Offset(parallaxDropX, parallaxDropY),
                end = Offset(parallaxDropX + drop.z * 5f + slantX * drop.z, parallaxDropY + drop.length),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
        }
    }

    // 2. Snow
    if (condition.hasSnow) {
        val snowColor = Color.White
        for (flake in engine.snowFlakes) {
            val passFilter = if (isForeground) flake.z >= zThreshold else flake.z < zThreshold
            if (!passFilter) continue

            val depthScale = if (isForeground) 2.2f else 0.65f
            val pX = flake.x + px * flake.z * depthScale
            val pY = flake.y + py * flake.z * (depthScale * 0.5f)

            drawCircle(
                color = snowColor.copy(alpha = flake.alpha),
                radius = flake.radius,
                center = Offset(pX, pY)
            )

            if (isForeground && flake.z > 0.75f) {
                drawCircle(
                    color = Color(0x44E0F2FE),
                    radius = flake.radius * 2.0f,
                    center = Offset(pX, pY)
                )
            }
        }
    }

    // 3. Sunny Dust Motes
    if (!condition.isPrecipitating && !condition.hasSnow) {
        val moteColor = if (condition.isNight) Color(0xFF93C5FD) else Color(0xFFFDE68A)
        for (mote in engine.dustParticles) {
            val passFilter = if (isForeground) mote.z >= zThreshold else mote.z < zThreshold
            if (!passFilter) continue

            val depthScale = if (isForeground) 2.0f else 0.65f
            val pX = (mote.x + px * mote.z * depthScale) % size.width
            val pY = mote.y + py * mote.z * (depthScale * 0.45f)

            drawCircle(
                color = moteColor.copy(alpha = mote.alpha),
                radius = mote.radius,
                center = Offset(if (pX < 0) pX + size.width else pX, pY)
            )
        }
    }
}

private fun DrawScope.drawLightning(bolt: LightningBolt) {
    if (!bolt.active || bolt.alpha <= 0f) return

    val boltColor = Color(0xFFE0F2FE).copy(alpha = bolt.alpha.coerceIn(0f, 1f))
    val glowColor = Color(0xFF38BDF8).copy(alpha = (bolt.alpha * 0.5f).coerceIn(0f, 1f))

    for ((p1, p2) in bolt.segments) {
        drawLine(
            color = glowColor,
            start = p1,
            end = p2,
            strokeWidth = 9f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = boltColor,
            start = p1,
            end = p2,
            strokeWidth = 3.5f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawSplashes(engine: PrecipitationEngine, px: Float, py: Float) {
    val splashColor = Color(0xFFBAE6FD)
    for (s in engine.splashes) {
        if (!s.active || s.alpha <= 0f) continue
        val sx = s.x + px
        val sy = s.y + py
        drawOval(
            color = splashColor.copy(alpha = s.alpha),
            topLeft = Offset(sx - s.currentRadius, sy - s.currentRadius * 0.35f),
            size = Size(s.currentRadius * 2f, s.currentRadius * 0.7f),
            style = Stroke(width = 1.8f)
        )
    }
}
