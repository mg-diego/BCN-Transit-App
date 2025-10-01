package com.example.bcntransit.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import com.example.bcntransit.BCNTransitApp.components.SegmentedSelector
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bcntransit.BCNTransitApp.components.InlineErrorBanner
import com.example.bcntransit.R
import com.example.bcntransit.api.ApiService
import com.example.bcntransit.model.transport.StationDto
import com.example.bcntransit.data.enums.TransportType

@Composable
fun StationListScreen(
    lineCode: String,
    transportType: TransportType,
    apiService: ApiService,
    currentUserId: String,
    onStationClick: (StationDto) -> Unit
) {
    val viewModel: StationListViewModel = viewModel(
        key = "$lineCode-${apiService.hashCode()}",
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return StationListViewModel(apiService, lineCode) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()

    uiState.line?.let { line ->
        val parsedColor = Color(android.graphics.Color.parseColor(if (line.color.startsWith("#")) line.color else "#${line.color}"))
        val context = LocalContext.current
        val drawableName = "${line.transport_type}_${line.name.lowercase().replace(" ", "_")}"
        val drawableId = remember(line.name) {
            context.resources.getIdentifier(drawableName, "drawable", context.packageName)
        }

        Column {
            // Cabecera
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
                        painter = painterResource(drawableId),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(42.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(line.description, style = MaterialTheme.typography.titleLarge)
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

            // Toggle de dirección
            if (line.origin.isNotEmpty() && line.destination.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    SegmentedSelector(
                        options = listOf("${line.origin} → ${line.destination}", "${line.destination} → ${line.origin}"),
                        direction = uiState.direction,
                        onOptiondirection = { viewModel.selectDirection(it) },
                        modifier = Modifier.fillMaxWidth(0.9f),
                        lineColor = parsedColor
                    )
                }
            }

            when {
                uiState.loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = colorResource(R.color.medium_red)) }

                uiState.error != null -> InlineErrorBanner(uiState.error!!)

                else -> LazyColumn(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    val showStations = if (transportType == TransportType.BUS) {
                        if (uiState.direction.startsWith(line.origin)) uiState.stations.filter { it.DESTI_SENTIT == line.destination }
                        else uiState.stations.filter { it.DESTI_SENTIT == line.origin }
                    } else {
                        if (uiState.direction.startsWith(line.origin)) uiState.stations
                        else uiState.stations.reversed()
                    }

                    itemsIndexed(showStations) { index, station ->
                        StationRow(
                            station = station,
                            isFirst = index == 0,
                            isLast = index == showStations.lastIndex,
                            lineColor = parsedColor,
                            lineType = line.transport_type,
                            currentUserId = currentUserId,
                            onClick = { onStationClick(station) }
                        )
                    }
                }
            }
        }
    }

    if (uiState.loading && uiState.line == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator(color = colorResource(R.color.medium_red)) }
    }
}


