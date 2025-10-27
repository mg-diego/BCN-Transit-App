package com.bcntransit.app.BCNTransitApp.Screens.map

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bcntransit.app.api.ApiClient
import com.bcntransit.app.api.ApiService
import com.bcntransit.app.model.MarkerInfo
import com.bcntransit.app.model.transport.*
import com.bcntransit.app.screens.map.getNearbyStations
import com.bcntransit.app.screens.map.getUserLocation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.maplibre.android.annotations.Icon
import org.maplibre.android.annotations.Marker
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.plugins.annotation.SymbolManager
import kotlin.math.*

class MapViewModel(private val context: Context) : ViewModel() {

    var symbolManager: SymbolManager? = null
    val symbolMap = mutableMapOf<String, org.maplibre.android.plugins.annotation.Symbol>()

    // --- Modelo de filtros extra ---
    data class ExtraFilters(
        val distance: Double,
        val onlySlotsAvailable: Boolean,
        val onlyElectricalBikesAvailable: Boolean,
        val onlyMechanicalBikesAvailable: Boolean
    )

    // Computed property para usar en filtros
    val selectedExtraFilters: ExtraFilters
        get() = ExtraFilters(
            distance = selectedDistance,
            onlySlotsAvailable = onlySlotsAvailable,
            onlyElectricalBikesAvailable = onlyElectricalBikesAvailable,
            onlyMechanicalBikesAvailable = onlyMechanicalBikesAvailable
        )

    // --- Estado ---
    var userLocation by mutableStateOf<LatLng?>(null)
        private set
    var nearbyStations by mutableStateOf<List<NearbyStation>>(emptyList())
        private set
    var selectedNearbyStation by mutableStateOf<NearbyStation?>(null)
        private set
    var selectedStation by mutableStateOf<StationDto?>(null)
        private set
    var selectedBicingStation by mutableStateOf<BicingStationDto?>(null)
        private set
    var selectedStationConnections by mutableStateOf<List<LineDto>?>(null)
        private set
    var isLoadingNearbyStations by mutableStateOf(false)
        private set
    var isLoadingConnections by mutableStateOf(false)
        private set

    private val _selectedFilters = MutableStateFlow(
        setOf("Metro", "Bus", "Tram", "Rodalies", "FGC", "Bicing")
    )
    val selectedFilters: StateFlow<Set<String>> = _selectedFilters

    private var markerToStation = mutableMapOf<Marker, MarkerInfo>()
    val markerMap = mutableMapOf<String, Marker>()
    var lastSelectedMarker: Marker? = null
        private set
    var lastUpdateLocation: LatLng? = null

    // --- Filtros ---
    var selectedDistance by mutableStateOf(0.5)
        private set
    fun updateDistance(distance: Double) { selectedDistance = distance }

    var onlySlotsAvailable by mutableStateOf(false)
        private set
    fun updateOnlySlotsAvailable(value: Boolean) { onlySlotsAvailable = value }

    var onlyElectricalBikesAvailable by mutableStateOf(false)
        private set
    fun updateOnlyElectricalBikesAvailable(value: Boolean) { onlyElectricalBikesAvailable = value }

    var onlyMechanicalBikesAvailable by mutableStateOf(false)
        private set
    fun updateOnlyMechanicalBikesAvailable(value: Boolean) { onlyMechanicalBikesAvailable = value }

    fun setFilters(filters: Set<String>) { _selectedFilters.value = filters }

    // --- Ubicación y estaciones ---
    init { startLocationUpdates() }

    private fun startLocationUpdates() {
        viewModelScope.launch {
            while (true) {
                val newLocation = getUserLocation(context)
                if (newLocation != null) {
                    val shouldUpdate = lastUpdateLocation?.let { prev ->
                        distanceMeters(prev.latitude, prev.longitude, newLocation.latitude, newLocation.longitude) > 100
                    } ?: true

                    userLocation = newLocation

                    if (shouldUpdate && !isLoadingNearbyStations) {
                        lastUpdateLocation = newLocation
                        updateNearbyStations()
                    }
                }
                delay(5000)
            }
        }
    }

    fun updateNearbyStations() {
        val loc = userLocation ?: return
        isLoadingNearbyStations = true
        viewModelScope.launch {
            try {
                nearbyStations = getNearbyStations(loc.latitude, loc.longitude, selectedDistance)
            } catch (e: Exception) {
                e.printStackTrace()
                nearbyStations = emptyList()
            } finally {
                isLoadingNearbyStations = false
            }
        }
    }

    private fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

    // --- Selección de estaciones ---
    fun selectNearbyStation(station: NearbyStation, apiService: ApiService?) {
        selectedNearbyStation = station
        fetchStationDetails(station, apiService)
    }

    private fun fetchStationDetails(station: NearbyStation, apiService: ApiService?) {
        isLoadingConnections = true
        viewModelScope.launch {
            try {
                when (station.type.lowercase()) {
                    "bicing" -> selectedBicingStation =
                        ApiClient.bicingApiService.getBicingStation(station.station_code)
                    else -> {
                        selectedStation = apiService?.getStationByCode(station.station_code)
                        selectedStationConnections = apiService?.getStationConnections(station.station_code)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                selectedStation = null
                selectedBicingStation = null
                selectedStationConnections = null
            } finally {
                isLoadingConnections = false
            }
        }
    }

    fun clearSelection() {
        selectedNearbyStation = null
        selectedStation = null
        selectedBicingStation = null
        selectedStationConnections = null
        lastSelectedMarker = null
    }

    // --- Markers ---
    fun registerMarker(
        stationId: String,
        marker: Marker,
        station: NearbyStation,
        normalIcon: Icon,
        enlargedIcon: Icon
    ) {
        markerMap[stationId] = marker
        markerToStation[marker] = MarkerInfo(station, normalIcon, enlargedIcon)
    }

    fun getStationForMarker(marker: Marker) = markerToStation[marker]
    fun setLastSelectedMarker(marker: Marker) { lastSelectedMarker = marker }

    suspend fun fetchSelectedConnection(
        stationName: String,
        connectionLineCode: String,
        apiService: ApiService?
    ): StationDto? {
        return try {
            apiService?.getStationsByLine(connectionLineCode)?.first { it.name == stationName }
        } catch (e: Exception) {
            null
        }
    }

    override fun onCleared() {
        super.onCleared()
        markerMap.clear()
        markerToStation.clear()
        lastSelectedMarker = null
    }
}
