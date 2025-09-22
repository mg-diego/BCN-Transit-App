package com.example.bcntransit.model

data class BicingStation(
    val id: String,
    val type: String,
    val latitude: Double,
    val longitude: Double,
    val streetName: String,
    val streetNumber: String,
    val slots: Int,
    val bikes: Int,
    val type_bicing: Int,
    val electrical_bikes: Int,
    val mechanical_bikes: Int,
    val status: Int,
    val disponibilidad: Int,
    val icon: String,
    val transition_start: String,
    val transition_end: String,
    val obcn: String
)