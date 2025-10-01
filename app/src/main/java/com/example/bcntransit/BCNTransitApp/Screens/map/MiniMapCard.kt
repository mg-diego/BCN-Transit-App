package com.example.bcntransit.BCNTransitApp.Screens.map


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Elevator
import androidx.compose.material.icons.filled.Stairs
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.bcntransit.model.transport.AccessDto
import com.example.bcntransit.screens.map.addMarker
import com.example.bcntransit.screens.map.configureMapStyle
import com.example.bcntransit.screens.map.rememberMapViewWithLifecycle

@Composable
fun MiniMap(
    transportType: String,
    latitude: Double,
    longitude: Double,
    accesses: List<AccessDto>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mapView = rememberMapViewWithLifecycle(context)

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { view ->
            view.getMapAsync { map ->
                configureMapStyle(
                    context = context,
                    map = map,
                    enableLocationComponent = false,
                    scrollEnabled = false,
                    zoomEnabled = false
                ) {
                    map.clear()
                    for (access in accesses) {
                        addMarker(
                            context = context,
                            map = map,
                            iconName = if (access.number_of_elevators > 0) "elevator" else "stairs",
                            latitude = access.latitude,
                            longitude = access.longitude,
                            zoom = 14.0
                        )
                    }
                    addMarker(
                        context = context,
                        map = map,
                        iconName = transportType,
                        latitude = latitude,
                        longitude = longitude,
                        markerSizeMultiplier = 1f,
                        zoom = 14.0
                    )
                }
            }
        }
    )
}