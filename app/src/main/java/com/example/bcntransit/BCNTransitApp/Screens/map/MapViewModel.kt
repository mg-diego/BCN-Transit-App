package com.example.bcntransit.BCNTransitApp.Screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bcntransit.api.ApiClient
import com.example.bcntransit.model.NearbyStation
import com.example.bcntransit.model.StationDto
import com.example.bcntransit.screens.map.getNearbyStations
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {

    private var _stations: List<StationDto>? = null
    val stations: List<StationDto> get() = _stations ?: emptyList()

    private var _cachedNearbyStations: List<NearbyStation> = emptyList()
    val cachedNearbyStations: List<NearbyStation> get() = _cachedNearbyStations

    // Cargar estaciones (solo si no están cacheadas)
    fun loadStations() = viewModelScope.launch {
        if (_stations == null) {
            val results = listOf(
                async { ApiClient.metroApiService.getStations() },
                async { ApiClient.busApiService.getStations() },
                async { ApiClient.rodaliesApiService.getStations() },
                async { ApiClient.fgcApiService.getStations() },
                async { ApiClient.tramApiService.getStations() }
            ).awaitAll()
            _stations = results.flatten().sortedBy { it.name }
        }
    }

    // Actualizar estaciones cercanas
    fun updateNearbyStations(lat: Double, lon: Double, radius: Double = 0.5) {
        viewModelScope.launch {
            _cachedNearbyStations = getNearbyStations(lat, lon, radius)
        }
    }
}
