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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.bcntransit.BCNTransitApp.components.ArrivalCountdown
import com.example.bcntransit.R
import com.example.bcntransit.model.transport.RouteDto
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
                            Text(buildString { if (!trip.platform.isNullOrEmpty()) append("Vía: ${trip.platform}") },
                                style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                        if (trip.delay_in_minutes != 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 2.dp)
                            ) {
                                Spacer(modifier = Modifier.width(48.dp))

                                val formatter = remember {
                                    SimpleDateFormat("HH:mm", Locale.getDefault())
                                }

                                val realTime = Date(trip.arrival_time * 1000)
                                val plannedTime = Date((trip.arrival_time - trip.delay_in_minutes * 60) * 1000)

                                val plannedStr = formatter.format(plannedTime) + "h"
                                val realStr = formatter.format(realTime) + "h"

                                val delaySign = if (trip.delay_in_minutes > 0) "+" else ""
                                val delayStr = " ($delaySign${trip.delay_in_minutes} min)"

                                val delayColor = if (trip.delay_in_minutes > 0) {
                                    colorResource(R.color.medium_red)
                                } else {
                                    colorResource(R.color.dark_green)
                                }

                                Text(
                                    text = buildAnnotatedString {
                                        // plannedTime tachado
                                        withStyle(
                                            style = SpanStyle(
                                                textDecoration = TextDecoration.LineThrough,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        ) {
                                            append(plannedStr)
                                        }

                                        append(" → ")

                                        // realTime normal
                                        withStyle(
                                            style = SpanStyle(
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                        ) {
                                            append(realStr)
                                        }

                                        // delay en color
                                        withStyle(
                                            style = SpanStyle(
                                                color = delayColor
                                            )
                                        ) {
                                            append(delayStr)
                                        }
                                    },
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}


