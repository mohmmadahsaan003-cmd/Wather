package com.example.ui.engine3d

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlin.math.PI

class TiltState {
    // Current smoothed sensor tilt (-1.5f to 1.5f)
    var sensorTiltX by mutableFloatStateOf(0f)
    var sensorTiltY by mutableFloatStateOf(0f)

    // Interactive user touch drag offset
    var dragTiltX by mutableFloatStateOf(0f)
    var dragTiltY by mutableFloatStateOf(0f)

    // Hardware sensor active flag
    var isSensorAvailable by mutableStateOf(false)

    // Combined tilt for parallax calculations
    val tiltX: Float
        get() = (sensorTiltX + dragTiltX).coerceIn(-1.5f, 1.5f)

    val tiltY: Float
        get() = (sensorTiltY + dragTiltY).coerceIn(-1.5f, 1.5f)

    fun updateSensor(x: Float, y: Float) {
        sensorTiltX = x.coerceIn(-1.5f, 1.5f)
        sensorTiltY = y.coerceIn(-1.5f, 1.5f)
    }

    fun addDrag(deltaX: Float, deltaY: Float) {
        dragTiltX = (dragTiltX + deltaX * 0.003f).coerceIn(-1.5f, 1.5f)
        dragTiltY = (dragTiltY + deltaY * 0.003f).coerceIn(-1.5f, 1.5f)
    }

    fun decayDrag() {
        if (dragTiltX != 0f || dragTiltY != 0f) {
            dragTiltX *= 0.94f
            dragTiltY *= 0.94f
            if (kotlin.math.abs(dragTiltX) < 0.001f) dragTiltX = 0f
            if (kotlin.math.abs(dragTiltY) < 0.001f) dragTiltY = 0f
        }
    }
}

@Composable
fun rememberParallaxTilt(): TiltState {
    val context = LocalContext.current
    val tiltState = remember { TiltState() }

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

        // Prefer ROTATION_VECTOR for accurate fused gyro/accelerometer orientation
        val rotationVectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelerometerSensor = if (rotationVectorSensor == null) {
            sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        } else null

        val activeSensor = rotationVectorSensor ?: accelerometerSensor
        tiltState.isSensorAvailable = activeSensor != null

        val rotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)

        var smoothX = 0f
        var smoothY = 0f
        val smoothingFactor = 0.12f // Smooth low-pass filter factor

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return

                val targetX: Float
                val targetY: Float

                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.getOrientation(rotationMatrix, orientation)

                    // orientation[1] is pitch (tilt front/back, -PI/2 to PI/2)
                    // orientation[2] is roll (tilt left/right, -PI to PI)
                    val pitch = orientation[1] // in radians
                    val roll = orientation[2]   // in radians

                    // Natural resting phone holding angle is ~45° (approx 0.78 rad)
                    val restingPitch = 0.78f
                    val normalizedRoll = (roll / (PI.toFloat() / 3f)).coerceIn(-1.5f, 1.5f)
                    val normalizedPitch = ((pitch - restingPitch) / (PI.toFloat() / 3f)).coerceIn(-1.5f, 1.5f)

                    targetX = -normalizedRoll
                    targetY = normalizedPitch
                } else {
                    // Fallback using raw Accelerometer values
                    val rawX = event.values[0] / 9.8f
                    val rawY = (event.values[1] - 4.5f) / 9.8f
                    targetX = -rawX
                    targetY = rawY
                }

                // Apply low-pass exponential moving average filter
                smoothX += smoothingFactor * (targetX - smoothX)
                smoothY += smoothingFactor * (targetY - smoothY)

                tiltState.updateSensor(smoothX, smoothY)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (sensorManager != null && activeSensor != null) {
            sensorManager.registerListener(listener, activeSensor, SensorManager.SENSOR_DELAY_GAME)
        }

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    return tiltState
}
