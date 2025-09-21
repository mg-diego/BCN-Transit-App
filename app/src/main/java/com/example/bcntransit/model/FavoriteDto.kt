package com.example.bcntransit.model

data class FavoriteDto(
    val USER_ID: String,
    val TYPE: String,
    val STATION_CODE: String,
    val STATION_NAME: String,
    val STATION_GROUP_CODE: String?,
    val LINE_NAME: String?,
    val LINE_NAME_WITH_EMOJI: String?,
    val LINE_CODE: String,
    val LATITUDE: Long,
    val LONGITUDE: Long
)

data class FavoriteItem(
    val STATION_CODE: String,
    val STATION_NAME: String,
    val STATION_GROUP_CODE: String,
    val LINE_NAME: String,
    val LINE_NAME_WITH_EMOJI: String,
    val LINE_CODE: String,
    val coordinates: List<Double>
)

data class FavoritePostRequest(
    val type: String,
    val item: FavoriteItem
)