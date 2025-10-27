package com.bcntransit.app.model.transport

data class AccessDto(
    val id: String,
    val code: String,
    val name: String,
    val station_group_code: String,
    val station_id: String,
    val station_name: String,
    val accesibility_type_id: String,
    val accesibility_type: String,
    val number_of_elevators: Int,
    val latitude: Double,
    val longitude: Double,
)