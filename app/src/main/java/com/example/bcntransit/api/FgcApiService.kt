package com.example.bcntransit.api

import com.example.bcntransit.model.LineDto
import com.example.bcntransit.model.RouteDto
import com.example.bcntransit.model.StationDto
import retrofit2.http.GET
import retrofit2.http.Path

interface FgcApiService {
    @GET("fgc/lines")
    suspend fun getFgcLines(): List<LineDto>

    @GET("fgc/stations")
    suspend fun getFgcStations(): List<StationDto>

    @GET("fgc/lines/{lineId}/stations")
    suspend fun getFgcStationsByLine(@Path("lineId") lineId: String): List<StationDto>

    @GET("fgc/stations/{stationId}/routes")
    suspend fun getFgcStationRoutes(@Path("stationId") stationId: String): List<RouteDto>
}