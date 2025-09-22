package com.example.bcntransit.api

import com.example.bcntransit.model.LineDto
import com.example.bcntransit.model.RouteDto
import com.example.bcntransit.model.StationDto
import retrofit2.http.GET
import retrofit2.http.Path

interface TramApiService {
    @GET("tram/lines")
    suspend fun getTramLines(): List<LineDto>

    @GET("tram/stops")
    suspend fun getTramStops(): List<StationDto>

    @GET("tram/lines/{lineId}/stops")
    suspend fun getTramStopsByLine(@Path("lineId") lineId: String): List<StationDto>

    @GET("tram/stops/{stationId}/routes")
    suspend fun getTramStopRoutes(@Path("stationId") stationId: String): List<RouteDto>

    @GET("tram/stops/{stationId}")
    suspend fun getTramStation(@Path("stationId") stationId: String): StationDto
}