package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.*
import com.example.ui.engine3d.WeatherEnvironment3D
import com.example.ui.engine3d.rememberParallaxTilt

@Composable
fun WeatherAppScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()

    val tiltState = rememberParallaxTilt()
    val scrollState = rememberScrollState()

    var showSearchSheet by remember { mutableStateOf(false) }
    var showStudioSheet by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("weather_app_root")
    ) {
        // 1. Full-Bleed 3D Animated Environment Background & Real-Time Precipitation Effects
        WeatherEnvironment3D(
            condition = state.activeCondition,
            windSpeedKmh = state.windSpeedKmh,
            precipitationIntensity = state.precipitationIntensity,
            isDay = state.isDay,
            tiltState = tiltState,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Foreground Glassmorphic Weather Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(scrollState)
        ) {
            // Hero Weather Card (Top bar, Temperature, Condition, Tilt gesture hint)
            WeatherHeroCard(
                state = state,
                isGyroActive = tiltState.isSensorAvailable,
                onOpenSearch = { showSearchSheet = true },
                onOpenStudio = { showStudioSheet = true },
                onToggleUnit = { viewModel.toggleUnit() },
                onRefresh = { viewModel.refreshWeather() }
            )

            // Dynamic spacing for 3D Centerpiece viewing window
            Spacer(modifier = Modifier.height(180.dp))

            // 24-Hour Forecast
            HourlyForecastRow(
                state = state,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 7-Day Daily Forecast
            DailyForecastCard(
                state = state,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Weather Metrics Grid (Wind Compass, Humidity, UV, Pressure, Solar Arc, Visibility)
            WeatherMetricsGrid(
                state = state,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Safe insets at bottom for navigation bar
            Spacer(modifier = Modifier.navigationBarsPadding())
        }

        // City Search & Saved Places Bottom Sheet
        CitySearchSheet(
            isOpen = showSearchSheet,
            onDismiss = { showSearchSheet = false },
            savedCities = state.savedCities,
            searchResults = searchResults,
            isSearching = isSearching,
            onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
            onSelectCity = { viewModel.selectCity(it) },
            onRemoveSavedCity = { viewModel.removeSavedCity(it) }
        )

        // 3D Environment Studio Sheet
        EnvironmentStudioSheet(
            isOpen = showStudioSheet,
            onDismiss = { showStudioSheet = false },
            activeCondition = state.activeCondition,
            isSimulationMode = state.isSimulationMode,
            precipitationIntensity = state.precipitationIntensity,
            windSpeedKmh = state.windSpeedKmh,
            onSelectCondition = { viewModel.setSimulationCondition(it) },
            onIntensityChange = { viewModel.setPrecipitationIntensity(it) },
            onWindSpeedChange = { viewModel.setWindSpeed(it) },
            onResetToLive = { viewModel.resetToLiveForecast() }
        )
    }
}
