package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.WeatherConditionType
import com.example.data.model.WeatherUIState
import com.example.data.repository.WeatherRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Atmosphere 3D", appName)
    }

    @Test
    fun `repository generates fallback data for default cities`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = WeatherRepository(context)
        val city = WeatherRepository.DEFAULT_CITIES.first()
        val data = repo.createFallbackData(city)

        assertNotNull(data)
        assertEquals(city.name, data.city.name)
        assertTrue(data.hourlyForecast.isNotEmpty())
        assertTrue(data.dailyForecast.isNotEmpty())
    }

    @Test
    fun `temperature conversion format handles celsius and fahrenheit`() {
        val stateCelsius = WeatherUIState(isFahrenheit = false)
        assertEquals("20°C", stateCelsius.formatTemp(20.0))

        val stateFahrenheit = WeatherUIState(isFahrenheit = true)
        assertEquals("68°F", stateFahrenheit.formatTemp(20.0))
    }

    @Test
    fun `weather condition flags identify precipitation and snow`() {
        assertTrue(WeatherConditionType.LIGHT_RAIN.isPrecipitating)
        assertTrue(WeatherConditionType.HEAVY_RAIN.isPrecipitating)
        assertTrue(WeatherConditionType.THUNDERSTORM.hasLightning)
        assertTrue(WeatherConditionType.HEAVY_SNOW.hasSnow)
        assertTrue(WeatherConditionType.CLEAR_NIGHT.isNight)
    }

    @Test
    fun `tilt state updates sensor and drag offsets with bounds clamping`() {
        val tiltState = com.example.ui.engine3d.TiltState()
        tiltState.updateSensor(0.5f, -0.4f)
        assertEquals(0.5f, tiltState.sensorTiltX, 0.01f)
        assertEquals(-0.4f, tiltState.sensorTiltY, 0.01f)

        tiltState.addDrag(100f, 50f)
        assertTrue(tiltState.tiltX > 0.5f)

        tiltState.decayDrag()
        assertTrue(tiltState.tiltX <= 1.5f)
        assertTrue(tiltState.tiltY >= -1.5f)
    }
}
