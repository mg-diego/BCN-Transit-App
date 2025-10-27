package com.bcntransit.app.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bcntransit.app.api.ApiService
import com.bcntransit.app.data.enums.TransportType
import com.bcntransit.app.model.transport.LineDto
import com.bcntransit.app.util.toApiError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LineListUiState(
    val lines: List<LineDto> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null
)

class LineListViewModel(
    private val apiService: ApiService,
    private val transportType: TransportType
) : ViewModel() {

    private val _uiState = MutableStateFlow(LineListUiState())
    val uiState: StateFlow<LineListUiState> = _uiState

    init {
        fetchLines()
    }

    private fun fetchLines() {
        viewModelScope.launch {
            _uiState.value = LineListUiState(loading = true)
            try {
                val lines = apiService.getLines().filter { it.transport_type == transportType.type }
                _uiState.value = LineListUiState(lines = lines, loading = false)
            } catch (e: Exception) {
                val apiError = e.toApiError()
                _uiState.value = LineListUiState(lines = emptyList(), loading = false, error = "(${apiError.code}) ${apiError.userMessage}")
            }
        }
    }
}
