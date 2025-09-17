package com.example.bcntransit.screens.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

fun hasLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

@Composable
fun rememberMapView(context: Context): MapView {
    // Inicializamos MapLibre
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
                    // Posición inicial en Barcelona
                    map.cameraPosition = CameraPosition.Builder()
                        .target(LatLng(41.3874, 2.1686))
                        .zoom(14.0)
                        .build()

                    // Activar componente de localización si hay permisos
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
