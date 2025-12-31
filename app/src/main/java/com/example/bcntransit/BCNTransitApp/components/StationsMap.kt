package com.bcntransit.app.BCNTransitApp.screens.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
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

private const val ICON_SIZE = 64
private const val STATION_RADIUS = 16f
private const val ALERT_RADIUS = 12f
private const val BORDER_WIDTH = 3f

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
        stations.joinToString(",") { "${it.code}-${it.has_alerts}" }
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
                // 1. Limpieza
                currentSymbolManager?.deleteAll()
                currentSymbolManager = null
                style.getLayer("route-layer")?.let { style.removeLayer(it) }
                style.getSource("route-source")?.let { style.removeSource(it) }

                // 2. Línea (Fondo)
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

                // 3. Symbol Manager
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

                // --- GENERACIÓN DE ICONOS ---
                // El centro es siempre el mismo para ambos casos (32, 32)
                val center = ICON_SIZE / 2f

                // A) Icono Normal
                val iconNormalName = "station-circle-${lineColor.toArgb()}"
                if (style.getImage(iconNormalName) == null) {
                    val bitmap = Bitmap.createBitmap(ICON_SIZE, ICON_SIZE, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                    paint.color = lineColor.toArgb()
                    paint.style = Paint.Style.FILL

                    // Dibujamos en el centro absoluto
                    canvas.drawCircle(center, center, STATION_RADIUS, paint)
                    style.addImage(iconNormalName, bitmap)
                }

                // B) Icono Warning (REEMPLAZO)
                val iconWarningName = "station-warning-${lineColor.toArgb()}"
                if (style.getImage(iconWarningName) == null) {
                    val bitmap = Bitmap.createBitmap(ICON_SIZE, ICON_SIZE, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)

                    // 1. Estación Base (IGUAL que el normal: Centro absoluto)
                    val paintBase = Paint(Paint.ANTI_ALIAS_FLAG)
                    paintBase.color = lineColor.toArgb()
                    paintBase.style = Paint.Style.FILL
                    canvas.drawCircle(center, center, STATION_RADIUS, paintBase)

                    // 2. Badge de Alerta (Superpuesto en la esquina superior derecha)
                    val badgeCx = center + (STATION_RADIUS * 0.7f)
                    val badgeCy = center - (STATION_RADIUS * 0.7f)

                    // Borde Blanco
                    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                    borderPaint.color = android.graphics.Color.WHITE
                    borderPaint.style = Paint.Style.FILL
                    canvas.drawCircle(badgeCx, badgeCy, ALERT_RADIUS + BORDER_WIDTH, borderPaint)

                    // Círculo Rojo
                    val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG)
                    badgePaint.color = android.graphics.Color.RED
                    badgePaint.style = Paint.Style.FILL
                    canvas.drawCircle(badgeCx, badgeCy, ALERT_RADIUS, badgePaint)

                    // Signo "!"
                    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                    textPaint.color = android.graphics.Color.WHITE
                    textPaint.textSize = ALERT_RADIUS * 1.5f
                    textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textPaint.textAlign = Paint.Align.CENTER
                    val textY = badgeCy - ((textPaint.descent() + textPaint.ascent()) / 2)
                    canvas.drawText("!", badgeCx, textY, textPaint)

                    style.addImage(iconWarningName, bitmap)
                }

                stations.forEach { station ->
                    val iconToUse = if (station.has_alerts) iconWarningName else iconNormalName

                    symbolManager.create(
                        SymbolOptions()
                            .withLatLng(LatLng(station.latitude, station.longitude))
                            .withIconImage(iconToUse)
                            .withIconSize(1.0f)
                            .withTextField(station.name)
                            .withTextOffset(arrayOf(0f, 1.2f)) // Texto debajo estándar
                            .withTextSize(12f)
                            .withTextColor("#000000")
                            .withTextHaloColor("#FFFFFF")
                            .withTextHaloWidth(2f)
                            .withTextAnchor("top")
                    )
                }

                // 5. Cámara
                if (stations.isNotEmpty()) {
                    val boundsBuilder = LatLngBounds.Builder()
                    stations.forEach { s -> boundsBuilder.include(LatLng(s.latitude, s.longitude)) }
                    val bounds = boundsBuilder.build()
                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150))
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
                ) { }
            }
        }
    )
}