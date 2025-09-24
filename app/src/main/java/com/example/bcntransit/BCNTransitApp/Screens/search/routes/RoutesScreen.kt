package com.example.bcntransit.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bcntransit.BCNTransitApp.Screens.search.routes.RouteCard
import com.example.bcntransit.R
import com.example.bcntransit.api.ApiService
import com.example.bcntransit.model.LineDto
import com.example.bcntransit.model.StationDto
import kotlinx.coroutines.delay
import remainingTime
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.Unit

@Composable
fun RoutesScreen(
    station: StationDto,
    lineCode: String,
    apiService: ApiService,
    onStationSelected: (StationDto) -> Unit,
    onLineSelected: (LineDto) -> Unit
) {
    val viewModel: RoutesViewModel = viewModel(
        key = "${station.code}-${lineCode}-${apiService.hashCode()}",
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return RoutesViewModel(apiService, station, lineCode) as T
            }
        }
    )

    val routesState by viewModel.routesState.collectAsState()
    val connectionsState by viewModel.connectionsState.collectAsState()
    val connectionStationUiState by viewModel.connectionStationState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    /*
    LaunchedEffect(connectionStationUiState.connectionStation) {
        connectionStationUiState.connectionStation?.let { station ->
            onStationSelected(station)
        }
    }*/

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .verticalScroll(scrollState)
    ) {
        val drawableName = station.transport_type
        val drawableId = remember(station.line_name) {
            context.resources.getIdentifier(drawableName, "drawable", context.packageName)
                .takeIf { it != 0 } ?: R.drawable.bus
        }

        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(drawableId),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(50.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = station.name,
                    style = MaterialTheme.typography.headlineMedium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "(${station.code})  ·  ",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val alertText = if (station.has_alerts) "Incidencias" else "Servicio normal"
                    val alertColor = if (station.has_alerts) colorResource(R.color.red) else colorResource(R.color.dark_green)
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(alertColor, shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(alertText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Alerts
        if (station.has_alerts && station.alerts.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                station.alerts.forEach { alert ->
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


        Row { Text("Llegadas", style = MaterialTheme.typography.labelSmall) }
        // Estado de carga / error / empty
        when {
            routesState.loading && routesState.routes.isEmpty() -> {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator(color = colorResource(R.color.medium_red))
                }
            }
            routesState.error != null -> {
                Text("Error cargando rutas: ${routesState.error}", color = MaterialTheme.colorScheme.error)
            }
            routesState.routes.isEmpty() -> {
                Text("No hay rutas disponibles.")
            }
            else -> {
                routesState.routes.forEach { route ->
                    RouteCard(route = route, isLoading = routesState.loading)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row { Text("Líneas", style = MaterialTheme.typography.labelSmall) }
        when {
            connectionsState.connections.isEmpty() -> {
                Text("No hay conexiones disponibles.")
            }
            connectionsState.loading -> {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator(color = colorResource(R.color.medium_red))
                }
            }
            connectionsState.error != null -> {
                Text("Error cargando rutas: ${connectionsState.error}", color = MaterialTheme.colorScheme.error)
            }
            else -> {
                connectionsState.connections.forEach { connection ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val context = LocalContext.current
                        val drawableName =
                            "${connection.transport_type}_${connection.name.lowercase().replace(" ", "_")}"
                        val drawableId = remember(connection.name) {
                            context.resources.getIdentifier(drawableName, "drawable", context.packageName)
                                .takeIf { it != 0 } ?: context.resources.getIdentifier(connection.transport_type, "drawable", context.packageName)
                        }

                        TextButton(
                            onClick = {
                                //viewModel.fetchConnectionStation(connection.code)
                                //onLineSelected(connection)
                            }
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
        }
    }
}

@Composable
fun ArrivalCountdown(arrivalEpochSeconds: Long, index: Int) {
    var remaining by remember { mutableStateOf(remainingTime(arrivalEpochSeconds)) }
    var showExactTime by remember { mutableStateOf(false) }
    var showDate by remember { mutableStateOf(false) }

    LaunchedEffect(arrivalEpochSeconds) {
        while (true) {
            val nowMillis = System.currentTimeMillis()
            val arrivalMillis = arrivalEpochSeconds * 1000

            remaining = remainingTime(arrivalEpochSeconds)
            showExactTime = arrivalMillis - nowMillis > TimeUnit.HOURS.toMillis(1)

            // comprobar si es el mismo día
            val nowCal = Calendar.getInstance()
            val arrivalCal = Calendar.getInstance().apply { timeInMillis = arrivalMillis }
            showDate = nowCal.get(Calendar.YEAR) != arrivalCal.get(Calendar.YEAR) ||
                    nowCal.get(Calendar.DAY_OF_YEAR) != arrivalCal.get(Calendar.DAY_OF_YEAR)

            delay(1000)
        }
    }

    val displayText = if (showExactTime) {
        val pattern = if (showDate) "dd/MM HH:mm" else "HH:mm"
        val formatter = remember(pattern) { SimpleDateFormat(pattern, Locale.getDefault()) }
        formatter.format(Date(arrivalEpochSeconds * 1000)) + "h"
    } else {
        remaining
    }

    Text(
        text = displayText,
        style = if(index == 0) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
        fontStyle = if (!showExactTime) FontStyle.Italic else FontStyle.Normal,
        fontWeight = if (remaining == "Llegando") { FontWeight.Bold } else { FontWeight.Normal }
    )
}
