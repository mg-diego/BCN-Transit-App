package com.bcntransit.app.data.enums

enum class TransportType(val type: String) {
    METRO("metro"),
    BUS("bus"),
    TRAM("tram"),
    RODALIES("rodalies"),
    FGC("fgc"),
    BICING("bicing");

    companion object {
        fun from(value: String): TransportType {
            return values().firstOrNull { it.type.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown TransportType: $value")
        }
    }
}