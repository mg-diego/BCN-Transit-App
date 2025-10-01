package com.example.bcntransit.BCNTransitApp.Screens.search.stations

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bcntransit.api.ApiClient
import com.example.bcntransit.model.transport.BicingStationDto
import com.example.bcntransit.screens.map.hasLocationPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.maplibre.android.geometry.LatLng

class BicingViewModel : ViewModel() {
    private val _stations = MutableStateFlow<List<BicingStationDto>>(emptyList())
    val stations: StateFlow<List<BicingStationDto>> = _stations

    private val _selectedFilter = MutableStateFlow("Todos")
    val selectedFilter: StateFlow<String> = _selectedFilter

    init {
        fetchStations()
    }

    private fun fetchStations() {
        viewModelScope.launch {
            try {
                val response: List<BicingStationDto> = ApiClient.bicingApiService.getBicingStations()
                _stations.value = response.filter { it.status == 1 }
            } catch (e: Exception) {
                _stations.value = emptyList()
            }
        }
    }

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    suspend fun getUserLocation(context: Context): LatLng? {
        if (!hasLocationPermission(context)) return null

        val fusedLocationClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(context)

        return try {
            val location = fusedLocationClient.lastLocation.await()
            location?.let { LatLng(it.latitude, it.longitude) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
