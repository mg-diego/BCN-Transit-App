package com.example.bcntransit.api

import com.example.bcntransit.model.transport.BicingStationDto
import retrofit2.http.GET
import retrofit2.http.Path

interface BicingApiService : ApiService {
    @GET("bicing/stations")
    suspend fun getBicingStations(): List<BicingStationDto>

    @GET("bicing/stations/{stationId}")
    suspend fun getBicingStation(@Path("stationId") stationId: String): BicingStationDto
}