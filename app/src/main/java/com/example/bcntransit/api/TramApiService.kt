package com.bcntransit.app.api

import com.bcntransit.app.model.transport.AccessDto
import com.bcntransit.app.model.transport.LineDto
import com.bcntransit.app.model.transport.RouteDto
import com.bcntransit.app.model.transport.StationDto
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

    @GET("tram/stops/{stationCode}/connections")
    override suspend fun getStationConnections(@Path("stationCode") stationCode: String): List<LineDto>

    @GET("tram/stops/{stationCode}/accesses")
    override suspend fun getStationAccesses(@Path("stationCode") stationCode: String): List<AccessDto>
}