package com.example.bcntransit.screens.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import com.example.bcntransit.api.ApiClient
import com.example.bcntransit.model.transport.NearbyStation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await
import org.maplibre.android.annotations.Icon
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style

fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

suspend fun getUserLocation(context: Context): LatLng? {
    if (!hasLocationPermission(context)) return null

    val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    return try {
        val location = fusedLocationClient.lastLocation.await()
        location?.let { LatLng(it.latitude, it.longitude) }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

suspend fun getNearbyStations(latitude: Double, longitude: Double, radiusKm: Double = 0.5): List<NearbyStation> {
    return try {
        ApiClient.resultsApiService.getResultsByLocation(latitude, longitude, radiusKm)
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

fun getDrawableIdByName(context: Context, transportType: String): Int {
    return context.resources.getIdentifier(
        transportType.lowercase(), "drawable", context.packageName
    )
}

fun getMarkerIcon(context: Context, drawableRes: Int, sizePx: Int = 80): Icon {
    // Cargar el drawable
    val drawable: Drawable = ContextCompat.getDrawable(context, drawableRes)
        ?: throw IllegalArgumentException("Drawable not found")

    // Convertir a Bitmap
    val bitmap: Bitmap = if (drawable is BitmapDrawable) {
        drawable.bitmap
    } else {
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bmp
    }

    // Crear el Icon para MapLibre
    return IconFactory.getInstance(context).fromBitmap(bitmap)
}

// Cantidad de desplazamiento en grados
// Positivo en latitude → mueve hacia el norte
// Positivo en longitude → mueve hacia el este
fun LatLng.withOffset(latOffset: Double = 0.0, lngOffset: Double = 0.0): LatLng {
    return LatLng(this.latitude + latOffset, this.longitude + lngOffset)
}

fun getMarkerSize(type: String): Int =
    when (type.lowercase()) {
        "metro" -> 90
        "bus" -> 40
        "bicing" -> 60
        "tram" -> 80
        "rodalies" -> 65
        "fgc" -> 60
        else -> 50
    }

/**
 * Crea y gestiona el ciclo de vida de un MapView para Compose
 */
@Composable
fun rememberMapViewWithLifecycle(context: Context): MapView {
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

    return mapView
}

/**
 * Configura el estilo base del mapa con gestos personalizables
 */
fun configureMapStyle(
    context: Context,
    map: MapLibreMap,
    enableLocationComponent: Boolean = false,
    scrollEnabled: Boolean = true,
    zoomEnabled: Boolean = true,
    rotateEnabled: Boolean = false,
    tiltEnabled: Boolean = false,
    onStyleLoaded: (Style) -> Unit = {}
) {
    map.setStyle(
        Style.Builder()
            .fromUri("https://basemaps.cartocdn.com/gl/voyager-gl-style/style.json")
    ) { style ->
        if (enableLocationComponent && hasLocationPermission(context)) {
            val locationComponent = map.locationComponent
            val options = LocationComponentActivationOptions
                .builder(context, style)
                .useDefaultLocationEngine(true)
                .build()
            locationComponent.activateLocationComponent(options)
            locationComponent.isLocationComponentEnabled = true
            locationComponent.cameraMode = CameraMode.TRACKING
            locationComponent.renderMode = RenderMode.COMPASS
        }

        map.uiSettings.apply {
            isScrollGesturesEnabled = scrollEnabled
            isZoomGesturesEnabled = zoomEnabled
            isRotateGesturesEnabled = rotateEnabled
            isTiltGesturesEnabled = tiltEnabled
        }

        onStyleLoaded(style)
    }
}

/**
 * Añade un marker de estación al mapa y centra la cámara
 */
fun addMarker(
    context: Context,
    map: MapLibreMap,
    iconName: String,
    latitude: Double,
    longitude: Double,
    markerSizeMultiplier: Float = 1f,
    zoom: Double = 14.0
): Marker {
    val drawableId = getDrawableIdByName(context, iconName)
    val markerSize = (getMarkerSize(iconName) * markerSizeMultiplier).toInt()
    val stationLatLng = LatLng(latitude, longitude)

    val marker = map.addMarker(
        MarkerOptions()
            .position(stationLatLng)
            .setIcon(getMarkerIcon(context, drawableId, sizePx = markerSize))
    )

    val cameraPosition = CameraPosition.Builder()
        .target(stationLatLng)
        .zoom(zoom)
        .build()
    map.cameraPosition = cameraPosition

    return marker
}
