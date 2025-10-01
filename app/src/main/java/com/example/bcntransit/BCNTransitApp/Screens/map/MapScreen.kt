package com.example.bcntransit.screens.map

import SearchTopBar
import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bcntransit.BCNTransitApp.Screens.map.MapViewModel
import com.example.bcntransit.R
import com.example.bcntransit.api.ApiClient
import com.example.bcntransit.data.enums.TransportType
import com.example.bcntransit.model.transport.BicingStationDto
import com.example.bcntransit.model.transport.LineDto
import com.example.bcntransit.model.transport.NearbyStation
import com.example.bcntransit.model.transport.StationDto
import kotlinx.coroutines.launch
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onViewLine: (String, String) -> Unit,
    onViewStation: (String, String, String) -> Unit
) {
    val context = LocalContext.current
    val mapKey = remember { System.currentTimeMillis().toString() }
    val viewModel: MapViewModel = viewModel(
        key = mapKey,
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return MapViewModel(context) as T
            }
        }
    )
    val mapView = rememberMapView(context)
    DisposableEffect(mapView) {
        mapView.onStart()
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    val userLocation by remember { viewModel::userLocation }
    val nearbyStations by remember { viewModel::nearbyStations }
    val selectedNearbyStation by remember { viewModel::selectedNearbyStation }
    val selectedStation by remember { viewModel::selectedStation }
    val selectedStationConnections by remember { viewModel::selectedStationConnections }
    val selectedBicingStation by remember { viewModel::selectedBicingStation }
    val isLoadingNearbyStations by remember { viewModel::isLoadingNearbyStations }
    val isLoadingConnections by remember { viewModel::isLoadingConnections }

    LaunchedEffect(Unit) {
        mapView.getMapAsync { map ->
            map.setOnMarkerClickListener { marker ->
                val stationInfo = viewModel.getStationForMarker(marker)
                if (stationInfo == null) return@setOnMarkerClickListener false

                val station = stationInfo.station

                // --- Restaurar el último marker si era distinto ---
                viewModel.lastSelectedMarker?.let { last ->
                    if (last != marker) {
                        viewModel.getStationForMarker(last)?.let { prev ->
                            last.setIcon(prev.normalIcon)
                        }
                    }
                }

                // --- Seleccionar el nuevo marker y agrandarlo ---
                viewModel.selectNearbyStation(
                    station,
                    ApiClient.from(TransportType.from(station.type))
                )

                // Solo cambiamos el icono, no creamos ni borramos marker
                marker.setIcon(stationInfo.enlargedIcon)
                viewModel.setLastSelectedMarker(marker)

                // --- Animar la cámara ---
                val cameraPosition = CameraPosition.Builder()
                    .target(marker.position.withOffset(-0.001))
                    .zoom(16.0)
                    .build()

                map.animateCamera(
                    CameraUpdateFactory.newCameraPosition(cameraPosition),
                    1000,
                    null
                )

                scope.launch { sheetState.show() }
                true
            }
        }
    }


    LaunchedEffect(viewModel.lastUpdateLocation) {
        mapView.getMapAsync { map ->
            userLocation?.let { latLng ->
                val cameraPosition = CameraPosition.Builder()
                    .target(latLng)
                    .zoom(16.0)
                    .build()
                map.cameraPosition = cameraPosition
            }
        }
    }

    LaunchedEffect(nearbyStations) {
        mapView.getMapAsync { map ->
            // Quitar los que ya no están
            val currentIds = nearbyStations.map { it.station_code }.toSet()
            viewModel.markerMap.keys
                .filter { it !in currentIds }
                .forEach { id ->
                    viewModel.markerMap[id]?.remove()
                    viewModel.markerMap.remove(id)
                }

            // Añadir/actualizar los existentes
            nearbyStations.forEach { station ->
                val existing = viewModel.markerMap[station.station_code]
                val drawableId = getDrawableIdByName(context, station.type)
                val sizePx = getMarkerSize(station.type)
                val normalIcon = getMarkerIcon(context, drawableId, sizePx)

                if (existing == null) {
                    val marker = map.addMarker(
                        MarkerOptions()
                            .position(LatLng(station.coordinates[0], station.coordinates[1]))
                            .icon(normalIcon)
                    )
                    viewModel.registerMarker(
                        station.station_code,
                        marker,
                        station,
                        normalIcon,
                        getMarkerIcon(context, drawableId, (sizePx * 1.6f).roundToInt())
                    )
                } else {
                    // opcional: actualiza icono o posición si cambian
                    existing.setIcon(normalIcon)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())

        SearchTopBar(
            initialQuery = "",
            onSearch = onViewStation,
            enabled = !isLoadingNearbyStations
        )

        if (isLoadingNearbyStations) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colorResource(R.color.medium_red))
            }
        }

        userLocation?.let { location ->
            FloatingActionButton(
                onClick = {
                    mapView.getMapAsync { map ->
                        userLocation?.let { latLng ->
                            val cameraPosition = CameraPosition.Builder()
                                .target(latLng)
                                .zoom(16.0)
                                .build()

                            val durationMs = 1000L

                            map.animateCamera(
                                CameraUpdateFactory.newCameraPosition(cameraPosition),
                                durationMs.toInt(),
                                null
                            )
                        }
                    }
                },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Centrar en ubicación"
                )
            }
        }
    }

    selectedNearbyStation?.let { nearby ->
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = {
                viewModel.lastSelectedMarker?.let { prev ->
                    viewModel.getStationForMarker(prev)?.let { prevStation ->
                        val prevSize = getMarkerSize(prevStation.station.type)
                        val prevDrawable = getDrawableIdByName(context, prevStation.station.type)
                        prev.setIcon(getMarkerIcon(context, prevDrawable, sizePx = prevSize))
                    }
                }
                scope.launch { sheetState.hide() }
                viewModel.clearSelection()
            },
            scrimColor = Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            BottomSheetContent(
                nearby,
                selectedStation,
                selectedBicingStation,
                selectedStationConnections,
                isLoadingConnections,
                onViewLine,
                onViewStation,
                viewModel,
                sheetState
            )
        }
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomSheetContent(
    selectedNearbyStation: NearbyStation,
    selectedStation: StationDto?,
    selectedBicingStation: BicingStationDto?,
    selectedStationConnections: List<LineDto>?,
    isLoadingConnections: Boolean = false,
    onViewLine: (String, String) -> Unit,
    onViewStation: (String, String, String) -> Unit,
    viewModel: MapViewModel,
    sheetState: SheetState
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        var isLoadingSelectedConnection by remember { mutableStateOf(false) }
        val drawableId = remember(selectedNearbyStation.line_name) {
            context.resources.getIdentifier(
                selectedNearbyStation.type,
                "drawable",
                context.packageName
            ).takeIf { it != 0 } ?: R.drawable.bus
        }

        Column {
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
                        text = selectedNearbyStation.station_name,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "(${selectedNearbyStation.station_code})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            if (isLoadingConnections || isLoadingSelectedConnection) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = colorResource(R.color.medium_red))
                }

            } else {
                if (selectedNearbyStation.type == TransportType.BICING.type) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Disponibilidad:",
                            style = MaterialTheme.typography.titleMedium
                        )
                        // Disponibilidad: ProgressCircular
                        val availability = selectedBicingStation?.disponibilidad ?: 0
                        val availabilityPercent = (availability / 100f).coerceIn(0f, 1f)

                        // Detalles de bicis y slots
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "   - Bicis eléctricas: ${selectedBicingStation?.electrical_bikes}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "   - Bicis mecánicas: ${selectedBicingStation?.mechanical_bikes}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "   - Slots libres: ${selectedBicingStation?.slots}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        /*SegmentedProgressBar(
                            firstCategoryStr = "Slots libres",
                            firstCategoryValue = selectedBicingStation?.slots!!,
                            secondCategoryStr = "Bicis Eléctricas",
                            secondCategoryValue = selectedBicingStation.electrical_bikes,
                            thirdCategoryStr = "Bicis Mecánicas",
                            thirdCategoryValue = selectedBicingStation.mechanical_bikes
                        )*/
                    }

                } else {
                    selectedNearbyStation?.let { st ->
                        selectedStation?.let { station ->
                            val lineas = selectedStationConnections?.filter {
                                it.transport_type.equals(
                                    st.type,
                                    ignoreCase = true
                                )
                            }
                            val conexiones = selectedStationConnections?.filter {
                                !it.transport_type.equals(
                                    st.type,
                                    ignoreCase = true
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {

                                // --- Líneas ---
                                val listaFinal: List<LineDto> = if (lineas.isNullOrEmpty()) {
                                    listOf(
                                        LineDto(
                                            id = selectedStation.line_id,
                                            code = selectedStation.line_code,
                                            name = selectedStation.line_name,
                                            description = "",
                                            origin = "",
                                            destination = "",
                                            color = selectedStation.line_color,
                                            transport_type = selectedStation.transport_type,
                                            name_with_emoji = "❔ Sin línea",
                                            has_alerts = false,
                                            alerts = emptyList(),
                                            category = ""
                                        )
                                    )
                                } else {
                                    lineas
                                }

                                if (listaFinal.isNotEmpty()) {
                                    Text(
                                        text = "Líneas:",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    for (connection in listaFinal) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            val context = LocalContext.current
                                            val drawableName =
                                                "${connection.transport_type}_${
                                                    connection.name.lowercase().replace(" ", "_")
                                                }"
                                            val drawableId = remember(connection.name) {
                                                context.resources.getIdentifier(
                                                    drawableName,
                                                    "drawable",
                                                    context.packageName
                                                )
                                                    .takeIf { it != 0 }
                                                    ?: context.resources.getIdentifier(
                                                        connection.transport_type,
                                                        "drawable",
                                                        context.packageName
                                                    )
                                            }

                                            TextButton(
                                                onClick = {
                                                    scope.launch {
                                                        sheetState.expand()
                                                    }
                                                    viewModel.viewModelScope.launch {
                                                        isLoadingSelectedConnection = true
                                                        val apiService = ApiClient.from(TransportType.from(connection.transport_type)                                                        )
                                                        val station =
                                                            viewModel.fetchSelectedConnection(
                                                                selectedStation.name,
                                                                connection.code,
                                                                apiService
                                                            )
                                                        onViewStation(
                                                            connection.transport_type,
                                                            connection.code,
                                                            station?.code ?: ""
                                                        )
                                                        isLoadingSelectedConnection = false
                                                    }
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

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // --- Conexiones agrupadas por transport_type ---
                                    if (!conexiones.isNullOrEmpty()) {
                                        Text(
                                            text = "Conexiones:",
                                            style = MaterialTheme.typography.titleMedium
                                        )

                                        // Agrupar por tipo de transporte
                                        val grouped =
                                            conexiones.groupBy { it.transport_type.uppercase() }

                                        for ((type, group) in grouped) {
                                            Text(
                                                text = type,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            for (connection in group) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    val context = LocalContext.current
                                                    val drawableName =
                                                        "${connection.transport_type}_${
                                                            connection.name.lowercase()
                                                                .replace(" ", "_")
                                                        }"
                                                    val drawableId = remember(connection.name) {
                                                        context.resources.getIdentifier(
                                                            drawableName,
                                                            "drawable",
                                                            context.packageName
                                                        )
                                                            .takeIf { it != 0 }
                                                            ?: context.resources.getIdentifier(
                                                                connection.transport_type,
                                                                "drawable",
                                                                context.packageName
                                                            )
                                                    }

                                                    TextButton(
                                                        onClick = {
                                                            scope.launch {
                                                                sheetState.expand()
                                                            }
                                                            viewModel.viewModelScope.launch {
                                                                isLoadingSelectedConnection = true
                                                                onViewLine(
                                                                    connection.transport_type,
                                                                    connection.code
                                                                )
                                                                isLoadingSelectedConnection = false
                                                            }
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
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun rememberMapView(context: Context): MapView {
    val mapView = remember {
        MapView(context).apply {
            onCreate(null)
        }
    }

    LaunchedEffect(Unit) {
        mapView.getMapAsync { map ->
            map.setStyle(
                Style.Builder().fromUri(
                    "https://basemaps.cartocdn.com/gl/voyager-gl-style/style.json"
                )
            ) { style ->
                if (hasLocationPermission(context)) {
                    val locationComponent = map.locationComponent
                    val options = LocationComponentActivationOptions.builder(context, style)
                        .useDefaultLocationEngine(true)
                        .build()
                    locationComponent.activateLocationComponent(options)
                    locationComponent.isLocationComponentEnabled = true
                    locationComponent.cameraMode = CameraMode.TRACKING
                    locationComponent.renderMode = RenderMode.COMPASS
                }
                map.uiSettings.apply {
                    isScrollGesturesEnabled = true
                    isZoomGesturesEnabled = true
                    isRotateGesturesEnabled = false
                    isTiltGesturesEnabled = false
                }
            }
        }
    }

    return mapView
}
