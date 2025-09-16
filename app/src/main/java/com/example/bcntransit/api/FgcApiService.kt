package com.example.bcntransit.api

import com.example.bcntransit.model.LineDto
import retrofit2.http.GET

interface FgcApiService {
    @GET("fgc/lines")
    suspend fun getFgcLines(): List<LineDto>
}