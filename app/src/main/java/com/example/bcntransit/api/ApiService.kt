package com.example.bcntransit.api

import com.example.bcntransit.model.LineDto
import com.example.bcntransit.model.RouteDto
import com.example.bcntransit.model.StationDto
import retrofit2.http.GET

interface ApiService {
    suspend fun getLines(): List<LineDto>
    suspend fun getStations(): List<StationDto>
    suspend fun getStationsByLine(lineId: String): List<StationDto>
    suspend fun getStationRoutes(stationCode: String): List<RouteDto>
    suspend fun getStationByCode(stationCode: String): StationDto
    suspend fun getStationConnections(stationCode: String): List<LineDto>
}