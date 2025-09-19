package com.example.bcntransit.model

data class StationDto(
    val id: String,
    val code: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val order: Int,
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
    val connections: List<ConnectionDto>
)

data class StationAlertDto(
    val headerCa: String,
    val headerEn: String,
    val headerEs: String,
    val textCa: String,
    val textEn: String,
    val textEs: String
)

data class ConnectionDto(
    val ID_ESTACIO: Int?,
    val CODI_ESTACIO: Int?,
    val ID_LINIA_BASE: Int?,
    val CODI_LINIA_BASE: Int?,
    val ORDRE_BASE: Int?,
    val ID_OPERADOR: Int?,
    val NOM_OPERADOR: String?,
    val CODI_OPERADOR: String?,
    val CODI_FAMILIA: Int?,
    val NOM_FAMILIA: String?,
    val ORDRE_FAMILIA: Int?,
    val ID_LINIA: Int?,
    val CODI_LINIA: Int?,
    val NOM_LINIA: String?,
    val DESC_LINIA: String?,
    val ORIGEN_LINIA: String?,
    val DESTI_LINIA: String?,
    val ORDRE_LINIA: Int?,
    val COLOR_LINIA: String?,
    val COLOR_TEXT_LINIA: String?,
    val CODI_ELEMENT_CORRESP: Int?,
    val NOM_ELEMENT_CORRESP: String?,
    val DESC_CORRESP: String?,
    val DATA: String?
)
