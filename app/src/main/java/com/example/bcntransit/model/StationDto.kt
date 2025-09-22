package com.example.bcntransit.model

data class StationDto(
    val id: String,
    val code: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val order: Int,
    val transport_type: String,
    val name_with_emoji: String?,
    val description: String?,
    val line_id: String,
    val line_code: String,
    val line_color: String,
    val line_name: String,
    val line_name_with_emoji: String?,
    val has_alerts: Boolean,
    val alerts: List<StationAlertDto>,
    val CODI_GRUP_ESTACIO: Int,
    val ORIGEN_SERVEI: String?,
    val DESTI_SERVEI: String?,
    val DESTI_SENTIT: String?,
    val connections: List<LineDto>?
)

data class StationAlertDto(
    val headerCa: String,
    val headerEn: String,
    val headerEs: String,
    val textCa: String,
    val textEn: String,
    val textEs: String
)
