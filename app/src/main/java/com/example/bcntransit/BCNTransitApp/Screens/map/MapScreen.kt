package com.example.bcntransit.screens.map

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

@Composable
fun MapScreen(context: Context) {
    val mapView = rememberMapView(context)
    val scope = rememberCoroutineScope()
    val appContext = LocalContext.current

    var searchText by remember { mutableStateOf(TextFieldValue("")) }
    val focusManager = LocalFocusManager.current   // 👈 para controlar el foco

    // Carga inicial de mapa y estaciones
    LaunchedEffect(mapView) {
        scope.launch {
            val userLocation = getUserLocation(appContext)
            userLocation?.let { latLng ->
                mapView.getMapAsync { map ->
                    map.cameraPosition = CameraPosition.Builder()
                        .target(latLng)
                        .zoom(14.0)
                        .build()
                }

                val stations = getNearbyStations(latLng.latitude, latLng.longitude, 0.5)
                mapView.getMapAsync { map ->
                    stations.forEach { station ->
                        val coords = org.maplibre.android.geometry.LatLng(
                            station.coordinates[0],
                            station.coordinates[1]
                        )
                        val drawableId = appContext.resources.getIdentifier(
                            station.type.lowercase(),
                            "drawable",
                            appContext.packageName
                        )
                        val sizePx = when (station.type.lowercase()) {
                            "metro" -> 100
                            "bus" -> 40
                            "bicing" -> 60
                            "tram" -> 80
                            "rodalies" -> 80
                            else -> 50
                        }
                        addMarkerWithDrawable(
                            appContext,
                            map,
                            coords,
                            drawableId,
                            station.station_name,
                            sizePx
                        )
                    }
                }
            }
        }
    }

    // Contenedor superpuesto
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
            // Cuando el usuario pulse "Enter"/"Done" en el teclado
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            )
        )
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
                    Style.Builder().fromUri("https://basemaps.cartocdn.com/gl/positron-gl-style/style.json")
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
