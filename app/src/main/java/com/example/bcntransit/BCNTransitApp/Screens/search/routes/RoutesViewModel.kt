package com.example.bcntransit.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bcntransit.api.ApiService
import com.example.bcntransit.data.enums.TransportType
import com.example.bcntransit.model.transport.AccessDto
import com.example.bcntransit.model.transport.LineDto
import com.example.bcntransit.model.transport.RouteDto
import com.example.bcntransit.model.transport.StationDto
import com.example.bcntransit.util.toApiError
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class RoutesUiState(
    val routes: List<RouteDto> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

data class StationConnectionsUiState(
    val connections: List<LineDto> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

data class StationAccessesUiState(
    val accesses: List<AccessDto> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

class RoutesViewModel(
    private val apiService: ApiService,
    private val lineCode: String,
    private val stationCode: String? = null
) : ViewModel() {

    private val _routesState = MutableStateFlow(RoutesUiState())
    private val _stationConnectionState = MutableStateFlow(StationConnectionsUiState())
    private val _stationAccessesState = MutableStateFlow(StationAccessesUiState())
    private val _selectedStation = MutableStateFlow<StationDto?>(null)

    val routesState: StateFlow<RoutesUiState> = _routesState
    val stationConnectionsState: StateFlow<StationConnectionsUiState> = _stationConnectionState
    val stationAccessesState: StateFlow<StationAccessesUiState> = _stationAccessesState
    val selectedStation: StateFlow<StationDto?> = _selectedStation


    init {
        viewModelScope.launch {
            stationCode?.let { code ->
                try {
                    val stations = apiService.getStationsByLine(lineCode)
                    _selectedStation.value = stations.find { it.code == code }
                } catch (e: Exception) {
                    e.printStackTrace()
                    _selectedStation.value = null
                }
            }
            fetchRoutesPeriodically()
            fetchConnections()
            fetchAccesses()
        }
    }

    private fun fetchRoutesPeriodically() {
        viewModelScope.launch {
            while (true) {
                val station = _selectedStation.value ?: return@launch
                _routesState.value = _routesState.value.copy(loading = true, error = null)
                try {
                    val routes = apiService.getStationRoutes(station.code).let { list ->
                        if (station.transport_type == TransportType.BUS.type) list
                        else list.filter { it.line_code == lineCode }
                    }
                    _routesState.value = RoutesUiState(routes = routes, loading = false)
                } catch (e: Exception) {
                    val apiError = e.toApiError()
                    _routesState.value = RoutesUiState(routes = emptyList(), loading = false, error = "(${apiError.code}) ${apiError.userMessage}")
                }
                delay(20_000L)
            }
        }
    }

    fun fetchConnections() {
        viewModelScope.launch {
            val station = _selectedStation.value ?: return@launch
            _stationConnectionState.value = _stationConnectionState.value.copy(loading = true, error = null)
            try {
                val connections = apiService.getStationConnections(station.code)
                    .filter { it.transport_type == station.transport_type }
                _stationConnectionState.value = StationConnectionsUiState(
                    connections = connections,
                    loading = false
                )
            } catch (e: Exception) {
                val apiError = e.toApiError()
                _stationConnectionState.value = StationConnectionsUiState(
                    connections = emptyList(),
                    loading = false,
                    error = "(${apiError.code}) ${apiError.userMessage}"
                )
            }
        }
    }

    fun fetchAccesses() {
        viewModelScope.launch {
            val station = _selectedStation.value ?: return@launch
            _stationAccessesState.value = _stationAccessesState.value.copy(loading = true, error = null)
            try {
                val accesses = apiService.getStationAccesses(station.code)
                _stationAccessesState.value = StationAccessesUiState(
                    accesses = accesses,
                    loading = false
                )
            } catch (e: Exception) {
                val apiError = e.toApiError()
                _stationAccessesState.value = StationAccessesUiState(
                    accesses = emptyList(),
                    loading = false,
                    error = "(${apiError.code}) ${apiError.userMessage}"
                )
            }
        }
    }

    suspend fun fetchSelectedConnection(connectionLineCode: String): StationDto? {
        return try {
            val stations = apiService.getStationsByLine(connectionLineCode)
            val connectionStation = stations.first { it.name == _selectedStation.value?.name}
            connectionStation
        } catch (e: Exception) {
            null
        }
    }
}
