package com.example.bcntransit.api

import com.example.bcntransit.model.LineDto
import com.example.bcntransit.model.RouteDto
import com.example.bcntransit.model.StationDto
import retrofit2.http.GET
import retrofit2.http.Path

interface BusApiService {
    @GET("bus/lines")
    suspend fun getBusLines(): List<LineDto>

    @GET("bus/stops")
    suspend fun getBusStops(): List<StationDto>

    @GET("bus/lines/{lineId}/stops")
    suspend fun getBusStopsByLine(@Path("lineId") lineId: String): List<StationDto>

    @GET("bus/stops/{stopId}/routes")
    suspend fun getBusStopRoutes(@Path("stopId") stopId: String): List<RouteDto>
}