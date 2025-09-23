package com.example.bcntransit.api

import com.example.bcntransit.model.LineDto
import com.example.bcntransit.model.RouteDto
import com.example.bcntransit.model.StationDto
import retrofit2.http.GET
import retrofit2.http.Path

interface RodaliesApiService : ApiService {
    @GET("rodalies/lines")
    override suspend fun getLines(): List<LineDto>

    @GET("rodalies/stations")
    override suspend fun getStations(): List<StationDto>

    @GET("rodalies/lines/{lineId}/stations")
    override suspend fun getStationsByLine(@Path("lineId") lineId: String): List<StationDto>

    @GET("rodalies/stations/{stationCode}/routes")
    override suspend fun getStationRoutes(@Path("stationCode") stationCode: String): List<RouteDto>

    @GET("rodalies/stations/{stationCode}")
    override suspend fun getStationByCode(@Path("stationCode") stationCode: String): StationDto
}