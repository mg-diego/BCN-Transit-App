package com.bcntransit.app.api

import com.bcntransit.app.model.transport.AccessDto
import com.bcntransit.app.model.transport.LineDto
import com.bcntransit.app.model.transport.RouteDto
import com.bcntransit.app.model.transport.StationDto

interface ApiService {
    suspend fun getLines(): List<LineDto>
    suspend fun getStations(): List<StationDto>
    suspend fun getStationsByLine(lineId: String): List<StationDto>
    suspend fun getStationRoutes(stationCode: String): List<RouteDto>
    suspend fun getStationByCode(stationCode: String): StationDto
    suspend fun getStationConnections(stationCode: String): List<LineDto>
    suspend fun getStationAccesses(stationCode: String): List<AccessDto>
}