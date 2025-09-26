package com.example.bcntransit.api

import com.example.bcntransit.model.BicingStation
import retrofit2.http.GET
import retrofit2.http.Path

interface BicingApiService : ApiService {
    @GET("bicing/stations")
    suspend fun getBicingStations(): List<BicingStation>

    @GET("bicing/stations/{stationId}")
    suspend fun getBicingStation(@Path("stationId") stationId: String): BicingStation
}