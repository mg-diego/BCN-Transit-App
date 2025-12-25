package com.bcntransit.app.screens.search

import AlertsContent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import com.bcntransit.app.BCNTransitApp.components.SegmentedSelector
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bcntransit.app.BCNTransitApp.components.InlineErrorBanner
import com.bcntransit.app.BCNTransitApp.screens.map.StationsMap
import com.bcntransit.app.R
import com.bcntransit.app.api.ApiService
import com.bcntransit.app.model.transport.StationDto
import com.bcntransit.app.data.enums.TransportType
import com.bcntransit.app.util.getAndroidId
import com.example.bcntransit.BCNTransitApp.components.CustomTopBar

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationListScreen(
    lineCode: String,
    transportType: TransportType,
    apiService: ApiService,
    onStationClick: (StationDto) -> Unit,
    onBackClick: () -> Unit
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
    val currentUserId = getAndroidId(LocalContext.current)
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Lista", "Mapa")

    var showAlertsSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    uiState.line?.let { line ->
        val parsedColor = Color(android.graphics.Color.parseColor(if (line.color.startsWith("#")) line.color else "#${line.color}"))
        val context = LocalContext.current
        val drawableName = "${line.transport_type}_${line.name.lowercase().replace(" ", "_")}"
        val drawableId = remember(line.name) {
            context.resources.getIdentifier(drawableName, "drawable", context.packageName)
        }

        Scaffold(
            topBar = {
                CustomTopBar(
                    onBackClick = onBackClick,
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
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
                    },
                    actions = {
                        val hasAlerts = line.has_alerts && line.alerts.isNotEmpty()

                        IconButton(onClick = { showAlertsSheet = true }) {
                            if (hasAlerts) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = colorResource(R.color.medium_red),
                                            contentColor = Color.White
                                        ) {
                                            Text("${line.alerts.size}")
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = "Ver incidencias",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            } else {
                                // Si no hay alertas, mostramos el icono más sutil
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = "Información del servicio",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
            ) {
                // AÑADIR DOS TABS: LISTA y MAPA
                // el SegmentedSelector se ve en ambas tabs, el itemsIndexed por ahora solo en LISTA, en MAPA deja un placeholder
                Column {
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
                                onSelectedOption = { viewModel.selectDirection(it) },
                                lineColor = parsedColor
                            )
                        }
                    }

                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { Text(title) }
                            )
                        }
                    }

                    when {
                        uiState.loading -> Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator(color = colorResource(R.color.medium_red)) }

                        uiState.error != null -> InlineErrorBanner(uiState.error!!)

                        else -> when (selectedTab) {
                            0 ->
                                LazyColumn(
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

                                    item {
                                        Text(
                                            text = "Paradas",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                    }
                                    itemsIndexed(showStations) { index, station ->
                                        StationItem(
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

                            1 -> Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val showStations = if (transportType == TransportType.BUS) {
                                    if (uiState.direction.startsWith(line.origin)) uiState.stations.filter { it.DESTI_SENTIT == line.destination }
                                    else uiState.stations.filter { it.DESTI_SENTIT == line.origin }
                                } else {
                                    if (uiState.direction.startsWith(line.origin)) uiState.stations
                                    else uiState.stations.reversed()
                                }

                                StationsMap(
                                    stations = showStations,
                                    lineColor = parsedColor,
                                    modifier = Modifier.fillMaxSize(),
                                    onStationClick = { onStationClick(it) }
                                )
                            }
                        }

                    }
                }
            }
        }

        // NUEVO: Implementación del BottomSheet de Incidencias
        if (showAlertsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAlertsSheet = false },
                sheetState = sheetState
            ) {
                AlertsContent(
                    lineName = line.name,
                    alerts = line.alerts
                )
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


