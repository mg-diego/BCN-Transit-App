package com.bcntransit.app.screens.search.stations

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bcntransit.app.BCNTransitApp.Screens.search.stations.BicingViewModel
import com.bcntransit.app.BCNTransitApp.components.CategoryCollapsable
import com.bcntransit.app.data.enums.TransportType
import org.maplibre.android.geometry.LatLng
import kotlin.math.*

@SuppressLint("MissingPermission")
@Composable
fun BicingScreen() {
    val viewModel: BicingViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

    val stations by viewModel.stations.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    // Estado para la ubicación del usuario
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var viewMode by remember { mutableStateOf("Lista") }
    val context = LocalContext.current

    // Obtener ubicación una vez al cargar la pantalla
    LaunchedEffect(Unit) {
        val location = viewModel.getUserLocation(context)
        if (location != null) {
            userLocation = location
        }
    }

    // Filtrar las estaciones según el filtro seleccionado
    val filteredStations = remember(stations, selectedFilter) {
        when (selectedFilter) {
            "Slots" -> stations.filter { it.slots > 0 }
            "Eléctricas" -> stations.filter { it.electrical_bikes > 0 }
            "Mecánicas" -> stations.filter { it.mechanical_bikes > 0 }
            else -> stations
        }
    }

    // Agrupar por distancia solo si la ubicación está disponible
    val stationsByDistance = remember(filteredStations, userLocation) {
        userLocation?.let { loc ->
            filteredStations.groupBy { station ->
                val distance = distanceBetween(
                    loc.latitude, loc.longitude,
                    station.latitude, station.longitude
                )
                when {
                    distance < 100 -> "< 100m"
                    distance < 500 -> "< 500m"
                    distance < 1000 -> "< 1km"
                    else -> "> 1km"
                }
            } ?: emptyMap()
        } ?: mapOf("Ubicación no disponible" to filteredStations)
    }

    // Estado de colapsado por categoría
    val expandedStates = remember {
        mutableStateMapOf<String, Boolean>().apply {
            stationsByDistance.keys.forEach { this[it] = true }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row {
            Box(Modifier.fillMaxWidth()) {
                val drawableId = remember(TransportType.BUS) {
                    context.resources.getIdentifier(
                        TransportType.BICING.type,
                        "drawable",
                        context.packageName
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(8.dp)
                ) {
                    Icon(
                        painter = painterResource(drawableId),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(42.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Text("Bicing", style = MaterialTheme.typography.titleLarge)
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
        }

        Row {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row {
                    Text(text = "Filtrar paradas:")
                }
                // Filtros
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Todas", "Slots", "Eléctricas", "Mecánicas").forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { viewModel.setFilter(filter) },
                            label = { Text(filter) }
                        )
                    }
                }

                Row {
                    Text(text = "Filtrar paradas:")
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    FilterChip(
                        selected = viewMode == "Lista",
                        onClick = { viewMode = "Lista" },
                        label = { Text("Lista") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = viewMode == "Mapa",
                        onClick = { viewMode = "Mapa" },
                        label = { Text("Mapa") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Paradas disponibles: ${filteredStations.size}")

                if (viewMode == "Lista") {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        stationsByDistance.toSortedMap(compareBy {
                            when (it) {
                                "< 100m" -> 0
                                "< 500m" -> 1
                                "< 1km" -> 2
                                "> 1km" -> 3
                                else -> 4
                            }
                        }).forEach { (distanceLabel, stations) ->
                            item {
                                CategoryCollapsable(
                                    category = "$distanceLabel (${stations.size})",
                                    isExpanded = expandedStates[distanceLabel] == true,
                                    onToggle = {
                                        expandedStates[distanceLabel] =
                                            !(expandedStates[distanceLabel] ?: true)
                                    }
                                ) {
                                    stations.forEach { station ->
                                        BicingStationItem(station, selectedFilter)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Text("placeholder map")
                }
            }
        }
    }
}

// Función para calcular distancia entre coordenadas (metros)
fun distanceBetween(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371000.0 // radio de la Tierra en metros
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2.0) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c
}
