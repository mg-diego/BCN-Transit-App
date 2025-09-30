package com.example.bcntransit.screens.map

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.example.bcntransit.api.ApiClient
import com.example.bcntransit.model.NearbyStation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await
import org.maplibre.android.annotations.Icon
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.geometry.LatLng

fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.ACCESS_FINE_LOCATION
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

/**
 * Convierte un drawable en un Icon de MapLibre
 */
fun getMapIcon(context: Context, drawableId: Int, sizePx: Int = 80): Icon {
    val drawable = ContextCompat.getDrawable(context, drawableId)
        ?: throw IllegalArgumentException("Drawable no encontrado")

    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    return IconFactory.getInstance(context).fromBitmap(bitmap)
}

fun getDrawableIdByTransportType(context: Context, transportType: String): Int {
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
