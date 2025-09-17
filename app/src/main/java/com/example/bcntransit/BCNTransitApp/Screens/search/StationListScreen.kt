package com.example.bcntransit.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.bcntransit.model.LineDto
import com.example.bcntransit.model.StationDto
import com.example.bcntransit.R

@Composable
fun StationListScreen(
    line: LineDto,
    stations: List<StationDto>,
    loading: Boolean,
    lineColor: String,
    error: String? = null,
    modifier: Modifier = Modifier,
    onStationClick: (StationDto) -> Unit
) {
    val context = LocalContext.current
    val drawableName = "${line.transport_type}_${line.name.lowercase().replace(" ", "_")}"   // ej: "metro_l1"
    val drawableId = remember(line.name) {
        context.resources.getIdentifier(drawableName, "drawable", context.packageName)
    }

    Column(modifier = modifier.padding(16.dp)) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Icon(
                painter = painterResource(drawableId),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(42.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(line.description, style = MaterialTheme.typography.headlineSmall)
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            error != null -> Text("Error: $error", color = Color.Red)

            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(0.dp) // sin huecos para que la línea parezca continua
            ) {
                itemsIndexed(stations) { index, station ->
                    StationRow(
                        station = station,
                        isFirst = index == 0,
                        isLast = index == stations.lastIndex,
                        lineColor = lineColor,
                        onClick = { onStationClick(station) }
                    )
                }
            }
        }
    }
}

