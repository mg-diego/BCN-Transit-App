package com.bcntransit.app.api

import com.bcntransit.app.model.transport.AccessDto
import com.bcntransit.app.model.transport.LineDto
import com.bcntransit.app.model.transport.RouteDto
import com.bcntransit.app.model.transport.StationDto
import retrofit2.http.GET
import retrofit2.http.Path

interface MetroApiService : ApiService {
    @GET("metro/lines")
    override suspend fun getLines(): List<LineDto>

    @GET("metro/stations")
    override suspend fun getStations(): List<StationDto>

    @GET("metro/lines/{lineId}/stations")
    override suspend fun getStationsByLine(@Path("lineId") lineId: String): List<StationDto>

    @GET("metro/stations/{stationCode}/routes")
    override suspend fun getStationRoutes(@Path("stationCode") stationCode: String): List<RouteDto>

    @GET("metro/stations/{stationCode}/connections")
    override suspend fun getStationConnections(@Path("stationCode") stationCode: String): List<LineDto>

    @GET("metro/stations/{stationCode}")
    override suspend fun getStationByCode(@Path("stationCode") stationCode: String): StationDto

    @GET("metro/stations/{stationCode}/accesses")
    override suspend fun getStationAccesses(@Path("stationCode") stationCode: String): List<AccessDto>
}