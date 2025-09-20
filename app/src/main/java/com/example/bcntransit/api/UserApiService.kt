package com.example.bcntransit.api

import com.example.bcntransit.model.FavoriteDto
import com.example.bcntransit.model.RegisterUserDto
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UserApiService {
    @POST("users/{userId}/register")
    suspend fun registerUser(@Path("userId") userId: String): RegisterUserDto

    @GET("users/{userId}/favorites")
    suspend fun getUserFavorites(@Path("userId") userId: String): List<FavoriteDto>

}