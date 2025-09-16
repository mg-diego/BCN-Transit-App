package com.example.bcntransit.model

data class TramLineDto(
    val id: Int,
    val name: String,
    val description: Description,
    val network: Network,
    val code: Int,
    val image: String?,
    val color: String?,
    val original_name: String?,
    val has_alerts: Boolean,
    val alerts: List<AlertDto>
)

data class Description(
    val ca: String?,
    val es: String?,
    val en: String?
)

data class Network(
    val id: Int,
    val name: String
)

data class AlertDto(
    val id: String?,
    val transport_type: String?,
    val begin_date: String?,
    val end_date: String?,
    val status: String?,
    val cause: String?,
    val publications: List<PublicationDto>?,
    val affected_entities: List<AffectedEntityDto>?
)

data class PublicationDto(
    val headerCa: String?,
    val headerEn: String?,
    val headerEs: String?,
    val textCa: String?,
    val textEn: String?,
    val textEs: String?
)

data class AffectedEntityDto(
    val direction_code: String?,
    val direction_name: String?,
    val entrance_code: String?,
    val entrance_name: String?,
    val line_code: String?,
    val line_name: String?,
    val station_code: String?,
    val station_name: String?
)
