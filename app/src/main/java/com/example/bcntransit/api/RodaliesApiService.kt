package com.example.bcntransit.api

import com.example.bcntransit.model.LineDto
import com.example.bcntransit.model.RouteDto
import com.example.bcntransit.model.StationDto
import retrofit2.http.GET
import retrofit2.http.Path

interface RodaliesApiService {
    @GET("rodalies/lines")
    suspend fun getRodaliesLines(): List<LineDto>

    @GET("rodalies/stations")
    suspend fun getRodaliesStations(): List<StationDto>

    @GET("rodalies/lines/{lineId}/stations")
    suspend fun getRodaliesStationsByLine(@Path("lineId") lineId: String): List<StationDto>

    @GET("rodalies/stations/{stationId}/routes")
    suspend fun getRodaliesStationRoutes(@Path("stationId") stationId: String): List<RouteDto>

    @GET("rodalies/stations/{stationId}")
    suspend fun getRodaliesStation(@Path("stationId") stationId: String): StationDto
}