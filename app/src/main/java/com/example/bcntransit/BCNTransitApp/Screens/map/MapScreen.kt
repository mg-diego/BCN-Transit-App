package com.example.bcntransit.screens.map

import SearchTopBar
import android.content.Context
import android.location.Location
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.bcntransit.R
import com.example.bcntransit.api.ApiClient
import com.example.bcntransit.api.ApiService
import com.example.bcntransit.model.BicingStation
import com.example.bcntransit.model.LineDto
import com.example.bcntransit.model.NearbyStation
import com.example.bcntransit.model.StationDto
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    context: Context,
    onStationSelected: (StationDto, String) -> Unit,
    onLineSelected: (LineDto) -> Unit
) {

    val mapView = rememberMapView(context)
    val appContext = LocalContext.current
    val scope = rememberCoroutineScope()

    var searchText: String by remember { mutableStateOf("") }

    // Estado para ubicación y estaciones
    var lastLocation by remember { mutableStateOf<Location?>(null) }
    var cachedStations by remember { mutableStateOf<List<NearbyStation>>(emptyList()) }

    // Estado para hoja inferior
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var selectedNearbyStation by remember { mutableStateOf<NearbyStation?>(null) }
    var selectedStation by remember { mutableStateOf<StationDto?>(null) }
    var selectedStationConnections by remember { mutableStateOf<List<LineDto>?>(null) }
    var selectedBicingStation by remember { mutableStateOf<BicingStation?>(null) }

    // Estado de marcadores
    val markerToStation = remember { mutableStateMapOf<Marker, NearbyStation>() }
    var lastSelectedMarker by remember { mutableStateOf<Marker?>(null) }

    var isLoadingNearbyStations by remember { mutableStateOf(false) }
    var isLoadingConnections by remember { mutableStateOf(false) }

    /**
     * Cuando cambia la estación seleccionada, pedimos detalles
     */
    LaunchedEffect(selectedNearbyStation) {
        selectedNearbyStation?.let { nearby ->
            isLoadingConnections = true
            var apiService: ApiService? = null
            try {
                when (selectedNearbyStation!!.type) {
                    "bus" -> { apiService = ApiClient.busApiService }
                    "metro" -> { apiService = ApiClient.metroApiService }
                    "rodalies" -> { apiService = ApiClient.rodaliesApiService }
                    "fgc" -> { apiService = ApiClient.fgcApiService }
                    "tram" -> {  apiService = ApiClient.tramApiService }
                    "bicing" -> { selectedBicingStation = ApiClient.bicingApiService.getBicingStation(nearby.station_code) }
                }

                selectedStation = apiService?.getStationByCode(nearby.station_code)
                selectedStationConnections = apiService?.getStationConnections(nearby.station_code)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoadingConnections = false
            }
        }
    }

    /**
     * Configuración inicial del mapa y carga de estaciones cercanas
     */
    var userLocation by remember { mutableStateOf<LatLng?>(null) }

    LaunchedEffect(Unit) {
        // Lanzamos un bucle para actualizar la ubicación periódicamente
        while (true) {
            val newLocation = getUserLocation(appContext)
            if (newLocation != null) {
                userLocation = newLocation
            }
            delay(5000) // cada 5 segundos, ajusta según necesidad
        }
    }

    // Efecto que se dispara cada vez que la ubicación cambia
    LaunchedEffect(userLocation) {
        val latLng = userLocation ?: return@LaunchedEffect

        val currentLoc = Location("current").apply {
            latitude = latLng.latitude
            longitude = latLng.longitude
        }

        val needUpdate = lastLocation?.let { old ->
            val results = FloatArray(1)
            Location.distanceBetween(
                old.latitude, old.longitude,
                currentLoc.latitude, currentLoc.longitude,
                results
            )
            results[0] > 100f
        } ?: true


        if (needUpdate || cachedStations.isEmpty()) {
            if (selectedNearbyStation == null) {
                mapView.getMapAsync { map ->
                    // Cámara inicial o animada
                    map.cameraPosition = CameraPosition.Builder()
                        .target(latLng)
                        .zoom(16.0)
                        .build()
                }
            }

            isLoadingNearbyStations = true
            cachedStations = getNearbyStations(latLng.latitude, latLng.longitude, 0.5)
            isLoadingNearbyStations = false
            lastLocation = currentLoc

            // Pintar marcadores
            mapView.getMapAsync { map ->
                map.clear()
                markerToStation.clear()

                cachedStations.forEach { station ->
                    val coords = LatLng(station.coordinates[0], station.coordinates[1])
                    val drawableId = appContext.resources.getIdentifier(
                        station.type.lowercase(),
                        "drawable",
                        appContext.packageName
                    )
                    val sizePx = markerSize(station.type)
                    val marker = map.addMarker(
                        MarkerOptions()
                            .position(coords)
                            .title(station.station_name)
                            .icon(getMarkerIcon(appContext, drawableId, sizePx = sizePx))
                    )
                    markerToStation[marker] = station
                }

                map.setOnMarkerClickListener { marker ->
                    markerToStation[marker]?.let { st ->
                        lastSelectedMarker?.let { previous ->
                            markerToStation[previous]?.let { prevStation ->
                                val prevSize = markerSize(prevStation.type)
                                val prevDrawable = appContext.resources.getIdentifier(
                                    prevStation.type.lowercase(),
                                    "drawable",
                                    appContext.packageName
                                )
                                previous.setIcon(getMarkerIcon(appContext, prevDrawable, sizePx = prevSize))
                            }
                        }

                        selectedNearbyStation = st
                        val baseSize = markerSize(st.type)
                        val selectedDrawable = appContext.resources.getIdentifier(
                            st.type.lowercase(),
                            "drawable",
                            appContext.packageName
                        )
                        marker.setIcon(getMarkerIcon(appContext, selectedDrawable, sizePx = baseSize * 2))
                        lastSelectedMarker = marker

                        val cameraPosition = CameraPosition.Builder()
                            .target(marker.position.withOffset(-0.0005 ))
                            .zoom(17.0)
                            .build()
                        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1000, null)

                        scope.launch { sheetState.show() }
                        true
                    } ?: false
                }
            }
        }
    }

    // UI principal
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
        SearchTopBar(
            initialQuery = searchText,
            onSearch = { val it = 2 },
            enabled = !isLoadingNearbyStations
        )

        if (isLoadingNearbyStations) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally){
                    CircularProgressIndicator(color = colorResource(R.color.medium_red))
                }
            }
        }
    }

    // Hoja inferior con datos
    selectedNearbyStation?.let {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = {
                scope.launch { sheetState.hide() }
                selectedNearbyStation = null
                selectedStation = null
                selectedBicingStation = null
                // Restaurar icono original
                lastSelectedMarker?.let { previous ->
                    markerToStation[previous]?.let { prevStation ->
                        val size = markerSize(prevStation.type)
                        val drawableId = appContext.resources.getIdentifier(
                            prevStation.type.lowercase(),
                            "drawable",
                            appContext.packageName
                        )
                        previous.setIcon(getMarkerIcon(appContext, drawableId, sizePx = size))
                    }
                }
            },
            scrimColor = Color.Transparent,
            modifier = Modifier.fillMaxWidth().fillMaxHeight()
        ) {
            BottomSheetContent(
                selectedNearbyStation!!,
                selectedStation,
                selectedBicingStation,
                selectedStationConnections,
                onStationSelected,
                onLineSelected,
                isLoadingConnections
            )
        }
    }
}

