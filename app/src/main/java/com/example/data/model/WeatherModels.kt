package com.example.data.model

import com.squareup.moshi.Json

enum class WeatherConditionType(
    val displayName: String,
    val description: String,
    val isPrecipitating: Boolean,
    val hasLightning: Boolean,
    val hasSnow: Boolean,
    val isNight: Boolean
) {
    CLEAR_DAY("Sunny", "Clear skies and abundant sunshine", false, false, false, false),
    CLEAR_NIGHT("Clear Night", "Starlit sky with glowing celestial moon", false, false, false, true),
    PARTLY_CLOUDY_DAY("Partly Cloudy", "Scattered clouds with warm sunshine", false, false, false, false),
    PARTLY_CLOUDY_NIGHT("Partly Cloudy Night", "Drifting clouds under moonlight", false, false, false, true),
    OVERCAST("Overcast", "Dense cloud cover with diffused light", false, false, false, false),
    FOG("Mist & Fog", "Low-lying atmospheric fog and reduced visibility", false, false, false, false),
    LIGHT_RAIN("Light Rain", "Gentle refreshing rainfall", true, false, false, false),
    HEAVY_RAIN("Heavy Rain", "Intense downpour with dynamic rain sheets", true, false, false, false),
    THUNDERSTORM("Thunderstorm", "Severe thunderstorm with dynamic lightning strikes", true, true, false, false),
    LIGHT_SNOW("Light Snow", "Gentle swirling crystalline snowflakes", false, false, true, false),
    HEAVY_SNOW("Heavy Snow", "Crisp snow flurry with blustery winds", false, false, true, false),
    AURORA_NIGHT("Polar Aurora", "Vibrant celestial aurora curtains and shooting stars", false, false, false, true)
}

// Open-Meteo Forecast Response Models
data class OpenMeteoResponse(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val timezone: String? = null,
    val current: CurrentUnits? = null,
    val hourly: HourlyData? = null,
    val daily: DailyData? = null
)

data class CurrentUnits(
    val time: String? = null,
    @Json(name = "temperature_2m") val temperature: Double? = null,
    @Json(name = "relative_humidity_2m") val humidity: Int? = null,
    @Json(name = "apparent_temperature") val apparentTemperature: Double? = null,
    @Json(name = "is_day") val isDay: Int? = null,
    val precipitation: Double? = null,
    @Json(name = "weather_code") val weatherCode: Int? = null,
    @Json(name = "cloud_cover") val cloudCover: Int? = null,
    @Json(name = "pressure_msl") val pressureMsl: Double? = null,
    @Json(name = "wind_speed_10m") val windSpeed: Double? = null,
    @Json(name = "wind_direction_10m") val windDirection: Double? = null,
    @Json(name = "uv_index") val uvIndex: Double? = null
)

data class HourlyData(
    val time: List<String>? = null,
    @Json(name = "temperature_2m") val temperature: List<Double>? = null,
    @Json(name = "precipitation_probability") val precipitationProbability: List<Int>? = null,
    @Json(name = "weather_code") val weatherCode: List<Int>? = null
)

data class DailyData(
    val time: List<String>? = null,
    @Json(name = "weather_code") val weatherCode: List<Int>? = null,
    @Json(name = "temperature_2m_max") val temperatureMax: List<Double>? = null,
    @Json(name = "temperature_2m_min") val temperatureMin: List<Double>? = null,
    val sunrise: List<String>? = null,
    val sunset: List<String>? = null,
    @Json(name = "uv_index_max") val uvIndexMax: List<Double>? = null,
    @Json(name = "precipitation_probability_max") val precipitationProbabilityMax: List<Int>? = null
)

// Geocoding Response
data class GeocodingResponse(
    val results: List<GeocodingResult>? = null
)

data class GeocodingResult(
    val id: Long? = null,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    @Json(name = "admin1") val adminRegion: String? = null,
    val timezone: String? = null
)

// UI Domain Models
data class CityLocation(
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val region: String = "",
    val isFavorite: Boolean = false
)

data class HourlyForecastItem(
    val timeLabel: String,
    val hour: Int,
    val temperatureC: Double,
    val condition: WeatherConditionType,
    val precipitationChance: Int,
    val isCurrentHour: Boolean = false
)

data class DailyForecastItem(
    val dayName: String,
    val dateLabel: String,
    val minTempC: Double,
    val maxTempC: Double,
    val condition: WeatherConditionType,
    val precipitationChance: Int
)

data class WeatherUIState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val city: CityLocation = CityLocation("Tokyo", "Japan", 35.6762, 139.6503, "Tokyo Prefecture", true),
    val currentTempC: Double = 18.0,
    val feelsLikeTempC: Double = 17.5,
    val condition: WeatherConditionType = WeatherConditionType.CLEAR_DAY,
    val humidityPercent: Int = 62,
    val windSpeedKmh: Double = 12.5,
    val windDirectionDegrees: Double = 145.0,
    val uvIndex: Double = 4.2,
    val pressureHpa: Double = 1014.0,
    val visibilityKm: Double = 10.0,
    val dewPointC: Double = 10.5,
    val precipitationMm: Double = 0.0,
    val sunriseTime: String = "06:12 AM",
    val sunsetTime: String = "05:48 PM",
    val daylightProgress: Float = 0.65f, // 0.0 at sunrise, 1.0 at sunset
    val isDay: Boolean = true,
    val hourlyForecast: List<HourlyForecastItem> = emptyList(),
    val dailyForecast: List<DailyForecastItem> = emptyList(),
    // Settings & Simulator Controls
    val isFahrenheit: Boolean = false,
    val isSimulationMode: Boolean = false,
    val simulatedCondition: WeatherConditionType? = null,
    val precipitationIntensity: Float = 1.0f, // 0.2 to 2.0
    val windIntensity: Float = 1.0f,
    val savedCities: List<CityLocation> = emptyList()
) {
    val activeCondition: WeatherConditionType
        get() = if (isSimulationMode && simulatedCondition != null) simulatedCondition else condition

    fun formatTemp(celsius: Double): String {
        return if (isFahrenheit) {
            val f = celsius * 9.0 / 5.0 + 32.0
            "${Math.round(f)}°F"
        } else {
            "${Math.round(celsius)}°C"
        }
    }

    fun formatTempShort(celsius: Double): String {
        return if (isFahrenheit) {
            val f = celsius * 9.0 / 5.0 + 32.0
            "${Math.round(f)}°"
        } else {
            "${Math.round(celsius)}°"
        }
    }

    fun formatWindSpeed(kmh: Double): String {
        return if (isFahrenheit) {
            val mph = kmh * 0.621371
            String.format(java.util.Locale.US, "%.1f mph", mph)
        } else {
            String.format(java.util.Locale.US, "%.1f km/h", kmh)
        }
    }
}
