package com.example.bcntransit.api

import com.example.bcntransit.model.FavoriteDto
import com.example.bcntransit.model.FavoritePostRequest
import com.example.bcntransit.model.RegisterUserDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface UserApiService {
    @POST("users/{userId}/register")
    suspend fun registerUser(@Path("userId") userId: String): RegisterUserDto

    @GET("users/{userId}/favorites")
    suspend fun getUserFavorites(@Path("userId") userId: String): List<FavoriteDto>

    @POST("users/{userId}/favorites")
    suspend fun addUserFavorite(
        @Path("userId") userId: String,
        @Body favorite: FavoritePostRequest
    ): FavoriteDto

    @DELETE("users/{userId}/favorites")
    suspend fun deleteUserFavorite(
        @Path("userId") userId: String,
        @Query("type") type: String,
        @Query("item_id") itemId: String
    ): Unit
}