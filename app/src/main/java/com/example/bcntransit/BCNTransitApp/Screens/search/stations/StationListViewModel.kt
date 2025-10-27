package com.bcntransit.app.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bcntransit.app.api.ApiService
import com.bcntransit.app.model.transport.LineDto
import com.bcntransit.app.model.transport.StationDto
import com.bcntransit.app.util.toApiError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class StationListUiState(
    val line: LineDto? = null,
    val stations: List<StationDto> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val direction: String = ""
)

class StationListViewModel(
    private val apiService: ApiService,
    private val lineCode: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(StationListUiState(loading = true))
    val uiState: StateFlow<StationListUiState> = _uiState

    init {
        fetchLineAndStations()
    }

    private fun fetchLineAndStations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            try {
                val line = apiService.getLines().first { it.code == lineCode }
                val stations = apiService.getStationsByLine(lineCode)

                // Direccion por defecto
                val defaultDirection = "${line.origin} → ${line.destination}"

                _uiState.value = StationListUiState(
                    line = line,
                    stations = stations,
                    loading = false,
                    direction = defaultDirection
                )
            } catch (e: Exception) {
                val apiError = e.toApiError()
                _uiState.value = StationListUiState(
                    line = null,
                    stations = emptyList(),
                    loading = false,
                    error = "(${apiError.code}) ${apiError.userMessage}"
                )
            }
        }
    }

    fun selectDirection(newDirection: String) {
        _uiState.value = _uiState.value.copy(direction = newDirection)
    }
}
