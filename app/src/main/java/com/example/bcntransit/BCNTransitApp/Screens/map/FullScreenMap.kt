package com.bcntransit.app.BCNTransitApp.Screens.map

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
import com.bcntransit.app.model.transport.AccessDto
import com.bcntransit.app.screens.map.addMarker
import com.bcntransit.app.screens.map.configureMapStyle
import com.bcntransit.app.screens.map.getBitmapFromDrawable
import com.bcntransit.app.screens.map.rememberMapViewWithLifecycle
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.plugins.annotation.Symbol
import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.plugins.annotation.SymbolOptions
import com.bcntransit.app.R
import com.bcntransit.app.screens.map.getDrawableIdByName
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLngBounds

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
                    ) { style ->

                        // Limpiar todo antes de dibujar
                        map.clear()

                        // Crear SymbolManager (para usar texto y símbolos personalizados)
                        val symbolManager = SymbolManager(mapView, map, style).apply {
                            iconAllowOverlap = true
                            textAllowOverlap = false
                        }

                        // Dibujar los accesos
                        val accessesSymbols = mutableListOf<Symbol>()
                        for (access in accesses) {

                            val iconName = if (access.number_of_elevators > 0) "elevator" else "stairs"
                            val drawableId = if (access.number_of_elevators > 0)
                                R.drawable.elevator
                            else
                                R.drawable.stairs

                            // Registrar icono si no existe ya
                            if (style.getImage(iconName) == null) {
                                val bitmap = getBitmapFromDrawable(context, drawableId, 64)
                                style.addImage(iconName, bitmap)
                            }

                            // Crear símbolo con texto debajo
                            val symbolOptions = SymbolOptions()
                                .withLatLng(LatLng(access.latitude, access.longitude))
                                .withIconImage(iconName)
                                .withIconSize(1.2f)
                                .withTextField(access.name ?: "")
                                .withTextOffset(arrayOf(0f, 2.0f)) // texto debajo del icono
                                .withTextSize(10f)
                                .withTextColor("#333333")
                                .withTextHaloColor("#FFFFFF")
                                .withTextHaloWidth(2f)
                                .withTextAnchor("top")

                            val symbol = symbolManager.create(symbolOptions)
                            accessesSymbols.add(symbol)
                        }

                        // Dibujar marcador principal (transporte)
                        val mainIconName = transportType
                        if (style.getImage(mainIconName) == null) {
                            val bitmap = getBitmapFromDrawable(
                                context,
                                getDrawableIdByName(context, transportType),
                                72)
                            style.addImage(mainIconName, bitmap)
                        }

                        val mainSymbol = symbolManager.create(
                            SymbolOptions()
                                .withLatLng(LatLng(latitude, longitude))
                                .withIconImage(mainIconName)
                                .withIconSize(1.5f)
                        )

                        val allPoints = accesses.map { LatLng(it.latitude, it.longitude) } + LatLng(latitude, longitude)

                        if (allPoints.size > 1) {
                            val boundsBuilder = LatLngBounds.Builder()
                            allPoints.forEach { boundsBuilder.include(it) }
                            val bounds = boundsBuilder.build()
                            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200))
                        } else {
                            map.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 16.0)
                            )
                        }

                        // Click listener para los símbolos
                        symbolManager.addClickListener { clickedSymbol ->
                            if (clickedSymbol == mainSymbol || clickedSymbol in accessesSymbols) {
                                showDirectionsPopup = true
                                selectedMarkerLatitude = clickedSymbol.latLng.latitude
                                selectedMarkerLongitude = clickedSymbol.latLng.longitude
                                true  // <- Indica que manejaste el clic
                            } else {
                                false // <- No se maneja el clic
                            }
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