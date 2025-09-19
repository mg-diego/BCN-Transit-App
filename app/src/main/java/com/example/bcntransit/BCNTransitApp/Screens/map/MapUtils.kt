package com.example.bcntransit.screens.map

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.bcntransit.api.ApiClient
import com.example.bcntransit.model.NearbyStation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.IconFactory

fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

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

suspend fun getNearbyStations(latitude: Double, longitude: Double): List<NearbyStation> {
    return try {
        ApiClient.nearApiService.getNearbyStations(latitude, longitude)
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}