package com.example.bcntransit.screens.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bcntransit.BCNTransitApp.Screens.search.lines.BusLineCard
import com.example.bcntransit.R
import com.example.bcntransit.api.ApiClient
import com.example.bcntransit.api.ApiService
import com.example.bcntransit.model.LineDto
import com.example.bcntransit.model.StationDto

@Composable
fun BusLinesScreen(
    selectedLine: LineDto?,
    selectedStation: StationDto?,
    onLineSelected: (LineDto) -> Unit,
    onStationSelected: (StationDto?) -> Unit,
    currentUserId: String
) {

    val viewModel: BusLinesViewModel = viewModel(
        factory = BusLinesViewModelFactory(ApiClient.busApiService)
    )
    val lines by viewModel.lines.collectAsState()
    val loadingLines by viewModel.loadingLines.collectAsState()
    val expandedStates by viewModel.expandedStates.collectAsState()
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {

        // ===== LISTADO DE LÍNEAS =====
        if (selectedLine == null) {
            if (loadingLines) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colorResource(R.color.medium_red))
                }
            } else {
                val groupedByCategory = lines.groupBy { viewModel.mapToCustomCategory(it) }

                Column(modifier = Modifier.fillMaxSize()) {
                    // CABECERA FIJA
                    HeaderBusLines()

                    // LISTADO SCROLLABLE
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        groupedByCategory.forEach { (category, linesInCategory) ->
                            item {
                                CategoryCollapsable(
                                    category = category,
                                    isExpanded = expandedStates[category] == true,
                                    onToggle = { viewModel.toggleCategory(category) }
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        linesInCategory.forEach { line ->
                                            BusLineCard(
                                                line = line,
                                                onClick = { onLineSelected(line) },
                                                drawableId = viewModel.mapLineToDrawableId(line, context)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ===== LISTADO DE ESTACIONES =====
        else if (selectedLine != null && selectedStation == null) {
            StationListScreen(
                line = selectedLine,
                apiService = viewModel.apiService,
                onStationClick = { st -> onStationSelected(st) },
                currentUserId = currentUserId
            )
        }

        // ===== RUTAS DE LA ESTACIÓN SELECCIONADA =====
        if (selectedStation != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Barra superior con botón de volver
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { onStationSelected(null) }) {
                        Text("← Volver")
                    }
                }

                RoutesScreen(
                    station = selectedStation,
                    lineCode = selectedLine?.code ?: "",
                    apiService = viewModel.apiService,
                    onStationSelected = {},
                    onLineSelected = {}
                )
            }
        }
    }
}

@Composable
fun HeaderBusLines() {
    Box(Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(8.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.bus),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(42.dp)
            )
            Spacer(Modifier.width(16.dp))
            Text("Líneas", style = MaterialTheme.typography.titleLarge)
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(6.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
fun CategoryCollapsable(
    category: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        onClick = onToggle
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                    content()
                }
            }
        }
    }
}

class BusLinesViewModelFactory(
    private val apiService: ApiService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BusLinesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BusLinesViewModel(apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}