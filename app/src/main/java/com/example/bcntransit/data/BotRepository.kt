package com.example.bcntransit.data

import com.example.bcntransit.api.MetroApiService
import com.example.bcntransit.api.TramApiService

import com.example.bcntransit.model.LineDto

class BotRepository(private val apiMetro: MetroApiService, private val apiTram: TramApiService) {

    suspend fun fetchMetroLines(): List<LineDto> {
        return try {
            apiMetro.getMetroLines()
        } catch (e: Exception) {
            // Puedes manejar error aquí o propagarlo
            throw IllegalStateException("Error al obtener líneas de metro", e)
        }
    }

    suspend fun fetchTramLines(): List<LineDto> {
        return try {
            apiTram.getTramLines()
        } catch (e: Exception) {
            // Puedes manejar error aquí o propagarlo
            throw IllegalStateException("Error al obtener líneas de tram", e)
        }
    }

}
