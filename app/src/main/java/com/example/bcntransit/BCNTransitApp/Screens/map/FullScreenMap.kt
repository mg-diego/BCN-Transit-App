package com.example.bcntransit.BCNTransitApp.Screens.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.bcntransit.screens.map.rememberMapView
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.Style
import androidx.compose.material3.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.bcntransit.screens.map.getDrawableIdByTransportType
import com.example.bcntransit.screens.map.getMarkerIcon
import com.example.bcntransit.screens.map.getMarkerSize
import org.maplibre.android.maps.MapView

@Composable
fun FullScreenMap(
    transportType: String,
    latitude: Double,
    longitude: Double,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    // CAMBIO: Crear instancia única sin depender de función externa
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

    var showDirectionsPopup by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                // CAMBIO: Mover lógica a update
                view.getMapAsync { map ->
                    map.setStyle(
                        Style.Builder().fromUri("https://basemaps.cartocdn.com/gl/voyager-gl-style/style.json")
                    ) { style ->
                        // CAMBIO: usar map.clear() en lugar de annotations.clear()
                        map.clear()

                        val prevDrawable = getDrawableIdByTransportType(context, transportType)
                        val stationLatLng = LatLng(latitude, longitude)
                        val marker = map.addMarker(
                            MarkerOptions()
                                .position(stationLatLng)
                                .setIcon(getMarkerIcon(context, prevDrawable, sizePx = getMarkerSize(transportType) * 2))
                        )

                        // Click listener del marker
                        map.setOnMarkerClickListener { clickedMarker ->
                            if (clickedMarker == marker) {
                                showDirectionsPopup = true
                                true
                            } else false
                        }

                        val cameraPosition = CameraPosition.Builder()
                            .target(stationLatLng)
                            .zoom(16.0)
                            .build()
                        map.cameraPosition = cameraPosition

                        map.uiSettings.apply {
                            isScrollGesturesEnabled = true
                            isZoomGesturesEnabled = true
                            isRotateGesturesEnabled = false
                            isTiltGesturesEnabled = false
                        }
                    }
                }
            }
        )

        // Botón para cerrar mapa
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(Color.White.copy(alpha = 0.8f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Cerrar mapa",
            )
        }

        // Popup de "Cómo llegar"
        if (showDirectionsPopup) {
            AlertDialog(
                onDismissRequest = { showDirectionsPopup = false },
                containerColor = Color(0xFFFAFAFA),
                titleContentColor = Color.Black,
                textContentColor = Color.DarkGray,
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 8.dp,
                modifier = Modifier.padding(8.dp),
                title = { Text("Cómo llegar", style = MaterialTheme.typography.titleMedium) },
                text = { Text("Abrir Google Maps con la ruta hacia la estación.") },
                confirmButton = {
                    TextButton(onClick = {
                        val uri = "https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude"
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                            data = android.net.Uri.parse(uri)
                            setPackage("com.google.android.apps.maps")
                        }
                        context.startActivity(intent)
                        showDirectionsPopup = false
                    }) {
                        Text("Ir")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDirectionsPopup = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}