package com.example.bcntransit.model

import kotlinx.serialization.Serializable

@Serializable
data class RouteDto(
    val route_id: String,
    val line_type: String,
    val line_name: String,
    val color: String,
    val destination: String,
    val next_trips: List<NextTrip>,
    val name_with_emoji: String,
    val line_id: String,
    val line_code: String
)

@Serializable
data class NextTrip(
    val id: String,
    val arrival_time: Long,
    val delay_in_minutes: Int,
    val platform: String
)
