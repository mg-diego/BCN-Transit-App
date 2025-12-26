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

// --- CONSTANTES DE DISEÑO ---
private const val ICON_SIZE = 64         // Lienzo total
private const val STATION_RADIUS = 16f   // Radio del círculo de la estación (un poco más pequeño para que quepa todo)
private const val ALERT_RADIUS = 14f     // Radio del círculo rojo de alerta
private const val BORDER_WIDTH = 4f      // Borde blanco para contraste

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
                // 1. LIMPIEZA PREVIA
                currentSymbolManager?.deleteAll()
                currentSymbolManager = null
                style.getLayer("route-layer")?.let { style.removeLayer(it) }
                style.getSource("route-source")?.let { style.removeSource(it) }

                // 2. AÑADIR LA LÍNEA PRIMERO (Para que quede al fondo)
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
                    // Al añadirla ahora, se dibuja antes que los símbolos que vendrán después.
                    style.addLayer(lineLayer)
                }

                // 3. INICIALIZAR SYMBOL MANAGER (Sus capas se pondrán automáticamente ENCIMA de la línea)
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

                // --- PREPARACIÓN DE ICONOS ---
                val centerX = ICON_SIZE / 2f
                // Movemos el centro de la estación hacia abajo en el lienzo (aprox al 70% de la altura)
                val stationCenterY = ICON_SIZE * 0.7f

                // Icono Normal
                val iconNormalName = "station-circle-${lineColor.toArgb()}"
                if (style.getImage(iconNormalName) == null) {
                    val bitmap = Bitmap.createBitmap(ICON_SIZE, ICON_SIZE, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                    paint.color = lineColor.toArgb()
                    paint.style = Paint.Style.FILL
                    // Dibujamos el círculo desplazado hacia abajo
                    canvas.drawCircle(centerX, stationCenterY, STATION_RADIUS, paint)
                    style.addImage(iconNormalName, bitmap)
                }

                // Icono con ALERTA (Diseño Vertical / Piruleta)
                val iconWarningName = "station-warning-${lineColor.toArgb()}"
                if (style.getImage(iconWarningName) == null) {
                    val bitmap = Bitmap.createBitmap(ICON_SIZE, ICON_SIZE, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)

                    // a) Círculo de la Estación (Base - Abajo)
                    val paintBase = Paint(Paint.ANTI_ALIAS_FLAG)
                    paintBase.color = lineColor.toArgb()
                    paintBase.style = Paint.Style.FILL
                    canvas.drawCircle(centerX, stationCenterY, STATION_RADIUS, paintBase)

                    // --- CÁLCULO DE POSICIÓN DEL BADGE (ARRIBA) ---
                    val badgeCx = centerX // Centrado horizontalmente con la estación
                    // Calculamos la Y para que se pose justo encima de la estación, con un ligero solapamiento visual (+2f)
                    val stationTopEdge = stationCenterY - STATION_RADIUS
                    val badgeCy = stationTopEdge - ALERT_RADIUS + 2f

                    // b) Borde Blanco del Badge (Para contraste máximo)
                    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                    borderPaint.color = android.graphics.Color.WHITE
                    borderPaint.style = Paint.Style.FILL
                    canvas.drawCircle(badgeCx, badgeCy, ALERT_RADIUS + BORDER_WIDTH, borderPaint)

                    // c) Círculo Rojo del Badge
                    val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG)
                    badgePaint.color = android.graphics.Color.RED // Rojo intenso
                    badgePaint.style = Paint.Style.FILL
                    canvas.drawCircle(badgeCx, badgeCy, ALERT_RADIUS, badgePaint)

                    // d) Signo de Exclamación (!)
                    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                    textPaint.color = android.graphics.Color.WHITE
                    textPaint.textSize = ALERT_RADIUS * 1.5f // Texto grande y legible
                    textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textPaint.textAlign = Paint.Align.CENTER
                    // Centrado vertical del texto
                    val textY = badgeCy - ((textPaint.descent() + textPaint.ascent()) / 2)
                    canvas.drawText("!", badgeCx, textY, textPaint)

                    style.addImage(iconWarningName, bitmap)
                }

                // 4. CREAR SÍMBOLOS
                stations.forEach { station ->
                    val iconToUse = if (station.has_alerts) iconWarningName else iconNormalName

                    symbolManager.create(
                        SymbolOptions()
                            .withLatLng(LatLng(station.latitude, station.longitude))
                            .withIconImage(iconToUse)
                            .withIconSize(1.0f)
                            .withTextField(station.name)
                            // Ajustamos el offset del texto porque el icono ahora es más alto visualmente
                            .withTextOffset(arrayOf(0f, 2.0f))
                            .withTextSize(12f)
                            .withTextColor("#000000")
                            .withTextHaloColor("#FFFFFF")
                            .withTextHaloWidth(2f)
                            .withTextAnchor("top")
                    )
                }

                // 5. CÁMARA
                if (stations.isNotEmpty()) {
                    val boundsBuilder = LatLngBounds.Builder()
                    stations.forEach { s -> boundsBuilder.include(LatLng(s.latitude, s.longitude)) }
                    val bounds = boundsBuilder.build()
                    // Añadimos padding para que los iconos de alerta no se corten en los bordes
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