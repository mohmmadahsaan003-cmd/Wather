package com.example.ui.engine3d

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import kotlin.math.*
import kotlin.random.Random

data class RainDrop(
    var x: Float,
    var y: Float,
    var z: Float, // Depth: 0.15 (far) to 1.0 (near)
    var length: Float,
    var speed: Float,
    var alpha: Float
)

data class SplashRipple(
    var x: Float,
    var y: Float,
    var currentRadius: Float,
    var maxRadius: Float,
    var alpha: Float,
    var active: Boolean = true
)

data class SnowFlake(
    var x: Float,
    var y: Float,
    var z: Float,
    var radius: Float,
    var speed: Float,
    var swayPhase: Float,
    var swaySpeed: Float,
    var swayAmp: Float,
    var rotation: Float,
    var rotationSpeed: Float,
    var alpha: Float
)

data class StarParticle(
    val x: Float,
    val y: Float,
    val baseAlpha: Float,
    val size: Float,
    val twinkleSpeed: Float,
    val phase: Float
)

data class ShootingStar(
    var startX: Float = 0f,
    var startY: Float = 0f,
    var endX: Float = 0f,
    var endY: Float = 0f,
    var progress: Float = 1f, // 0 to 1
    var length: Float = 140f,
    var active: Boolean = false
)

data class DustParticle(
    var x: Float,
    var y: Float,
    var z: Float,
    var speedY: Float,
    var phase: Float,
    var radius: Float,
    var alpha: Float
)

data class LightningBolt(
    val segments: List<Pair<Offset, Offset>>,
    var alpha: Float = 0f,
    var active: Boolean = false
)

class PrecipitationEngine {
    private val random = Random(42)

    val rainDrops = ArrayList<RainDrop>()
    val splashes = ArrayList<SplashRipple>()
    val snowFlakes = ArrayList<SnowFlake>()
    val stars = ArrayList<StarParticle>()
    val shootingStar = ShootingStar()
    val dustParticles = ArrayList<DustParticle>()

    var lightningBolt = LightningBolt(emptyList(), 0f, false)
    var lightningTimer = 0f
    var nextLightningInterval = 5.0f // seconds
    var skyFlashAlpha = 0f

    private var initialized = false
    private var width = 1000f
    private var height = 2000f

    fun initIfNeeded(w: Float, h: Float) {
        if (initialized && abs(width - w) < 10f && abs(height - h) < 10f) return
        width = w
        height = h
        initialized = true

        // Initialize 120 raindrops
        rainDrops.clear()
        for (i in 0 until 120) {
            val z = random.nextFloat() * 0.85f + 0.15f // 0.15 to 1.0
            rainDrops.add(
                RainDrop(
                    x = random.nextFloat() * (w + 400f) - 200f,
                    y = random.nextFloat() * h,
                    z = z,
                    length = (15f + z * 35f),
                    speed = (800f + z * 1200f),
                    alpha = 0.25f + z * 0.65f
                )
            )
        }

        // Initialize splashes pool
        splashes.clear()
        for (i in 0 until 35) {
            splashes.add(SplashRipple(0f, 0f, 0f, 0f, 0f, false))
        }

        // Initialize 80 snowflakes
        snowFlakes.clear()
        for (i in 0 until 80) {
            val z = random.nextFloat() * 0.85f + 0.15f
            snowFlakes.add(
                SnowFlake(
                    x = random.nextFloat() * (w + 200f) - 100f,
                    y = random.nextFloat() * h,
                    z = z,
                    radius = 2.5f + z * 5.0f,
                    speed = 60f + z * 100f,
                    swayPhase = random.nextFloat() * 6.28f,
                    swaySpeed = 1.2f + random.nextFloat() * 1.5f,
                    swayAmp = 20f + z * 35f,
                    rotation = random.nextFloat() * 360f,
                    rotationSpeed = (random.nextFloat() - 0.5f) * 60f,
                    alpha = 0.35f + z * 0.6f
                )
            )
        }

        // Initialize 90 cosmic stars
        stars.clear()
        for (i in 0 until 90) {
            stars.add(
                StarParticle(
                    x = random.nextFloat() * w,
                    y = random.nextFloat() * (h * 0.65f),
                    baseAlpha = 0.3f + random.nextFloat() * 0.7f,
                    size = 1.2f + random.nextFloat() * 2.5f,
                    twinkleSpeed = 1.5f + random.nextFloat() * 3.5f,
                    phase = random.nextFloat() * 6.28f
                )
            )
        }

        // Initialize 40 warm sun dust motes
        dustParticles.clear()
        for (i in 0 until 40) {
            val z = random.nextFloat() * 0.8f + 0.2f
            dustParticles.add(
                DustParticle(
                    x = random.nextFloat() * w,
                    y = random.nextFloat() * h,
                    z = z,
                    speedY = 25f + z * 45f,
                    phase = random.nextFloat() * 6.28f,
                    radius = 2.0f + z * 4.0f,
                    alpha = 0.25f + z * 0.55f
                )
            )
        }
    }

