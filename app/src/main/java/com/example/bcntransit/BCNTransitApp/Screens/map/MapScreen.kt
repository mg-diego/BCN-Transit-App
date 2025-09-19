package com.example.bcntransit.screens.map

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.bcntransit.model.NearbyStation
import kotlinx.coroutines.launch
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.geometry.LatLng

/**
 * Composable principal que muestra el mapa y obtiene estaciones cercanas
 */
@Composable
fun MapScreen(context: Context) {
    val mapView = rememberMapView(context)
    val scope = rememberCoroutineScope()

    LaunchedEffect(mapView) {
        scope.launch {
            val userLocation = getUserLocation(context)
            userLocation?.let { latLng ->
                // Centrar mapa en la ubicación del usuario
                mapView.getMapAsync { map ->
                    map.cameraPosition = CameraPosition.Builder()
                        .target(latLng)
                        .zoom(14.0)
                        .build()
                }

                // Obtener estaciones cercanas desde el API
                val stations = getNearbyStations(latLng.latitude, latLng.longitude)
                mapView.getMapAsync { map ->
                    stations.forEach { station ->
                        val coords = LatLng(station.coordinates[0], station.coordinates[1])
                        val marker = MarkerOptions()
                            .position(coords)
                            .title("${station.type} - ${station.station_name}")
                        map.addMarker(marker)
                    }
                }
            }
        }
    }

    AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
}

/**
 * Inicializa MapLibre y devuelve un MapView listo para usar
 */
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
