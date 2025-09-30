package com.example.bcntransit.BCNTransitApp.Screens.map

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.bcntransit.screens.map.getDrawableIdByTransportType
import com.example.bcntransit.screens.map.getMarkerIcon
import com.example.bcntransit.screens.map.getMarkerSize
import com.example.bcntransit.screens.map.hasLocationPermission
import com.example.bcntransit.screens.map.rememberMapView
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

@Composable
fun MiniMap(
    transportType: String,
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            onCreate(null)
        }
    }

    DisposableEffect(Unit) {
        mapView.onStart()
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { view ->
            // CAMBIO: Mover la lógica aquí para que se ejecute después de que el mapa esté listo
            view.getMapAsync { map ->
                map.setStyle(
                    Style.Builder().fromUri("https://basemaps.cartocdn.com/gl/voyager-gl-style/style.json")
                ) { style ->
                    // Limpiar markers antiguos
                    map.clear()

                    // Añadir marker de la estación
                    val prevDrawable = getDrawableIdByTransportType(context, transportType)
                    val stationLatLng = LatLng(latitude, longitude)
                    map.addMarker(
                        MarkerOptions()
                            .position(stationLatLng)
                            .setIcon(getMarkerIcon(context, prevDrawable, sizePx = getMarkerSize(transportType)))
                    )

                    // Centrar cámara en la estación
                    val cameraPosition = CameraPosition.Builder()
                        .target(stationLatLng)
                        .zoom(14.0)
                        .build()
                    map.cameraPosition = cameraPosition

                    map.uiSettings.apply {
                        isScrollGesturesEnabled = false
                        isZoomGesturesEnabled = false
                        isRotateGesturesEnabled = false
                        isTiltGesturesEnabled = false
                    }
                }
            }
        }
    )
}