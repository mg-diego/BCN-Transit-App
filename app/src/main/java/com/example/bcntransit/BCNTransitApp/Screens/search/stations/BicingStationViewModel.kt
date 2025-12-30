package com.bcntransit.app.screens.search.bicing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bcntransit.app.api.ApiService
import com.bcntransit.app.api.BicingApiService
import com.bcntransit.app.model.transport.BicingStationDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BicingStationViewModel(
    private val bicingApiService: BicingApiService,
    private val stationId: String
) : ViewModel() {

    private val _stationState = MutableStateFlow<BicingStationDto?>(null)
    val stationState: StateFlow<BicingStationDto?> = _stationState.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init {
        fetchStationStatus()
    }

    fun fetchStationStatus() {
        viewModelScope.launch {
            try {
                _loading.value = true
                val result = bicingApiService.getBicingStation(stationId)
                _stationState.value = result
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _loading.value = false
            }
        }
    }
}