package com.example.bcntransit.model


data class NearbyStation(
    val type: String,
    val line_name: String? = null,  // solo para metro y bicing (vacío para bicing)
    val line_code: String? = null,  // puede ser String o Int, usar String para uniformidad
    val station_name: String,
    val station_code: String,       // convertir todo a String para uniformidad
    val coordinates: List<Double>,  // [latitude, longitude]

    // Campos específicos de bicing
    val slots: Int? = null,
    val mechanical: Int? = null,
    val electrical: Int? = null,
    val availability: Int? = null,
    val distance_km: Double
) {
    val latitude: Double
        get() = coordinates.getOrNull(0) ?: 0.0

    val longitude: Double
        get() = coordinates.getOrNull(1) ?: 0.0
}
