package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CityLocation
import com.example.data.model.WeatherConditionType
import com.example.data.model.WeatherUIState
import com.example.data.repository.WeatherRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WeatherRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(
        WeatherRepository.DEFAULT_CITIES.first().let { repository.createFallbackData(it) }
    )
    val uiState: StateFlow<WeatherUIState> = _uiState.asStateFlow()

    private val _searchResults = MutableStateFlow<List<CityLocation>>(emptyList())
    val searchResults: StateFlow<List<CityLocation>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private var searchJob: Job? = null

    init {
        val saved = repository.getSavedCities()
        val initialCity = saved.firstOrNull() ?: WeatherRepository.DEFAULT_CITIES.first()
        loadWeather(initialCity)
    }

    fun loadWeather(city: CityLocation) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val data = repository.getWeatherData(city)
            _uiState.update { current ->
                data.copy(
                    isFahrenheit = current.isFahrenheit,
                    isSimulationMode = current.isSimulationMode,
                    simulatedCondition = current.simulatedCondition,
                    precipitationIntensity = current.precipitationIntensity,
                    windIntensity = current.windIntensity,
                    savedCities = repository.getSavedCities()
                )
            }
        }
    }

    fun refreshWeather() {
        loadWeather(_uiState.value.city)
    }

    fun selectCity(city: CityLocation) {
        repository.saveCity(city)
        loadWeather(city)
    }

    fun removeSavedCity(city: CityLocation) {
        repository.removeCity(city)
        _uiState.update { it.copy(savedCities = repository.getSavedCities()) }
    }

    fun toggleUnit() {
        _uiState.update { it.copy(isFahrenheit = !it.isFahrenheit) }
    }

    fun setSimulationCondition(condition: WeatherConditionType) {
        _uiState.update {
            it.copy(
                isSimulationMode = true,
                simulatedCondition = condition
            )
        }
    }

    fun setPrecipitationIntensity(intensity: Float) {
        _uiState.update { it.copy(precipitationIntensity = intensity) }
    }

    fun setWindSpeed(speed: Double) {
        _uiState.update { it.copy(windSpeedKmh = speed) }
    }

    fun resetToLiveForecast() {
        _uiState.update {
            it.copy(
                isSimulationMode = false,
                simulatedCondition = null,
                precipitationIntensity = 1.0f
            )
        }
        refreshWeather()
    }

    fun onSearchQueryChange(query: String) {
        searchJob?.cancel()
        if (query.trim().isEmpty()) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }

        searchJob = viewModelScope.launch {
            _isSearching.value = true
            delay(350) // Debounce
            val results = repository.searchCities(query)
            _searchResults.value = results
            _isSearching.value = false
        }
    }
}
