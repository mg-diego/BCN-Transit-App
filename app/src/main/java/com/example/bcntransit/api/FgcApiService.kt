package com.example.bcntransit.api

import com.example.bcntransit.model.LineDto
import com.example.bcntransit.model.RouteDto
import com.example.bcntransit.model.StationDto
import retrofit2.http.GET
import retrofit2.http.Path

interface FgcApiService : ApiService {
    @GET("fgc/lines")
    override suspend fun getLines(): List<LineDto>

    @GET("fgc/stations")
    override suspend fun getStations(): List<StationDto>

    @GET("fgc/stations/{stationCode}/routes")
    override suspend fun getStationRoutes(@Path("stationCode") stationCode: String): List<RouteDto>

    @GET("fgc/lines/{lineId}/stations")
    override suspend fun getStationsByLine(@Path("lineId") lineId: String): List<StationDto>

    @GET("fgc/stations/{stationCode}")
    override suspend fun getStationByCode(@Path("stationCode") stationCode: String): StationDto

    @GET("fgc/stations/{stationCode}/connections")
    override suspend fun getStationConnections(@Path("stationCode") stationCode: String): List<LineDto>
}