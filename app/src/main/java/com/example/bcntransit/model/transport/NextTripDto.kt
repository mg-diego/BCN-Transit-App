package com.bcntransit.app.model.transport


data class NextTripDto(
    val id: String,
    val arrival_time: Long,
    val delay_in_minutes: Int,
    val platform: String
)