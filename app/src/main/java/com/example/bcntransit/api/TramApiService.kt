package com.example.bcntransit.api

import com.example.bcntransit.model.LineDto
import retrofit2.http.GET

interface TramApiService {
    @GET("tram/lines")
    suspend fun getTramLines(): List<LineDto>
}