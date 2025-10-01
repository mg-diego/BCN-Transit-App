package com.example.bcntransit.api

import com.example.bcntransit.model.transport.AccessDto
import com.example.bcntransit.model.transport.LineDto
import com.example.bcntransit.model.transport.RouteDto
import com.example.bcntransit.model.transport.StationDto

interface ApiService {
    suspend fun getLines(): List<LineDto>
    suspend fun getStations(): List<StationDto>
    suspend fun getStationsByLine(lineId: String): List<StationDto>
    suspend fun getStationRoutes(stationCode: String): List<RouteDto>
    suspend fun getStationByCode(stationCode: String): StationDto
    suspend fun getStationConnections(stationCode: String): List<LineDto>
    suspend fun getStationAccesses(stationCode: String): List<AccessDto>
}