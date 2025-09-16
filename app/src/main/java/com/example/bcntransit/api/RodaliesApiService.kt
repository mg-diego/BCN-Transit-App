package com.example.bcntransit.api

import com.example.bcntransit.model.LineDto
import retrofit2.http.GET

interface RodaliesApiService {
    @GET("rodalies/lines")
    suspend fun getRodaliesLines(): List<LineDto>
}