package com.bcntransit.app.model.transport

import java.util.Locale

data class LineDto(
    val id: String,
    val code: String,
    val name: String,
    val description: String,
    val origin: String,
    val destination: String,
    val color: String,
    val transport_type: String,
    val name_with_emoji: String,
    val has_alerts: Boolean,
    val alerts: List<AlertDto>,
    val category: String? = ""
)

data class AlertDto(
    val id: Int,
    val transport_type: String,
    val begin_date: String,
    val end_date: String?,
    val status: String?,
    val cause: String?,
    val publications: List<PublicationDto>,
    val affected_entities: List<AffectedEntityDto>
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

fun PublicationDto.getLocalizedHeader(): String {
    val language = Locale.getDefault().language

    fun getHeader(lang: String): String? {
        return when (lang) {
            "es" -> headerEs
            "en" -> headerEn
            "ca" -> headerCa
            else -> null
        }
    }

    val target = getHeader(language)

    return if (!target.isNullOrEmpty()) target
    else if (!headerCa.isNullOrEmpty()) headerCa
    else headerEs ?: ""
}

fun PublicationDto.getLocalizedBody(): String {
    val language = Locale.getDefault().language

    fun getText(lang: String): String? {
        return when (lang) {
            "es" -> textEs
            "en" -> textEn
            "ca" -> textCa
            else -> null
        }
    }

    val target = getText(language)

    return if (!target.isNullOrEmpty()) target
    else if (!textCa.isNullOrEmpty()) textCa
    else textEs ?: ""
}
