package com.example.bcntransit.screens.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.bcntransit.R
import com.example.bcntransit.data.enums.CustomColors
import com.example.bcntransit.model.LineDto
import com.example.bcntransit.model.RouteDto
import com.example.bcntransit.model.StationDto

@Composable
fun <T : StationDto> BusLinesScreen(
    selectedLine: LineDto?,
    selectedStation: T?,
    loadLines: suspend () -> List<LineDto>,
    loadStationsByLine: suspend (String) -> List<T>,
    loadStationRoutes: suspend (String) -> List<RouteDto>,
    onLineSelected: (LineDto) -> Unit,
    onStationSelected: (StationDto?) -> Unit,
    loadingFavorite: Boolean = false
) {
    if (loadingFavorite) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }

    var lines by remember { mutableStateOf<List<LineDto>>(emptyList()) }
    var loadingLines by remember { mutableStateOf(true) }

    var stations by remember { mutableStateOf<List<T>>(emptyList()) }
    var stationRoutes by remember { mutableStateOf<List<RouteDto>>(emptyList()) }
    var loadingStations by remember { mutableStateOf(false) }
    var loadingRoutes by remember { mutableStateOf(false) }
    var errorStations by remember { mutableStateOf<String?>(null) }
    var errorRoutes by remember { mutableStateOf<String?>(null) }

    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }

    fun mapToCustomCategory(line: LineDto): String {
        val cat = line.category?.trim().orEmpty()
        return when {
            cat == "Diagonals"    -> "D"
            cat == "Horitzontals" -> "H"
            cat == "Verticals"    -> "V"
            cat == "Llançadores"  -> "M"
            cat == "XPRESBus"     -> "X"
            else -> {
                val number = Regex("""\d+""").find(line.name)?.value?.toIntOrNull()
                when (number) {
                    in 1..60   -> "1-60"
                    in 61..100 -> "61-100"
                    in 101..120-> "101-120"
                    in 121..140-> "121-140"
                    in 141..200-> "141-200"
                    else       -> "Sin categoría"
                }
            }
        }
    }

    val groupedByCategory: Map<String, List<LineDto>> = remember(lines) {
        lines.groupBy { mapToCustomCategory(it) }
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
                loadingLines = true
                errorStations = null
                try { lines = loadLines() }
                catch (e: Exception) { errorStations = e.message }
                finally { loadingLines = false }
            }

            if (loadingLines) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // CABECERA FIJA
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
                                    onToggle = { expandedStates[category] = !(expandedStates[category] ?: false) }
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        linesInCategory.forEach { line ->
                                            BusLineCard(line = line, onClick = { onLineSelected(line) })
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
            LaunchedEffect(selectedLine) {
                loadingStations = true
                errorStations = null
                try { stations = loadStationsByLine(selectedLine.code) }
                catch (e: Exception) { errorStations = e.message }
                finally { loadingStations = false }
            }

            StationListScreen(
                line = selectedLine,
                stations = stations,
                loading = loadingStations,
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
                    try { stationRoutes = loadStationRoutes(selectedStation.code) }
                    catch (e: Exception) { errorRoutes = e.message }
                    finally { loadingRoutes = false }
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
            .padding(vertical = 1.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        onClick = onToggle
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
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

@Composable
private fun BusLineCard(line: LineDto, onClick: () -> Unit) {
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
            val drawableName = "${line.transport_type}_${line.name.lowercase().replace(" ", "_")}"
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

            val alertText = if (line.has_alerts) "Incidencias" else "Servicio normal"
            val alertColor = if (line.has_alerts) CustomColors.RED.color else CustomColors.DARK_GREEN.color

            Column {
                Text(line.description, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(alertColor, shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(alertText, style = MaterialTheme.typography.bodyMedium, color = CustomColors.GRAY.color)
                }
            }
        }
    }
}
