package com.example.bcntransit.api

import com.example.bcntransit.model.LineDto
import com.example.bcntransit.model.RouteDto
import com.example.bcntransit.model.StationDto
import retrofit2.http.GET
import retrofit2.http.Path

interface TramApiService {
    @GET("tram/lines")
    suspend fun getTramLines(): List<LineDto>

    @GET("tram/stations")
    suspend fun getTramStops(): List<StationDto>

    @GET("tram/lines/{lineId}/stations")
    suspend fun getTramStopsByLine(@Path("lineId") lineId: String): List<StationDto>

    @GET("tram/stations/{stationId}/routes")
    suspend fun getTramStopRoutes(@Path("stationId") stationId: String): List<RouteDto>
}