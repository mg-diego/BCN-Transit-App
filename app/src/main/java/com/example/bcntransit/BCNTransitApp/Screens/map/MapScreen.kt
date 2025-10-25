package com.example.bcntransit.screens.map

import SearchTopBar
import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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

import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.plugins.annotation.SymbolOptions
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.ContextCompat

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
    val selectedFilters by viewModel.selectedFilters.collectAsState()
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
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

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

    LaunchedEffect(nearbyStations, selectedFilters, viewModel.selectedExtraFilters) {
        val extra = viewModel.selectedExtraFilters

        mapView.getMapAsync { map ->
            map.getStyle { style ->
                // Inicializa SymbolManager si aún no existe
                if (viewModel.symbolManager == null) {
                    viewModel.symbolManager = SymbolManager(mapView, map, style).apply {
                        iconAllowOverlap = true
                        textAllowOverlap = true
                    }
                }

                val symbolManager = viewModel.symbolManager!!

                // --- Eliminar markers/symbols que ya no cumplen filtros ---
                viewModel.markerMap.keys
                    .filter { id ->
                        val station = nearbyStations.firstOrNull { it.station_code == id }
                        station == null || run {
                            val matchesType = selectedFilters.any { filter ->
                                filter.equals(TransportType.from(station.type).name, ignoreCase = true)
                            }

                            val matchesBicing = if (station.type == TransportType.BICING.type) {
                                val slotsOk = !extra.onlySlotsAvailable || (station.slots ?: 0) > 0
                                val bikesOk = when {
                                    extra.onlyElectricalBikesAvailable && extra.onlyMechanicalBikesAvailable ->
                                        (station.electrical ?: 0) > 0 || (station.mechanical ?: 0) > 0
                                    extra.onlyElectricalBikesAvailable -> (station.electrical ?: 0) > 0
                                    extra.onlyMechanicalBikesAvailable -> (station.mechanical ?: 0) > 0
                                    else -> true
                                }
                                slotsOk && bikesOk
                            } else true

                            !(matchesType && matchesBicing)
                        }
                    }
                    .forEach { id ->
                        viewModel.markerMap[id]?.remove()
                        viewModel.markerMap.remove(id)

                        // Eliminar symbol de Bicing si existe
                        viewModel.symbolMap[id]?.let { symbol ->
                            symbolManager.delete(symbol)
                            viewModel.symbolMap.remove(id)
                        }
                    }

                viewModel.symbolMap.keys.toList() // Copia la lista para evitar ConcurrentModificationException
                    .filter { id ->
                        val station = nearbyStations.firstOrNull { it.station_code == id }
                        station == null || run {
                            val matchesType = selectedFilters.any { filter ->
                                filter.equals(TransportType.BICING.name, ignoreCase = true)
                            }

                            val matchesBicing = if (station.type == TransportType.BICING.type) {
                                val slotsOk = !extra.onlySlotsAvailable || (station.slots ?: 0) > 0
                                val bikesOk = when {
                                    extra.onlyElectricalBikesAvailable && extra.onlyMechanicalBikesAvailable ->
                                        (station.electrical ?: 0) > 0 || (station.mechanical ?: 0) > 0
                                    extra.onlyElectricalBikesAvailable -> (station.electrical ?: 0) > 0
                                    extra.onlyMechanicalBikesAvailable -> (station.mechanical ?: 0) > 0
                                    else -> true
                                }
                                slotsOk && bikesOk
                            } else true

                            !(matchesType && matchesBicing)
                        }
                    }
                    .forEach { id ->
                        viewModel.symbolMap[id]?.let { symbol ->
                            symbolManager.delete(symbol)
                            viewModel.symbolMap.remove(id)
                        }
                    }

                // --- Añadir/actualizar markers/symbols que cumplen filtros ---
                nearbyStations
                    .filter { station ->
                        val matchesType = selectedFilters.any { filter ->
                            filter.equals(TransportType.from(station.type).name, ignoreCase = true)
                        }

                        val matchesBicing = if (station.type == TransportType.BICING.type) {
                            val slotsOk = !extra.onlySlotsAvailable || (station.slots ?: 0) > 0
                            val bikesOk = when {
                                extra.onlyElectricalBikesAvailable && extra.onlyMechanicalBikesAvailable ->
                                    (station.electrical ?: 0) > 0 || (station.mechanical ?: 0) > 0
                                extra.onlyElectricalBikesAvailable -> (station.electrical ?: 0) > 0
                                extra.onlyMechanicalBikesAvailable -> (station.mechanical ?: 0) > 0
                                else -> true
                            }
                            slotsOk && bikesOk
                        } else true

                        matchesType && matchesBicing
                    }
                    .forEach { station ->
                        val drawableId = getDrawableIdByName(context, station.type)
                        val sizePx = getMarkerSize(station.type)

                        if (station.type == TransportType.BICING.type) {
                            // === BICING: Usar SymbolLayer ===
                            val existing = viewModel.symbolMap[station.station_code]

                            // Calcular los valores SIEMPRE
                            val elec = station.electrical ?: 0
                            val mech = station.mechanical ?: 0
                            val slots = station.slots ?: 0
                            val displayText = "Slots: $slots\nB. Eléctricas: $elec \nB. Mecánicas: $mech"

                            if (existing == null) {
                                // Añadir icono al style si no existe
                                val iconName = "bicing-icon"
                                if (style.getImage(iconName) == null) {
                                    val bitmap = getBitmapFromDrawable(context, drawableId, sizePx)
                                    style.addImage(iconName, bitmap)
                                }

                                // Crear symbol con texto permanente
                                val symbolOptions = SymbolOptions()
                                    .withLatLng(LatLng(station.coordinates[0], station.coordinates[1]))
                                    .withIconImage(iconName)
                                    .withIconSize(1.0f)
                                    .withTextField(displayText)
                                    .withTextOffset(arrayOf(0f, 2.2f))
                                    .withTextSize(10f)
                                    .withTextColor("#333333")
                                    .withTextHaloColor("#FFFFFF")
                                    .withTextHaloWidth(2f)
                                    .withTextAnchor("top")

                                val symbol = symbolManager.create(symbolOptions)
                                viewModel.symbolMap[station.station_code] = symbol
                            } else {
                                // IMPORTANTE: Actualizar con los 3 valores
                                existing.textField = displayText
                                symbolManager.update(existing)
                            }

                            // Click listener para symbols de Bicing
                            symbolManager.addClickListener { symbol ->
                                val stationCode = viewModel.symbolMap.entries
                                    .firstOrNull { it.value == symbol }?.key

                                stationCode?.let { code ->
                                    val stationInfo = nearbyStations.firstOrNull {
                                        it.station_code == code
                                    }
                                    stationInfo?.let {
                                        viewModel.selectNearbyStation(
                                            it,
                                            ApiClient.from(TransportType.BICING)
                                        )

                                        // Animar cámara
                                        val cameraPosition = CameraPosition.Builder()
                                            .target(LatLng(it.coordinates[0], it.coordinates[1]))
                                            .zoom(16.0)
                                            .build()

                                        map.animateCamera(
                                            CameraUpdateFactory.newCameraPosition(cameraPosition),
                                            1000,
                                            null
                                        )

                                        scope.launch { sheetState.show() }
                                    }
                                }
                                true
                            }

                        } else {
                            // === OTROS TRANSPORTES: Usar Markers normales ===
                            val existing = viewModel.markerMap[station.station_code]
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
                                existing.setIcon(normalIcon)
                            }
                        }
                    }
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            FilterPanel(
                onClose = {
                    scope.launch {
                        drawerState.close()
                    }
                },
                viewModel = viewModel
            )
        },
        gesturesEnabled = false
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())

            Column {
                Row {
                    SearchTopBar(
                        initialQuery = "",
                        onSearch = onViewStation,
                        enabled = !isLoadingNearbyStations
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                val iconMap = mapOf(
                    "Metro" to R.drawable.metro,
                    "Bus" to R.drawable.bus,
                    "Tram" to R.drawable.tram,
                    "Rodalies" to R.drawable.rodalies,
                    "FGC" to R.drawable.fgc,
                    "Bicing" to R.drawable.bicing
                )

                if (!isLoadingNearbyStations) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("Metro", "Bus", "Tram", "Rodalies", "FGC", "Bicing").forEach { filter ->
                            FilterChip(
                                selected = selectedFilters.contains(filter),
                                enabled = !isLoadingNearbyStations,
                                onClick = {
                                    val newSet = selectedFilters.toMutableSet()
                                    if (newSet.contains(filter)) newSet.remove(filter) else newSet.add(filter)
                                    viewModel.setFilters(newSet)
                                },
                                label = {},
                                leadingIcon = {
                                    val isSelected = selectedFilters.contains(filter)
                                    Icon(
                                        painter = painterResource(id = iconMap[filter] ?: R.drawable.bcn_transit),
                                        contentDescription = null,
                                        tint = Color.Unspecified,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .alpha(if (isSelected) 1f else 0.4f)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (selectedFilters.contains(filter))
                                        MaterialTheme.colorScheme.primary   // borde visible solo si está seleccionado
                                    else
                                        Color.Transparent,                  // sin borde si no lo está
                                    selectedBorderColor = MaterialTheme.colorScheme.primary,
                                    disabledBorderColor = Color.Transparent,
                                    borderWidth = 1.dp,
                                    enabled = true,
                                    selected = selectedFilters.contains(filter)
                                ),
                                modifier = Modifier.size(34.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                    }
                }
            }

            if (isLoadingNearbyStations) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colorResource(R.color.medium_red))
                }
            }

            userLocation?.let { location ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.End
                    ) {
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
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Centrar en ubicación"
                            )
                        }

                        FloatingActionButton(
                            onClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            },
                            shape = CircleShape,
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filtros adicionales"
                            )
                        }
                    }
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterPanel(
    onClose: () -> Unit,
    viewModel: MapViewModel
) {
    val currentDistance = viewModel.selectedDistance.toFloat()
    val currentOnlyElectricalBikes = viewModel.onlyElectricalBikesAvailable
    val currentOnlyMechanical = viewModel.onlyMechanicalBikesAvailable
    val currentOnlySlots = viewModel.onlySlotsAvailable

    var localDistance by remember { mutableStateOf(currentDistance) }
    var localOnlyElectricalBikes by remember { mutableStateOf(currentOnlyElectricalBikes) }
    var localOnlyMechanicalBikes by remember { mutableStateOf(currentOnlyMechanical) }
    var localOnlySlots by remember { mutableStateOf(currentOnlySlots) }

    val hasChanges by remember(
        localDistance, localOnlyElectricalBikes, localOnlyMechanicalBikes, localOnlySlots,
        currentDistance, currentOnlyElectricalBikes, currentOnlyMechanical, currentOnlySlots
    ) {
        derivedStateOf {
            localDistance != currentDistance ||
                    localOnlyElectricalBikes != currentOnlyElectricalBikes ||
                    localOnlyMechanicalBikes != currentOnlyMechanical ||
                    localOnlySlots != currentOnlySlots
        }
    }

    ModalDrawerSheet {
        Column(modifier = Modifier.fillMaxHeight()) {

            // --- Header ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Filtros extra",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = {
                    // Reiniciar valores locales al cerrar sin aplicar
                    localDistance = currentDistance
                    localOnlyElectricalBikes = currentOnlyElectricalBikes
                    localOnlyMechanicalBikes = currentOnlyMechanical
                    localOnlySlots = currentOnlySlots
                    onClose()
                }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar drawer"
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))

            DistanceSlider(
                selectedDistance = localDistance,
                onDistanceChange = { localDistance = it }
            )

            BicingAvailabilityFilters(
                onlyElectricalBikesAvailable = localOnlyElectricalBikes,
                onlyMechanicalBikesAvailable = localOnlyMechanicalBikes,
                onOnlyElectricalBikesChange = { localOnlyElectricalBikes = it },
                onOnlyMechanicalBikesChange = { localOnlyMechanicalBikes = it },
                onlySlotsAvailable = localOnlySlots,
                onOnlySlotsAvailableChange = { localOnlySlots = it }
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    enabled = hasChanges,
                    onClick = {
                        val distanceChanged = localDistance.toDouble() != viewModel.selectedDistance
                        viewModel.updateDistance(localDistance.toDouble())
                        viewModel.updateOnlyElectricalBikesAvailable(localOnlyElectricalBikes)
                        viewModel.updateOnlyMechanicalBikesAvailable(localOnlyMechanicalBikes)
                        viewModel.updateOnlySlotsAvailable(localOnlySlots)

                        if (distanceChanged) {
                            viewModel.updateNearbyStations()
                        }

                        onClose()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasChanges)
                            colorResource(R.color.medium_red)
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (hasChanges)
                            Color.White
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Text("Aplicar")
                }
            }
        }
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BicingAvailabilityFilters(
    onlyElectricalBikesAvailable: Boolean,
    onlyMechanicalBikesAvailable: Boolean,
    onOnlyElectricalBikesChange: (Boolean) -> Unit,
    onOnlyMechanicalBikesChange: (Boolean) -> Unit,
    onlySlotsAvailable: Boolean,
    onOnlySlotsAvailableChange: (Boolean) -> Unit
) {
    var localSlots by remember { mutableStateOf(onlySlotsAvailable) }

    // Estado del tipo de bici: "any", "electrical", "mechanical", "none"
    var localBikeFilter by remember {
        mutableStateOf(
            when {
                onlyElectricalBikesAvailable && !onlyMechanicalBikesAvailable -> "electrical"
                !onlyElectricalBikesAvailable && onlyMechanicalBikesAvailable -> "mechanical"
                onlyElectricalBikesAvailable && onlyMechanicalBikesAvailable -> "any"
                else -> "none"
            }
        )
    }

    // Sincronizar cambios externos
    LaunchedEffect(onlySlotsAvailable, onlyElectricalBikesAvailable, onlyMechanicalBikesAvailable) {
        localSlots = onlySlotsAvailable
        localBikeFilter = when {
            onlyElectricalBikesAvailable && !onlyMechanicalBikesAvailable -> "electrical"
            !onlyElectricalBikesAvailable && onlyMechanicalBikesAvailable -> "mechanical"
            onlyElectricalBikesAvailable && onlyMechanicalBikesAvailable -> "any"
            else -> "none"
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Bicing",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // --- Solo slots libres ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Solo paradas con slots libres", modifier = Modifier.weight(1f))
            Switch(
                checked = localSlots,
                onCheckedChange = {
                    localSlots = it
                    onOnlySlotsAvailableChange(it)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colorResource(R.color.medium_red),
                    checkedTrackColor = colorResource(R.color.medium_red).copy(alpha = 0.5f)
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- Solo bicis disponibles ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Solo paradas con bicis disponibles", modifier = Modifier.weight(1f))
            Switch(
                checked = localBikeFilter != "none",
                onCheckedChange = { checked ->
                    if (!checked) {
                        localBikeFilter = "none"
                        onOnlyElectricalBikesChange(false)
                        onOnlyMechanicalBikesChange(false)
                    } else {
                        localBikeFilter = "any"
                        onOnlyElectricalBikesChange(true)
                        onOnlyMechanicalBikesChange(true)
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colorResource(R.color.medium_red),
                    checkedTrackColor = colorResource(R.color.medium_red).copy(alpha = 0.5f)
                )
            )
        }

        // --- RadioGroup: tipo de bici ---
        if (localBikeFilter != "none") {
            Spacer(modifier = Modifier.height(8.dp))
            val options = listOf(
                "any" to "Cualquier tipo",
                "electrical" to "Con eléctricas",
                "mechanical" to "Con mecánicas"
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp) // espacio uniforme entre filas
            ) {
                options.forEach { (value, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth() // ocupa ancho pero no fuerza separación
                    ) {
                        RadioButton(
                            selected = localBikeFilter == value,
                            onClick = {
                                localBikeFilter = value
                                onOnlyElectricalBikesChange(value == "any" || value == "electrical")
                                onOnlyMechanicalBikesChange(value == "any" || value == "mechanical")
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = colorResource(R.color.medium_red)
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(label)
                    }
                }
            }
        }
    }
}


@Composable
fun DistanceSlider(
    selectedDistance: Float = 0.5f,
    onDistanceChange: (Float) -> Unit
) {
    val steps = 1
    var sliderPosition by remember { mutableStateOf(selectedDistance) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Resultados",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(text = "Distancia máxima desde ubicación actual:", style = MaterialTheme.typography.bodyMedium)

        Slider(
            value = sliderPosition,
            onValueChange = { sliderPosition = it },
            onValueChangeFinished = { onDistanceChange(sliderPosition) },
            valueRange = 0.5f..1.5f,
            steps = steps,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = colorResource(R.color.medium_red).copy(alpha = 1f),      // color del circulito
                activeTrackColor = colorResource(R.color.medium_red), // color de la parte activa de la barra
                inactiveTrackColor = colorResource(R.color.gray).copy(alpha = 0.3f) // parte inactiva
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("0.5 km")
            Text("1 km")
            Text("1.5 km")
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
                    }

                } else {
                    selectedNearbyStation.let { st ->
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

fun getBitmapFromDrawable(context: Context, drawableId: Int, sizePx: Int): Bitmap {
    val drawable = ContextCompat.getDrawable(context, drawableId)!!
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, sizePx, sizePx)
    drawable.draw(canvas)
    return bitmap
}
