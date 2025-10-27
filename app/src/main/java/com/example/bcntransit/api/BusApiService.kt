package com.bcntransit.app.api

import com.bcntransit.app.model.transport.AccessDto
import com.bcntransit.app.model.transport.LineDto
import com.bcntransit.app.model.transport.RouteDto
import com.bcntransit.app.model.transport.StationDto
import retrofit2.http.GET
import retrofit2.http.Path

interface BusApiService : ApiService {
    @GET("bus/lines")
    override suspend fun getLines(): List<LineDto>

    @GET("bus/stops")
    override suspend fun getStations(): List<StationDto>

    @GET("bus/stops/{stationCode}/routes")
    override suspend fun getStationRoutes(@Path("stationCode") stationCode: String): List<RouteDto>

    @GET("bus/lines/{lineId}/stops")
    override suspend fun getStationsByLine(@Path("lineId") lineId: String): List<StationDto>

    @GET("bus/stops/{stationCode}/connections")
    override suspend fun getStationConnections(@Path("stationCode") stationCode: String): List<LineDto>

    @GET("bus/stops/{stationCode}")
    override suspend fun getStationByCode(@Path("stationCode") stationCode: String): StationDto

    @GET("bus/stops/{stationCode}/accesses")
    override suspend fun getStationAccesses(@Path("stationCode") stationCode: String): List<AccessDto>
}