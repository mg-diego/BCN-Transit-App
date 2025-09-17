package com.example.bcntransit.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bcntransit.model.LineDto
import com.example.bcntransit.model.RouteDto
import com.example.bcntransit.model.StationDto

@Composable
fun <T : StationDto> TransportStationScreen(
    lines: List<LineDto>,
    selectedLine: LineDto?,
    selectedStation: StationDto?,
    loadLines: suspend () -> Unit,
    loadStationsByLine: suspend (String) -> List<T>,
    loadStationRoutes: suspend (String) -> List<RouteDto>,
    onLineSelected: (LineDto) -> Unit,
    onStationSelected: (StationDto?) -> Unit   // ← permite null para cerrar
) {
    var stations by remember { mutableStateOf<List<T>>(emptyList()) }
    var stationRoutes by remember { mutableStateOf<List<RouteDto>>(emptyList()) }
    var loadingStations by remember { mutableStateOf(false) }
    var loadingRoutes by remember { mutableStateOf(false) }
    var errorStations by remember { mutableStateOf<String?>(null) }
    var errorRoutes by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {

        // ===== LISTADO DE LÍNEAS O ESTACIONES =====
        if (selectedLine == null) {
            LaunchedEffect(Unit) {
                loadingStations = true
                errorStations = null
                try { loadLines() }
                catch (e: Exception) { errorStations = e.message }
                finally { loadingStations = false }
            }
            LineListScreen(
                lines,
                loadingStations,
                errorStations,
                onLineClick = onLineSelected
            )
        } else {
            LaunchedEffect(selectedLine) {
                loadingStations = true
                errorStations = null
                try { stations = loadStationsByLine(selectedLine.code) }
                catch (e: Exception) { errorStations = e.message }
                finally { loadingStations = false }
            }

            StationListScreen(
                selectedLine,
                stations,
                loadingStations,
                selectedLine.color,
                errorStations,
                onStationClick = { st -> onStationSelected(st) }
            )
        }

        // ===== CAPA DE RUTAS A PANTALLA COMPLETA =====
        if (selectedStation != null) {
            // Refresco automático cada 10 segundos
            LaunchedEffect(selectedStation) {
                while (true) {
                    loadingRoutes = true
                    errorRoutes = null
                    try {
                        stationRoutes = loadStationRoutes(selectedStation.code)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        errorRoutes = e.message
                    } finally {
                        loadingRoutes = false
                    }
                    kotlinx.coroutines.delay(10_000L) // 10 segundos
                }
            }

            // Pantalla completa que reemplaza el contenido
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

                    // Contenido de rutas
                    RoutesScreen(
                        station = selectedStation,
                        routes = stationRoutes,
                        loading = loadingRoutes,
                        error = errorRoutes
                    )
                }
            }
        }
    }
}
