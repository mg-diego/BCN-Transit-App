package com.example.bcntransit.model

data class FavoriteDto(
    val user_id: String,             // sigue siendo String
    val type: String,
    val station_code: String,        // puede ser número o texto ("LH")
    val station_name: String,
    val codi_group_estacio: String?, // puede venir como "" -> nullable
    val line_name: String?,
    val line_name_with_emoji: String?,
    val line_code: String,           // puede ser int o string ("R1", "L8")
    val latitude: Long,
    val longitude: Long
)