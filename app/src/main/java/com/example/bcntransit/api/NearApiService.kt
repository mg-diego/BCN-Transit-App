package com.example.bcntransit.api

import com.example.bcntransit.model.NearbyStation
import retrofit2.http.GET
import retrofit2.http.Query

interface NearApiService {

    /**
     * Obtiene estaciones cercanas a una ubicación dada.
     * @param lat Latitud del usuario
     * @param lon Longitud del usuario
     * @param radius Radio de búsqueda en km (opcional, default 1km)
     */
    @GET("near")
    suspend fun getNearbyStations(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("radius") radiusKm: Double = 0.5
    ): List<NearbyStation>
}