package com.example.bcntransit.api

import com.example.bcntransit.model.MetroLineDto
import retrofit2.http.GET

interface MetroApiService {
    @GET("metro/lines")
    suspend fun getMetroLines(): List<MetroLineDto>
}