package com.example.bcntransit.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.bcntransit.R
import com.example.bcntransit.model.LineDto
import com.example.bcntransit.model.RouteDto
import com.example.bcntransit.model.StationDto

@Composable
fun <T : StationDto> BusLinesScreen(
    busLines: List<LineDto>,
    selectedLine: LineDto?,
    selectedStation: T?,
    loadLines: suspend () -> Unit,
    loadStationsByLine: suspend (String) -> List<T>,
    loadStationRoutes: suspend (String) -> List<RouteDto>,
    onLineSelected: (LineDto) -> Unit,
    onStationSelected: (StationDto?) -> Unit
) {
    var stations by remember { mutableStateOf<List<T>>(emptyList()) }
    var stationRoutes by remember { mutableStateOf<List<RouteDto>>(emptyList()) }
    var loadingStations by remember { mutableStateOf(false) }
    var loadingRoutes by remember { mutableStateOf(false) }
    var errorStations by remember { mutableStateOf<String?>(null) }
    var errorRoutes by remember { mutableStateOf<String?>(null) }

    // Estados de expansión por categoría
    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }

    // Función para mapear a categoría personalizada
    fun mapToCustomCategory(line: LineDto): String {
        val cat = line.category?.trim().orEmpty()
        when {
            cat == "Diagonals"    -> return "D"
            cat == "Horitzontals" -> return "H"
            cat == "Verticals"    -> return "V"
            cat == "Llançadores"  -> return "M"
            cat == "XPRESBus"     -> return "X"
        }
        val number = Regex("""\d+""").find(line.name)?.value?.toIntOrNull()
        return when (number) {
            in 1..60   -> "1-60"
            in 61..100 -> "61-100"
            in 101..120-> "101-120"
            in 121..140-> "121-140"
            in 141..200-> "141-200"
            else       -> "Sin categoría"
        }
    }

    val groupedByCategory: Map<String, List<LineDto>> = remember(busLines) {
        busLines.groupBy { mapToCustomCategory(it) }
    }

    LaunchedEffect(groupedByCategory.keys) {
        groupedByCategory.keys.forEach { key ->
            if (!expandedStates.containsKey(key)) expandedStates[key] = false
        }
        val toRemove = expandedStates.keys - groupedByCategory.keys
        toRemove.forEach { expandedStates.remove(it) }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ===== LISTADO DE LÍNEAS =====
        if (selectedLine == null) {
            LaunchedEffect(Unit) {
                loadingStations = true
                errorStations = null
                try { loadLines() }
                catch (e: Exception) { errorStations = e.message }
                finally { loadingStations = false }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                groupedByCategory.forEach { (category, linesInCategory) ->
                    CategoryCollapsable(
                        category = category,
                        isExpanded = expandedStates[category] == true,
                        onToggle = { expandedStates[category] = !(expandedStates[category] ?: false) }
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 250.dp)
                                .padding(start = 16.dp, top = 8.dp)
                        ) {
                            items(linesInCategory) { line ->
                                BusLineCard(
                                    line = line,
                                    onClick = { onLineSelected(line) }
                                )
                            }
                        }
                    }
                }
            }
        }
        // ===== LISTADO DE ESTACIONES =====
        else if (selectedLine != null && selectedStation == null) {
            LaunchedEffect(selectedLine) {
                loadingStations = true
                errorStations = null
                try {
                    stations = loadStationsByLine(selectedLine.code)
                } catch (e: Exception) {
                    errorStations = e.message
                } finally {
                    loadingStations = false
                }
            }

            StationListScreen(
                line = selectedLine,
                stations = stations,
                loading = loadingStations,
                lineColor = selectedLine.color,
                error = errorStations,
                onStationClick = { st -> onStationSelected(st) }
            )
        }

        // ===== RUTAS DE LA ESTACIÓN SELECCIONADA =====
        if (selectedStation != null) {
            LaunchedEffect(selectedStation) {
                while (true) {
                    loadingRoutes = true
                    errorRoutes = null
                    try {
                        stationRoutes = loadStationRoutes(selectedStation.code)
                    } catch (e: Exception) {
                        errorRoutes = e.message
                    } finally {
                        loadingRoutes = false
                    }
                    kotlinx.coroutines.delay(20_000L)
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
    }
}


@Composable
private fun CategoryCollapsable(
    category: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        onClick = onToggle
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(text = category, style = MaterialTheme.typography.titleMedium)
            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                content()
            }
        }
    }
}

@Composable
private fun BusLineCard(
    line: LineDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            val context = LocalContext.current
            val drawableName = "${line.transport_type}_${line.name.lowercase().replace(" ", "_")}"   // ej: "metro_l1"
            val drawableId = remember(line.name) {
                context.resources.getIdentifier(drawableName, "drawable", context.packageName)
                    .takeIf { it != 0 } ?: R.drawable.bus
            }

            Icon(
                painter = painterResource(drawableId),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(34.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = line.description, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
