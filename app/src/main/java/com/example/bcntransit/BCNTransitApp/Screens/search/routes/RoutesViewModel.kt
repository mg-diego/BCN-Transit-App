package com.example.bcntransit.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bcntransit.api.ApiService
import com.example.bcntransit.data.enums.TransportType
import com.example.bcntransit.model.LineDto
import com.example.bcntransit.model.RouteDto
import com.example.bcntransit.model.StationDto
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class RoutesUiState(
    val routes: List<RouteDto> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

data class ConnectionsUiState(
    val connections: List<LineDto> = emptyList(),
    val connectionStation: StationDto? = null,
    val loading: Boolean = false,
    val error: String? = null
)

data class ConnectionStationUiState(
    val connectionStation: StationDto? = null,
    val loading: Boolean = false,
    val error: String? = null
)

class RoutesViewModel(
    private val apiService: ApiService,
    private val station: StationDto,
    private val lineCode: String
) : ViewModel() {

    private val _routesState = MutableStateFlow(RoutesUiState())
    private val _connectionsState = MutableStateFlow(ConnectionsUiState())
    private val _connectionStationState = MutableStateFlow(ConnectionStationUiState())
    val routesState: StateFlow<RoutesUiState> = _routesState
    val connectionsState: StateFlow<ConnectionsUiState> = _connectionsState
    val connectionStationState: StateFlow<ConnectionStationUiState> = _connectionStationState

    init {
        fetchRoutesPeriodically()
        fetchConnections()
    }

    private fun fetchRoutesPeriodically() {
        viewModelScope.launch {
            while (true) {
                _routesState.value = _routesState.value.copy(loading = true, error = null)
                try {
                    val routes = apiService.getStationRoutes(station.code).let { list ->
                        if (station.transport_type == TransportType.BUS.type) list
                        else list.filter { it.line_code == lineCode }
                    }
                    _routesState.value = RoutesUiState(routes = routes, loading = false)
                } catch (e: Exception) {
                    _routesState.value = RoutesUiState(routes = emptyList(), loading = false, error = e.message)
                }
                delay(20_000L)
            }
        }
    }

    fun fetchConnections() {
        viewModelScope.launch {
            _connectionsState.value = _connectionsState.value.copy(loading = true, error = null)
            try {
                val connections = apiService.getStationConnections(station.code).let { list ->
                    list.filter { it.transport_type == station.transport_type }
                }
                _connectionsState.value = ConnectionsUiState(connections = connections, loading = false, connectionStation = null)
            } catch (e: Exception) {
                _connectionsState.value = ConnectionsUiState(connections = emptyList(), loading = false, error = e.message,  connectionStation = null)
            }
        }
    }

    fun fetchConnectionStation(connectionLineCode: String) {
        viewModelScope.launch {
            _connectionStationState.value = _connectionStationState.value.copy(loading = true, error = null)
            try {
                val connectionStation = apiService.getStations().let { list ->
                    list.filter { it.name == station.name && it.line_code == connectionLineCode }
                }
                _connectionStationState.value = ConnectionStationUiState(connectionStation = connectionStation.first(), loading = false)
            } catch (e: Exception) {
                _connectionStationState.value = ConnectionStationUiState(loading = false, error = e.message, connectionStation = null)
            }
        }
    }
}