/** Tamaño del icono según tipo */
private fun markerSize(type: String): Int =
    when (type.lowercase()) {
        "metro" -> 100
        "bus" -> 40
        "bicing" -> 60
        "tram" -> 80
        "rodalies" -> 80
        else -> 50
    }

@Composable
private fun BottomSheetContent(
    selectedNearbyStation: NearbyStation,
    selectedStation: StationDto?,
    selectedBicingStation: BicingStation?,
    selectedStationConnections: List<LineDto>?,
    onStationSelected: (StationDto, String) -> Unit,
    onLineSelected: (LineDto) -> Unit,
    isLoadingConnections: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())   // ✅
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val context = LocalContext.current
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
                    Text(text = selectedNearbyStation.station_name, style = MaterialTheme.typography.headlineMedium)
                    Text(
                        text = "(${selectedNearbyStation.station_code})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))

            if (isLoadingConnections) {
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
                if (selectedNearbyStation.type == "bicing") {
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

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.4f)
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Disponibilidad: ProgressCircular
                            val availability = selectedBicingStation?.disponibilidad ?: 0
                            val availabilityPercent = (availability / 100f).coerceIn(0f, 1f)

                            // Detalles de bicis y slots
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Bicis eléctricas: ${selectedBicingStation?.electrical_bikes}", style = MaterialTheme.typography.bodyMedium)
                                Text("Bicis mecánicas: ${selectedBicingStation?.mechanical_bikes}", style = MaterialTheme.typography.bodyMedium)
                                Text("Slots libres: ${selectedBicingStation?.slots}", style = MaterialTheme.typography.bodyMedium)
                            }

                            Column{
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    CircularProgressIndicator(
                                        progress = availabilityPercent,
                                        strokeWidth = 12.dp,
                                        modifier = Modifier.size(100.dp),
                                        color = when {
                                            availability > 70 -> Color(0xFF4CAF50) // verde
                                            availability > 30 -> Color(0xFFFFC107) // amarillo
                                            else -> Color(0xFFF44336) // rojo
                                        }
                                    )
                                    Text(
                                        text = "${availability}%",
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }
                            }
                        }
                    }

                } else {
                    selectedNearbyStation?.let { st ->
                        selectedStation?.let { station ->
                            val lineas = selectedStationConnections?.filter { it.transport_type.equals(st.type, ignoreCase = true) }
                            val conexiones = selectedStationConnections?.filter { !it.transport_type.equals(st.type, ignoreCase = true) }

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
                                                "${connection.transport_type}_${connection.name.lowercase().replace(" ", "_")}"
                                            val drawableId = remember(connection.name) {
                                                context.resources.getIdentifier(drawableName, "drawable", context.packageName)
                                                    .takeIf { it != 0 } ?: context.resources.getIdentifier(connection.transport_type, "drawable", context.packageName)
                                            }

                                            TextButton(
                                                onClick = { onStationSelected(selectedStation, connection.code ) }
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
                                }

                                // --- Conexiones agrupadas por transport_type ---
                                if (!conexiones.isNullOrEmpty()) {
                                    Text(
                                        text = "Conexiones:",
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    // Agrupar por tipo de transporte
                                    val grouped = conexiones.groupBy { it.transport_type.uppercase() }

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
                                                    "${connection.transport_type}_${connection.name.lowercase().replace(" ", "_")}"
                                                val drawableId = remember(connection.name) {
                                                    context.resources.getIdentifier(drawableName, "drawable", context.packageName)
                                                        .takeIf { it != 0 } ?: context.resources.getIdentifier(connection.transport_type, "drawable", context.packageName)
                                                }

                                                TextButton(
                                                    onClick = { onLineSelected(connection) }
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



@Composable
fun rememberMapView(context: Context): MapView {
    MapLibre.getInstance(context)
    val mapView = remember {
        MapView(context).apply {
            onCreate(null)
            getMapAsync { map ->
                map.setStyle(
                    Style.Builder().fromUri(
                        "https://basemaps.cartocdn.com/gl/positron-gl-style/style.json"
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
    }

    DisposableEffect(mapView) {
        mapView.onStart()
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    return mapView
}
