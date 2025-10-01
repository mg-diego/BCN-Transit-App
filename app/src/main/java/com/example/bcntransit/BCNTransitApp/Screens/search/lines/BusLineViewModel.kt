package com.example.bcntransit.screens.search

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bcntransit.R
import com.example.bcntransit.api.ApiService
import com.example.bcntransit.model.transport.LineDto
import com.example.bcntransit.util.toApiError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BusLinesViewModel(val apiService: ApiService) : ViewModel() {

    private val _lines = MutableStateFlow<List<LineDto>>(emptyList())
    val lines: StateFlow<List<LineDto>> = _lines

    private val _loadingLines = MutableStateFlow(true)
    val loadingLines: StateFlow<Boolean> = _loadingLines

    private val _errorLines = MutableStateFlow<String?>(null)
    val errorLines: StateFlow<String?> = _errorLines

    val expandedStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    init {
        fetchLines()
    }

    private fun fetchLines() {
        viewModelScope.launch {
            _loadingLines.value = true
            _errorLines.value = null
            try {
                val fetchedLines = apiService.getLines()
                _lines.value = fetchedLines
                _loadingLines.value = false
                initializeExpandedStates(fetchedLines)
            } catch (e: Exception) {
                val apiError = e.toApiError()
                _errorLines.value = "(${apiError.code}) ${apiError.userMessage}"
                _loadingLines.value = false
            }
        }
    }

    private fun initializeExpandedStates(lines: List<LineDto>) {
        val grouped = lines.groupBy { mapToCustomCategory(it) }
        val initialStates = grouped.keys.associateWith { false }
        expandedStates.value = initialStates
    }

    fun toggleCategory(category: String) {
        val current = expandedStates.value.toMutableMap()
        current[category] = !(current[category] ?: false)
        expandedStates.value = current
    }

    fun mapToCustomCategory(line: LineDto): String {
        val cat = line.category?.trim().orEmpty()
        return when {
            cat == "Diagonals" -> "D"
            cat == "Horitzontals" -> "H"
            cat == "Verticals" -> "V"
            cat == "Llançadores" -> "M"
            cat == "XPRESBus" -> "X"
            else -> {
                val number = Regex("""\d+""").find(line.name)?.value?.toIntOrNull()
                when (number) {
                    in 1..60 -> "1-60"
                    in 61..100 -> "61-100"
                    in 101..120 -> "101-120"
                    in 121..140 -> "121-140"
                    in 141..200 -> "141-200"
                    else -> "Sin categoría"
                }
            }
        }
    }

    fun mapLineToDrawableId(line: LineDto, context: Context): Int {
        val name = "${line.transport_type}_${line.name.lowercase().replace(" ", "_")}"
        return context.resources.getIdentifier(name, "drawable", context.packageName).takeIf { it != 0 }
            ?: R.drawable.bus
    }
}
