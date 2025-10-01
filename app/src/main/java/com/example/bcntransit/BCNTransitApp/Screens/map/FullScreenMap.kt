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
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.bcntransit.model.transport.AccessDto
import com.example.bcntransit.screens.map.addMarker
import com.example.bcntransit.screens.map.configureMapStyle
import com.example.bcntransit.screens.map.rememberMapViewWithLifecycle

@Composable
fun FullScreenMap(
    transportType: String,
    latitude: Double,
    longitude: Double,
    accesses: List<AccessDto>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val mapView = rememberMapViewWithLifecycle(context)
    var showDirectionsPopup by remember { mutableStateOf(false) }
    var selectedMarkerLatitude by remember { mutableStateOf(0.0) }
    var selectedMarkerLongitude by remember { mutableStateOf(0.0) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                view.getMapAsync { map ->
                    configureMapStyle(
                        context = context,
                        map = map,
                        enableLocationComponent = false,
                        scrollEnabled = true,
                        zoomEnabled = true
                    ) {
                        map.clear()
                        val accessesMarkers = mutableListOf<org.maplibre.android.annotations.Marker>()

                        for (access in accesses) {
                            val marker = addMarker(
                                context = context,
                                map = map,
                                iconName = if (access.number_of_elevators > 0) "elevator" else "stairs",
                                latitude = access.latitude,
                                longitude = access.longitude,
                                markerSizeMultiplier = 1.5f,
                                zoom = 16.0
                            )
                            marker.let { accessesMarkers.add(it) }
                        }

                        val marker = addMarker(
                            context = context,
                            map = map,
                            iconName = transportType,
                            latitude = latitude,
                            longitude = longitude,
                            markerSizeMultiplier = 2f,
                            zoom = 16.0
                        )

                        map.setOnMarkerClickListener { clickedMarker ->
                            if (clickedMarker == marker || clickedMarker in accessesMarkers) {
                                showDirectionsPopup = true
                                selectedMarkerLatitude = clickedMarker.position.latitude
                                selectedMarkerLongitude = clickedMarker.position.longitude
                                true
                            } else false
                        }
                    }
                }
            }
        )

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
                text = { Text("¿Abrir Google Maps con la ruta?") },
                confirmButton = {
                    TextButton(onClick = {
                        val uri = "https://www.google.com/maps/dir/?api=1&destination=$selectedMarkerLatitude,$selectedMarkerLongitude"
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