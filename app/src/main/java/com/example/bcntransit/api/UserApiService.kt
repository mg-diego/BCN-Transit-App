package com.bcntransit.app.api

import com.bcntransit.app.model.FavoriteDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


import retrofit2.http.Query

interface UserApiService {
    @POST("users/{userId}/register")
    suspend fun registerUser(
        @Path("userId") userId: String,
        @Body body: Map<String, String>
    ): Boolean

    @POST("users/{userId}/notifications/toggle/{status}")
    suspend fun toggleUserNotifications(
        @Path("userId") userId: String,
        @Path("status") status: Boolean
    ): Boolean

    @GET("users/{userId}/notifications/configuration")
    suspend fun getUserNotificationsConfiguration(
        @Path("userId") userId: String
    ): Boolean

    @GET("users/{userId}/favorites")
    suspend fun getUserFavorites(@Path("userId") userId: String): List<FavoriteDto>

    @POST("users/{userId}/favorites")
    suspend fun addUserFavorite(
        @Path("userId") userId: String,
        @Body favorite: FavoriteDto
    ): Boolean

    @DELETE("users/{userId}/favorites")
    suspend fun deleteUserFavorite(
        @Path("userId") userId: String,
        @Query("type") type: String,
        @Query("item_id") itemId: String
    ): Boolean

    @GET("users/{userId}/favorites/exists")
    suspend fun userHasFavorite(
        @Path("userId") userId: String,
        @Query("type") type: String,
        @Query("item_id") itemId: String
    ): Boolean
}