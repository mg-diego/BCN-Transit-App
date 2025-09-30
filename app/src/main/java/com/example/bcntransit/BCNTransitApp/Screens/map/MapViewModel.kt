package com.example.bcntransit.BCNTransitApp.Screens.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bcntransit.api.ApiClient
import com.example.bcntransit.api.ApiService
import com.example.bcntransit.model.BicingStation
import com.example.bcntransit.model.LineDto
import com.example.bcntransit.model.NearbyStation
import com.example.bcntransit.model.StationDto
import com.example.bcntransit.screens.map.getNearbyStations
import com.example.bcntransit.screens.map.getUserLocation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.content.Context
import com.example.bcntransit.model.MarkerInfo
import org.maplibre.android.annotations.Icon
import org.maplibre.android.annotations.Marker
import org.maplibre.android.geometry.LatLng
import kotlin.math.*

class MapViewModel(private val context: Context) : ViewModel() {

    var userLocation by mutableStateOf<LatLng?>(null)
        private set
    var nearbyStations by mutableStateOf<List<NearbyStation>>(emptyList())
        private set
    var selectedNearbyStation by mutableStateOf<NearbyStation?>(null)
        private set
    var selectedStation by mutableStateOf<StationDto?>(null)
        private set
    var selectedBicingStation by mutableStateOf<BicingStation?>(null)
        private set
    var selectedStationConnections by mutableStateOf<List<LineDto>?>(null)
        private set
    var isLoadingNearbyStations by mutableStateOf(false)
        private set
    var isLoadingConnections by mutableStateOf(false)
        private set

    private var markerToStation = mutableMapOf<Marker, MarkerInfo>()
    val markerMap = mutableMapOf<String, Marker>()
    var lastSelectedMarker: Marker? = null
        private set
    var lastUpdateLocation: LatLng? = null

    init { startLocationUpdates() }

    private fun startLocationUpdates() {
        viewModelScope.launch {
            while (true) {
                val newLocation = getUserLocation(context)
                if (newLocation != null) {
                    var shouldUpdate = lastUpdateLocation?.let { prev ->
                        distanceMeters(prev.latitude, prev.longitude,
                            newLocation.latitude, newLocation.longitude) > 100
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

    private fun updateNearbyStations() {
        val loc = userLocation ?: return
        isLoadingNearbyStations = true
        viewModelScope.launch {
            try {
                nearbyStations = getNearbyStations(loc.latitude, loc.longitude, 0.5)
            } catch (e: Exception) {
                e.printStackTrace()
                nearbyStations = emptyList()
            } finally {
                isLoadingNearbyStations = false
            }
        }
    }

    /**
     * Calcula distancia en metros entre dos puntos lat/lon usando la fórmula de Haversine
     */
    private fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371000.0 // radio terrestre en metros
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

    fun selectNearbyStation(station: NearbyStation, apiService: ApiService?) {
        selectedNearbyStation = station
        fetchStationDetails(station, apiService)
    }

    private fun fetchStationDetails(station: NearbyStation, apiService: ApiService?) {
        isLoadingConnections = true
        viewModelScope.launch {
            try {
                when (station.type) {
                    "bicing" -> selectedBicingStation =
                        ApiClient.bicingApiService.getBicingStation(station.station_code)
                    else -> {
                        selectedStation = apiService?.getStationByCode(station.station_code)
                        selectedStationConnections =
                            apiService?.getStationConnections(station.station_code)
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

    fun registerMarker(
        stationId: String,
        marker: Marker,
        station: NearbyStation,
        normalIcon: Icon,
        enlargedIcon: Icon
    ) {
        markerMap[stationId] = marker
        markerToStation[marker] = MarkerInfo(
            station = station,
            normalIcon = normalIcon,
            enlargedIcon = enlargedIcon
        )
    }

    fun getStationForMarker(marker: Marker) = markerToStation[marker]

    fun setLastSelectedMarker(marker: Marker) { lastSelectedMarker = marker }

    suspend fun fetchSelectedConnection(
        stationName: String,
        connectionLineCode: String,
        apiService: ApiService?
    ): StationDto? {
        return try {
            val stations = apiService?.getStations()
            stations?.first { it.name == stationName && it.line_code == connectionLineCode }
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
