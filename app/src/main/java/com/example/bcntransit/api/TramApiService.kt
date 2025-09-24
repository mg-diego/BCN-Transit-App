package com.example.bcntransit.api

import com.example.bcntransit.model.LineDto
import com.example.bcntransit.model.RouteDto
import com.example.bcntransit.model.StationDto
import retrofit2.http.GET
import retrofit2.http.Path

interface TramApiService : ApiService {
    @GET("tram/lines")
    override suspend fun getLines(): List<LineDto>

    @GET("tram/stops")
    override suspend fun getStations(): List<StationDto>

    @GET("tram/lines/{lineId}/stops")
    override suspend fun getStationsByLine(@Path("lineId") lineId: String): List<StationDto>

    @GET("tram/stops/{stationCode}/routes")
    override suspend fun getStationRoutes(@Path("stationCode") stationCode: String): List<RouteDto>

    @GET("tram/stops/{stationCode}")
    override suspend fun getStationByCode(@Path("stationCode") stationCode: String): StationDto
}