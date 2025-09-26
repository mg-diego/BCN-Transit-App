package com.example.bcntransit.BCNTransitApp.Screens.search.routes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bcntransit.R
import com.example.bcntransit.model.RouteDto
import kotlinx.coroutines.delay
import remainingTime
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun RouteCard(route: RouteDto, isLoading: Boolean) {
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val drawableName = "${route.line_type}_${route.line_name.lowercase().replace(" ", "_")}"
                    val drawableId = remember(route.line_name) {
                        context.resources.getIdentifier(drawableName, "drawable", context.packageName).takeIf { it != 0 } ?: R.drawable.bus
                    }
                    Icon(painter = painterResource(drawableId), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(38.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Dirección", style = MaterialTheme.typography.labelSmall)
                        Text(if(drawableId == R.drawable.bus) "${route.line_name} - ${route.destination}" else route.destination,
                            style = MaterialTheme.typography.titleLarge)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isLoading) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        CircularProgressIndicator(color = colorResource(R.color.medium_red))
                    }
                }
                else if (route.next_trips.isEmpty()) {
                    Text("Sin próximos viajes")
                } else {
                    route.next_trips.take(5).forEachIndexed { index, trip ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(
                                modifier = Modifier.size(24.dp).background(color = MaterialTheme.colorScheme.secondary, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) { Text((index+1).toString(), style = MaterialTheme.typography.bodyMedium, color = Color.White) }

                            Spacer(modifier = Modifier.width(12.dp))
                            ArrivalCountdown(arrivalEpochSeconds = trip.arrival_time, index)
                            Spacer(modifier = Modifier.width(12.dp))

                            Text(buildString {
                                if (trip.delay_in_minutes != 0) {
                                    val symbol = if(trip.delay_in_minutes > 0) "+" else ""
                                    append(" (${symbol}${trip.delay_in_minutes} min)")
                                }
                            }, style = MaterialTheme.typography.bodyLarge,
                                color = if (trip.delay_in_minutes > 0) colorResource(R.color.medium_red) else colorResource(R.color.dark_green),
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.width(12.dp))
                            Text(buildString { if (!trip.platform.isNullOrEmpty()) append("Vía: ${trip.platform}") },
                                style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
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
