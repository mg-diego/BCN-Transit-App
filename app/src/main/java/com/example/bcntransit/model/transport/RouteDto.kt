package com.bcntransit.app.model.transport


data class RouteDto(
    val route_id: String,
    val line_type: String,
    val line_name: String,
    val color: String,
    val destination: String,
    val next_trips: List<NextTripDto>,
    val name_with_emoji: String,
    val line_id: String,
    val line_code: String
)

