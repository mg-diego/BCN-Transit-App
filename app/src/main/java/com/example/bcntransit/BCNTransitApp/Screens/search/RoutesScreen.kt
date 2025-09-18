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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bcntransit.model.RouteDto
import com.example.bcntransit.model.StationDto
import remainingTime
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun RoutesScreen(
    station: StationDto,
    routes: List<RouteDto>,
    loading: Boolean,
    error: String?
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Encabezado con icono de línea
        if (routes.isNotEmpty()) {
            val drawableName =
                "${routes[0].line_type}_${routes[0].line_name.lowercase().replace(" ", "_")}" // ej: "metro_l1"
            val drawableId = remember(routes[0].line_name) {
                context.resources.getIdentifier(drawableName, "drawable", context.packageName)
            }

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
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Text(
                        text = "Sin incidencias",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        when {
            loading -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Text(
                    text = "Error cargando rutas: $error",
                    color = MaterialTheme.colorScheme.error
                )
            }
            routes.isEmpty() -> {
                Text("No hay rutas disponibles.")
            }
            else -> {
                routes.forEach { route ->
                    Spacer(modifier = Modifier.height(16.dp))

                    // Card con overlay de loader
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                // Destino
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val drawableName =
                                        "${route.line_type}_${route.line_name.lowercase().replace(" ", "_")}"
                                    val drawableId = remember(route.line_name) {
                                        context.resources.getIdentifier(
                                            drawableName,
                                            "drawable",
                                            context.packageName
                                        )
                                    }

                                    Icon(
                                        painter = painterResource(drawableId),
                                        contentDescription = null,
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(30.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = route.destination,
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Próximos viajes
                                if (route.next_trips.isEmpty()) {
                                    Text("Sin próximos viajes")
                                } else {
                                    route.next_trips.take(5).forEachIndexed { index, trip ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp)
                                        ) {
                                            Spacer(modifier = Modifier.width(12.dp))
                                            // Icono numérico del orden
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .background(
                                                        color = MaterialTheme.colorScheme.secondary,
                                                        shape = CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = (index + 1).toString(),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Color.White
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))
                                            // Cuenta atrás
                                            ArrivalCountdown(arrivalEpochSeconds = trip.arrival_time)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            // Info de retraso y andén
                                            Text(
                                                text = buildString {
                                                    if (trip.delay_in_minutes != 0) {
                                                        val symbol = if(trip.delay_in_minutes > 0) "+" else ""
                                                        append(" (${symbol}${trip.delay_in_minutes} min)")
                                                    }
                                                },
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = if (trip.delay_in_minutes > 0) { Color.Red } else { Color.Green },
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = buildString {
                                                    if (!trip.platform.isNullOrEmpty()) {
                                                        append("Vía: ${trip.platform}")
                                                    }
                                                },
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Overlay loader en la card mientras se recarga
                        if (loading) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(Color(0x80FFFFFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
fun ArrivalCountdown(arrivalEpochSeconds: Long) {
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

            kotlinx.coroutines.delay(1000)
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
        style = MaterialTheme.typography.bodyLarge,
        fontStyle = if (!showExactTime) FontStyle.Italic else FontStyle.Normal,
        fontWeight = if (remaining == "Entrando") { FontWeight.Bold } else { FontWeight.Normal }
    )
}
