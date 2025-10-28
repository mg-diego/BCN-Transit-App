package com.bcntransit.app.BCNTransitApp.screens.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.bcntransit.app.model.transport.StationDto
import com.bcntransit.app.screens.map.configureMapStyle
import com.bcntransit.app.screens.map.rememberMapViewWithLifecycle
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.plugins.annotation.SymbolOptions
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.lineColor
import org.maplibre.android.style.layers.PropertyFactory.lineWidth
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point

@Composable
fun StationsMap(
    stations: List<StationDto>,
    lineColor: Color,
    modifier: Modifier = Modifier,
    onStationClick: (StationDto) -> Unit
) {
    val context = LocalContext.current
    val mapView = rememberMapViewWithLifecycle(context)

    var currentSymbolManager by remember { mutableStateOf<SymbolManager?>(null) }

    val stationsKey = remember(stations) {
        stations.joinToString(",") { it.code }
    }

    DisposableEffect(Unit) {
        onDispose {
            currentSymbolManager?.deleteAll()
            currentSymbolManager = null
        }
    }

    LaunchedEffect(stationsKey, lineColor) {
        mapView.getMapAsync { map ->
            map.getStyle { style ->
                currentSymbolManager?.deleteAll()
                currentSymbolManager = null

                style.getLayer("route-layer")?.let { style.removeLayer(it) }
                style.getSource("route-source")?.let { style.removeSource(it) }

                val symbolManager = SymbolManager(mapView, map, style).apply {
                    iconAllowOverlap = true
                    textAllowOverlap = false
                }
                currentSymbolManager = symbolManager

                symbolManager.addClickListener { symbol ->
                    val clickedStation = stations.find { station ->
                        symbol.latLng.latitude == station.latitude &&
                                symbol.latLng.longitude == station.longitude
                    }
                    clickedStation?.let { onStationClick(it) }
                    true
                }

                val iconName = "station-circle-${lineColor.toArgb()}"

                if (style.getImage(iconName) == null) {
                    val size = 32
                    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                    paint.color = lineColor.toArgb()
                    paint.style = Paint.Style.FILL
                    val radius = size / 2f
                    canvas.drawCircle(radius, radius, radius, paint)
                    style.addImage(iconName, bitmap)
                }

                stations.forEach { station ->
                    symbolManager.create(
                        SymbolOptions()
                            .withLatLng(LatLng(station.latitude, station.longitude))
                            .withIconImage(iconName)
                            .withIconSize(1.0f)
                            .withTextField(station.name)
                            .withTextOffset(arrayOf(0f, 1f))
                            .withTextSize(12f)
                            .withTextColor("#000000")
                            .withTextHaloColor("#FFFFFF")
                            .withTextHaloWidth(2f)
                            .withTextAnchor("top")
                    )
                }

                if (stations.size > 1) {
                    val points = stations.map { Point.fromLngLat(it.longitude, it.latitude) }
                    val lineSource = GeoJsonSource(
                        "route-source",
                        FeatureCollection.fromFeature(Feature.fromGeometry(LineString.fromLngLats(points)))
                    )
                    style.addSource(lineSource)

                    val lineLayer = LineLayer("route-layer", "route-source").withProperties(
                        lineColor(lineColor.toArgb()),
                        lineWidth(3f)
                    )
                    style.addLayer(lineLayer)
                }

                if (stations.isNotEmpty()) {
                    val boundsBuilder = LatLngBounds.Builder()
                    stations.forEach { s -> boundsBuilder.include(LatLng(s.latitude, s.longitude)) }
                    val bounds = boundsBuilder.build()
                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                }
            }
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { view ->
            view.getMapAsync { map ->
                configureMapStyle(
                    context = context,
                    map = map,
                    enableLocationComponent = false,
                    scrollEnabled = true,
                    zoomEnabled = true
                ) { /* El estilo ya se configura en LaunchedEffect */ }
            }
        }
    )
}