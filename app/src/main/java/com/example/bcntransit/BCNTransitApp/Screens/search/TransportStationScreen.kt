package com.example.bcntransit.screens.search

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bcntransit.model.LineDto
import com.example.bcntransit.model.RouteDto
import com.example.bcntransit.model.StationDto
import kotlinx.coroutines.delay

@Composable
fun <T : StationDto> TransportStationScreen(
    selectedLine: LineDto?,
    selectedStation: StationDto?,
    loadLines: suspend () -> List<LineDto>,
    loadStationsByLine: suspend (String) -> List<T>,
    loadStationRoutes: suspend (String) -> List<RouteDto>,
    onLineSelected: (LineDto) -> Unit,
    onStationSelected: (StationDto?) -> Unit,
    loadingFavorite: Boolean = false,
    currentUserId: String
) {
    var lines by remember { mutableStateOf<List<LineDto>>(emptyList()) }
    var loadingLines by remember { mutableStateOf(true) }

    var stations by remember { mutableStateOf<List<T>>(emptyList()) }
    var loadingStations by remember { mutableStateOf(false) }

    var stationRoutes by remember { mutableStateOf<List<RouteDto>>(emptyList()) }
    var loadingRoutes by remember { mutableStateOf(false) }

    var errorStations by remember { mutableStateOf<String?>(null) }
    var errorRoutes by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {

        // ======== CARGA DESDE FAVORITOS ========
        if (loadingFavorite) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // ======== CAPA DE RUTAS A PANTALLA COMPLETA ========
        else if (selectedStation != null) {
            // Refresco automático cada 20 segundos
            LaunchedEffect(selectedStation) {
                while (true) {
                    loadingRoutes = true
                    errorRoutes = null
                    try {
                        stationRoutes = loadStationRoutes(selectedStation.code)
                            .filter { route -> route.line_code == selectedLine?.code }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        errorRoutes = e.message
                    } finally {
                        loadingRoutes = false
                    }
                    delay(20_000L)
                }
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 4.dp
            ) {
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
                        routes = stationRoutes,
                        loading = loadingRoutes,
                        error = errorRoutes
                    )
                }
            }
        }

        // ======== LISTADO DE LÍNEAS O ESTACIONES ========
        else if (selectedLine == null) {
            LaunchedEffect(Unit) {
                loadingLines = true
                try {
                    lines = loadLines()
                } catch (e: Exception) {
                    errorStations = e.message
                } finally {
                    loadingLines = false
                }
            }

            if (loadingLines) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LineListScreen(
                    lines,
                    loading = false,
                    error = errorStations,
                    onLineClick = onLineSelected
                )
            }
        } else {
            // Cargar estaciones de la línea seleccionada
            LaunchedEffect(selectedLine) {
                loadingStations = true
                errorStations = null
                try {
                    stations = loadStationsByLine(selectedLine.code)
                } catch (e: Exception) {
                    e.printStackTrace()
                    errorStations = e.message
                } finally {
                    loadingStations = false
                }
            }

            StationListScreen(
                selectedLine,
                stations,
                loadingStations,
                errorStations,
                currentUserId = currentUserId,
                onStationClick = { st -> onStationSelected(st) }
            )
        }
    }
}
