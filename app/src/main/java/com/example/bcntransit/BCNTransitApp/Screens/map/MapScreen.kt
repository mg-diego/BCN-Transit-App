package com.example.bcntransit.screens.map

import android.content.Context
import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.bcntransit.model.NearbyStation
import com.google.android.gms.maps.model.BitmapDescriptor
import kotlinx.coroutines.launch
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.maps.MapLibreMap
import com.example.bcntransit.R
import com.example.bcntransit.data.enums.CustomColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(context: Context) {
    val (mapView, mapboxMap) = rememberMapView(context)
    val scope = rememberCoroutineScope()
    val appContext = LocalContext.current

    var searchText by remember { mutableStateOf(TextFieldValue("")) }
    val focusManager = LocalFocusManager.current

    // Última posición y estaciones cacheadas
    var lastLocation by remember { mutableStateOf<Location?>(null) }
    var cachedStations by remember { mutableStateOf<List<NearbyStation>>(emptyList()) }

    // Hoja inferior y estación seleccionada
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedStation by remember { mutableStateOf<NearbyStation?>(null) }

    // Relación Marker -> Station
    val markerToStation = remember { mutableStateMapOf<Marker, NearbyStation>() }
    var lastSelectedMarker by remember { mutableStateOf<Marker?>(null) }

    LaunchedEffect(mapView) {
        scope.launch {
            val userLocation = getUserLocation(appContext)
            userLocation?.let { latLng ->
                // Cámara inicial
                mapView.getMapAsync { map ->
                    map.cameraPosition = CameraPosition.Builder()
                        .target(latLng)
                        .zoom(15.0)
                        .build()
                }

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
                    cachedStations = getNearbyStations(
                        latLng.latitude,
                        latLng.longitude,
                        0.5
                    )
                    lastLocation = currentLoc
                }

                // Pintar marcadores y registrar clicks
                mapView.getMapAsync { map ->
                    // Limpia previos
                    map.clear()
                    markerToStation.clear()

                    cachedStations.forEach { station ->
                        val coords = org.maplibre.android.geometry.LatLng(
                            station.coordinates[0],
                            station.coordinates[1]
                        )
                        val drawableId = appContext.resources.getIdentifier(
                            station.type.lowercase(),
                            "drawable",
                            appContext.packageName
                        )

                        val sizePx = when (station.type.lowercase()) { "metro" -> 100 "bus" -> 40 "bicing" -> 60 "tram" -> 80 "rodalies" -> 80 else -> 50 }
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
                            // 1️⃣ Restaurar icono del marcador previamente seleccionado
                            lastSelectedMarker?.let { previous ->
                                markerToStation[previous]?.let { prevStation ->
                                    val prevSize = when (prevStation.type.lowercase()) {
                                        "metro" -> 100
                                        "bus" -> 40
                                        "bicing" -> 60
                                        "tram" -> 80
                                        "rodalies" -> 80
                                        else -> 50
                                    }
                                    val prevDrawableId = appContext.resources.getIdentifier(
                                        prevStation.type.lowercase(),
                                        "drawable",
                                        appContext.packageName
                                    )
                                    previous.setIcon(
                                        getMarkerIcon(appContext, prevDrawableId, sizePx = prevSize)
                                    )
                                }
                            }

                            selectedStation = st
                            val sizePx = when (st.type.lowercase()) { "metro" -> 100 "bus" -> 40 "bicing" -> 60 "tram" -> 80 "rodalies" -> 80 else -> 50 }
                            val selectedDrawableId = appContext.resources.getIdentifier(
                                st.type.lowercase(),
                                "drawable",
                                appContext.packageName
                            )
                            val highlightedIcon = getMarkerIcon(
                                context,
                                R.drawable.bus_selected, // este drawable tiene el trazo
                                sizePx = sizePx * 2
                            )
                            marker.setIcon(
                                getMarkerIcon(appContext, selectedDrawableId, sizePx = sizePx * 2)
                            )
                            lastSelectedMarker = marker

                            // Animar cámara al marcador
                            val cameraPosition = CameraPosition.Builder()
                                .target(marker.position)
                                .zoom(17.0)
                                .build()
                            map.animateCamera(
                                CameraUpdateFactory.newCameraPosition(cameraPosition),
                                1000,
                                null
                            )

                            scope.launch { sheetState.show() }
                            true
                        } ?: false
                    }
                }
            }
        }
    }

    // UI principal
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())

        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text("Buscar estación o dirección") },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .background(Color.White.copy(alpha = 0.9f), shape = MaterialTheme.shapes.medium),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )
    }

    // BottomSheet con los datos de la estación
    if (selectedStation != null) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = {
                scope.launch { sheetState.hide() }
                selectedStation = null
                lastSelectedMarker?.let { previous ->
                    markerToStation[previous]?.let { prevStation ->
                        val prevSize = when (prevStation.type.lowercase()) {
                            "metro" -> 100
                            "bus" -> 40
                            "bicing" -> 60
                            "tram" -> 80
                            "rodalies" -> 80
                            else -> 50
                        }
                        val prevDrawableId = appContext.resources.getIdentifier(
                            prevStation.type.lowercase(),
                            "drawable",
                            appContext.packageName
                        )
                        previous.setIcon(
                            getMarkerIcon(appContext, prevDrawableId, sizePx = prevSize)
                        )
                    }
                }
            },
            scrimColor = Color.Transparent,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.4f) // ✅ media pantalla
                    .padding(16.dp)
            ) {
                selectedStation?.let { st ->
                    val drawableName = st.type
                    val drawableId = remember(st.line_name) {
                        context.resources.getIdentifier(
                            drawableName,
                            "drawable",
                            context.packageName
                        )
                            .takeIf { it != 0 } ?: R.drawable.bus
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
                                text = st.station_name,
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "(${st.station_code})",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun rememberMapView(context: Context): Pair<MapView, MapLibreMap?> {
    MapLibre.getInstance(context)
    var mapboxMap by remember { mutableStateOf<MapLibreMap?>(null) }
    val mapView = remember {
        MapView(context).apply {
            onCreate(null)
            getMapAsync { map ->
                mapboxMap = map
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

    return Pair(mapView, mapboxMap)
}
