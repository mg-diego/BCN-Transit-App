package com.example.bcntransit.api

import com.example.bcntransit.model.LineDto
import com.example.bcntransit.model.StationDto
import com.example.bcntransit.model.RouteDto
import retrofit2.http.GET
import retrofit2.http.Path

interface MetroApiService {
    @GET("metro/lines")
    suspend fun getMetroLines(): List<LineDto>

    @GET("metro/stations")
    suspend fun getMetroStations(): List<StationDto>

    @GET("metro/lines/{lineId}/stations")
    suspend fun getMetroStationsByLine(@Path("lineId") lineId: String): List<StationDto>

    @GET("metro/stations/{stationId}/routes")
    suspend fun getMetroStationRoutes(@Path("stationId") stationId: String): List<RouteDto>
}