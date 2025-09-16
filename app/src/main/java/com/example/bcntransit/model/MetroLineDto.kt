package com.example.bcntransit.model

import kotlinx.serialization.Serializable

@Serializable
data class MetroLineDto (
    val ID_LINIA: Int,
    val CODI_LINIA: Int,
    val NOM_LINIA: String,
    val DESC_LINIA: String,
    val ORIGEN_LINIA: String,
    val DESTI_LINIA: String,
    val NUM_PAQUETS: Int,
    val ID_OPERADOR: Int,
    val NOM_OPERADOR: String,
    val NOM_TIPUS_TRANSPORT: String,
    val CODI_FAMILIA: Int,
    val NOM_FAMILIA: String,
    val ORDRE_FAMILIA: Int,
    val ORDRE_LINIA: Int,
    val CODI_TIPUS_CALENDARI: String,
    val NOM_TIPUS_CALENDARI: String,
    val DATA: String,
    val COLOR_LINIA: String,
    val COLOR_AUX_LINIA: String,
    val COLOR_TEXT_LINIA: String,
    val ORIGINAL_NOM_LINIA: String,
    val has_alerts: Boolean,
    val alerts: List<Alert> = emptyList()
)

@Serializable
data class Alert(
    val id: Int,
    val transport_type: String,
    val begin_date: String,
    val end_date: String,
    val status: String,
    val cause: String,
    val publications: List<Publication> = emptyList(),
    val affected_entities: List<AffectedEntity> = emptyList()
)

@Serializable
data class Publication(
    val headerCa: String,
    val headerEn: String,
    val headerEs: String,
    val textCa: String,
    val textEn: String,
    val textEs: String
)

@Serializable
data class AffectedEntity(
    val direction_code: String,
    val direction_name: String,
    val entrance_code: String,
    val entrance_name: String,
    val line_code: String,
    val line_name: String,
    val station_code: String,
    val station_name: String
)