    fun update(
        dt: Float,
        windKmh: Float,
        intensity: Float,
        isRain: Boolean,
        isSnow: Boolean,
        hasLightning: Boolean,
        isNight: Boolean,
        tiltX: Float
    ) {
        val windOffset = (windKmh * 3.5f + tiltX * 80f) * dt
        val groundHorizon = height * 0.88f

        // 1. Update Rain
        if (isRain) {
            val speedFactor = 1.0f + (intensity - 1.0f) * 0.5f
            for (drop in rainDrops) {
                drop.y += drop.speed * dt * speedFactor
                drop.x += windOffset * drop.z * 1.5f

                if (drop.y >= groundHorizon + (1f - drop.z) * 100f) {
                    // Spawn splash on ground
                    spawnSplash(drop.x, groundHorizon + (1f - drop.z) * 80f, drop.z)
                    // Reset to top
                    drop.y = -drop.length - random.nextFloat() * 50f
                    drop.x = random.nextFloat() * (width + 300f) - 150f
                }
            }

            // Update splashes
            for (splash in splashes) {
                if (splash.active) {
                    splash.currentRadius += 45f * dt
                    splash.alpha -= 2.2f * dt
                    if (splash.alpha <= 0f || splash.currentRadius >= splash.maxRadius) {
                        splash.active = false
                    }
                }
            }
        }

        // 2. Update Snow
        if (isSnow) {
            val speedFactor = 1.0f + (intensity - 1.0f) * 0.4f
            for (flake in snowFlakes) {
                flake.y += flake.speed * dt * speedFactor
                flake.swayPhase += flake.swaySpeed * dt
                flake.rotation += flake.rotationSpeed * dt
                val swayX = sin(flake.swayPhase) * flake.swayAmp * dt
                flake.x += swayX + windOffset * flake.z

                if (flake.y >= height + 20f) {
                    flake.y = -20f
                    flake.x = random.nextFloat() * (width + 200f) - 100f
                }
            }
        }

        // 3. Update Dust Motes (Sunny/Clear)
        if (!isRain && !isSnow) {
            for (p in dustParticles) {
                p.y -= p.speedY * dt
                p.phase += 1.8f * dt
                p.x += sin(p.phase) * 15f * dt + windOffset * 0.2f
                if (p.y <= -10f) {
                    p.y = height + 10f
                    p.x = random.nextFloat() * width
                }
            }
        }

        // 4. Update Shooting Star (Night/Aurora)
        if (isNight) {
            if (shootingStar.active) {
                shootingStar.progress += dt * 1.8f
                if (shootingStar.progress >= 1f) {
                    shootingStar.active = false
                }
            } else if (random.nextFloat() < 0.008f) {
                // Trigger shooting star
                shootingStar.startX = random.nextFloat() * (width * 0.8f)
                shootingStar.startY = random.nextFloat() * (height * 0.35f)
                val angle = 0.65f + random.nextFloat() * 0.35f
                val dist = 280f + random.nextFloat() * 200f
                shootingStar.endX = shootingStar.startX + cos(angle) * dist
                shootingStar.endY = shootingStar.startY + sin(angle) * dist
                shootingStar.progress = 0f
                shootingStar.length = 120f + random.nextFloat() * 80f
                shootingStar.active = true
            }
        }

        // 5. Update Lightning & Sky Flash
        if (hasLightning) {
            lightningTimer += dt
            if (lightningTimer >= nextLightningInterval) {
                triggerLightning()
                lightningTimer = 0f
                nextLightningInterval = 4.0f + random.nextFloat() * 6.0f
            }

            if (skyFlashAlpha > 0f) {
                skyFlashAlpha -= dt * 3.5f
                if (skyFlashAlpha < 0f) skyFlashAlpha = 0f
            }

            if (lightningBolt.active) {
                lightningBolt.alpha -= dt * 4.2f
                if (lightningBolt.alpha <= 0f) {
                    lightningBolt = lightningBolt.copy(alpha = 0f, active = false)
                }
            }
        } else {
            skyFlashAlpha = 0f
            lightningBolt = lightningBolt.copy(active = false)
        }
    }

    private fun spawnSplash(x: Float, y: Float, z: Float) {
        val inactive = splashes.firstOrNull { !it.active } ?: return
        inactive.x = x
        inactive.y = y
        inactive.currentRadius = 1.5f
        inactive.maxRadius = (6f + z * 14f)
        inactive.alpha = 0.5f + z * 0.45f
        inactive.active = true
    }

    private fun triggerLightning() {
        val segments = mutableListOf<Pair<Offset, Offset>>()
        val startX = width * (0.25f + random.nextFloat() * 0.5f)
        var curX = startX
        var curY = 40f
        val targetY = height * 0.85f

        while (curY < targetY) {
            val stepY = 25f + random.nextFloat() * 45f
            val stepX = (random.nextFloat() - 0.5f) * 70f
            val nextX = (curX + stepX).coerceIn(40f, width - 40f)
            val nextY = (curY + stepY).coerceAtMost(targetY)
            segments.add(Pair(Offset(curX, curY), Offset(nextX, nextY)))

            // Chance to branch
            if (random.nextFloat() < 0.35f && curY < targetY * 0.75f) {
                var branchX = curX
                var branchY = curY
                for (b in 0 until 3) {
                    val bNextX = branchX + (random.nextFloat() - 0.4f) * 60f
                    val bNextY = branchY + 20f + random.nextFloat() * 30f
                    segments.add(Pair(Offset(branchX, branchY), Offset(bNextX, bNextY)))
                    branchX = bNextX
                    branchY = bNextY
                }
            }

            curX = nextX
            curY = nextY
        }

        lightningBolt = LightningBolt(segments, 1.0f, true)
        skyFlashAlpha = 0.85f // Sky illumination flash
    }
}
