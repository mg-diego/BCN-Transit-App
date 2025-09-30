package com.example.bcntransit.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material3.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bcntransit.BCNTransitApp.Screens.map.FullScreenMap
import com.example.bcntransit.BCNTransitApp.Screens.map.MiniMap
import com.example.bcntransit.BCNTransitApp.Screens.search.routes.RouteCard
import com.example.bcntransit.BCNTransitApp.components.InlineErrorBanner
import com.example.bcntransit.R
import com.example.bcntransit.api.ApiService
import com.example.bcntransit.data.enums.TransportType
import com.example.bcntransit.screens.map.getDrawableIdByTransportType
import kotlinx.coroutines.launch
import kotlin.Unit

@Composable
fun RoutesScreen(
    lineCode: String,
    stationCode: String,
    apiService: ApiService,
    onConnectionClick: (String, String) -> Unit
) {
    val viewModel: RoutesViewModel = viewModel(
        key = "$lineCode-$stationCode-${apiService.hashCode()}",
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return RoutesViewModel(
                    apiService = apiService,
                    lineCode = lineCode,
                    stationCode = stationCode
                ) as T
            }
        }
    )

    val routesState by viewModel.routesState.collectAsState()
    val connectionsState by viewModel.stationConnectionsState.collectAsState()
    val selectedStation by viewModel.selectedStation.collectAsState()
    var showFullMap by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (selectedStation == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = colorResource(R.color.medium_red))
        }
        return
    }

    val drawableId = remember(selectedStation!!.line_name) {
        getDrawableIdByTransportType(context, selectedStation!!.transport_type)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Mostrar mapa en pantalla completa
        if (showFullMap) {
            FullScreenMap(
                transportType = selectedStation!!.transport_type,
                latitude = selectedStation!!.latitude,
                longitude = selectedStation!!.longitude,
                onDismiss = { showFullMap = false }
            )
        }
        else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // HEADER ESTACIÓN
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
                        Icon(
                            painter = painterResource(drawableId),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(50.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = selectedStation!!.name,
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "(${selectedStation!!.code})  ·  ",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                val alertText = if (selectedStation!!.has_alerts) "Incidencias" else "Servicio normal"
                                val alertColor = if (selectedStation!!.has_alerts) colorResource(R.color.red) else colorResource(R.color.dark_green)
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(alertColor, shape = CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    alertText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // ALERTS
                if (selectedStation!!.has_alerts) {
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            selectedStation!!.alerts.forEach { alert ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = colorResource(R.color.medium_red))
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(alert.headerEs, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        if (alert.textEs.isNotEmpty()) {
                                            Text(alert.textEs, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }


                // RUTAS
                item {Row { Text("Llegadas", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp)) }}
                if (routesState.loading && routesState.routes.isEmpty()) {
                    item { CircularProgressIndicator(modifier = Modifier.padding(16.dp), color = colorResource(R.color.medium_red)) }
                } else if (routesState.error != null) {
                    item { InlineErrorBanner(routesState.error!!) }
                } else if (routesState.routes.isEmpty()){
                    item {Text("No hay rutas disponibles.")}
                } else {
                    items(routesState.routes) { route ->
                        RouteCard(route, routesState.loading)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // CONEXIONES
                item { Row { Text("Líneas", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp)) } }
                if (connectionsState.loading) {
                    item { CircularProgressIndicator(modifier = Modifier.padding(16.dp), color = colorResource(R.color.medium_red)) }
                } else if (connectionsState.error != null) {
                    item { InlineErrorBanner(connectionsState.error!!) }
                } else if (connectionsState.connections.isEmpty()) {
                    item { Text("No hay conexiones disponibles.") }
                } else {
                    items(connectionsState.connections) { connection ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val context = LocalContext.current
                            val drawableName =
                                "${connection.transport_type}_${connection.name.lowercase().replace(" ", "_")}"
                            val drawableId = remember(connection.name) {
                                context.resources.getIdentifier(drawableName, "drawable", context.packageName)
                                    .takeIf { it != 0 } ?: getDrawableIdByTransportType(context, connection.transport_type)
                            }

                            TextButton(
                                onClick = {
                                    viewModel.viewModelScope.launch {
                                        val selectedConnection = viewModel.fetchSelectedConnection(connection.code)
                                        selectedConnection?.let {
                                            onConnectionClick(it.code, connection.code)
                                        }
                                    }
                                },
                                enabled = selectedStation!!.transport_type != TransportType.BUS.type
                            ) {
                                Icon(
                                    painter = painterResource(drawableId),
                                    contentDescription = null,
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(42.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    connection.description,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }


                // LOCATION
                item { Row { Text("Ubicación", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 32.dp)) } }
                item {
                    val station = selectedStation!!

                    // Altura fija para el mini-mapa
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        MiniMap(
                            transportType = station.transport_type,
                            latitude = station.latitude,
                            longitude = station.longitude,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(top = 16.dp)
                        )
                        FloatingActionButton(
                            onClick = { showFullMap = true },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.OpenInFull,
                                contentDescription = "Abrir mapa"
                            )
                        }
                    }
                }
            }
        }
    }
}
