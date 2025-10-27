package com.bcntransit.app.util

sealed class ApiError(val code: String, val userMessage: String) : Throwable() {
    object Network : ApiError("E001", "No se puede conectar a internet. Revisa tu conexión.")
    object Timeout : ApiError("E002", "La solicitud tardó demasiado. Intenta de nuevo.")
    object Unauthorized : ApiError("E003", "No tienes permiso para acceder a esta información.")
    data class Unknown(val original: Throwable) : ApiError("E999", "Ocurrió un error inesperado.")
}

fun Throwable.toApiError(): ApiError = when (this) {
    is java.net.SocketTimeoutException -> ApiError.Timeout
    is java.net.UnknownHostException -> ApiError.Network
    is retrofit2.HttpException -> when (this.code()) {
        401 -> ApiError.Unauthorized
        else -> ApiError.Unknown(this)
    }
    else -> ApiError.Unknown(this)
}

