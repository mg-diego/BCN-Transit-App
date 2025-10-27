package com.bcntransit.app.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

data class ArrivalDisplay(
    val text: String,
    val showExactTime: Boolean
)

fun formatArrivalTime(arrivalEpochSeconds: Long): ArrivalDisplay {
    val nowMillis = System.currentTimeMillis()
    val arrivalMillis = arrivalEpochSeconds * 1000

    // Calculamos tiempo restante
    val remaining = remainingTime(arrivalEpochSeconds)

    // Mostrar hora exacta si falta más de 1h
    val showExactTime = arrivalMillis - nowMillis > TimeUnit.HOURS.toMillis(1)

    if (!showExactTime) return ArrivalDisplay(remaining, false)

    // Comprobar si es otro día
    val nowCal = Calendar.getInstance()
    val arrivalCal = Calendar.getInstance().apply { timeInMillis = arrivalMillis }
    val showDate = nowCal.get(Calendar.YEAR) != arrivalCal.get(Calendar.YEAR) ||
            nowCal.get(Calendar.DAY_OF_YEAR) != arrivalCal.get(Calendar.DAY_OF_YEAR)

    val pattern = if (showDate) "dd/MM HH:mm" else "HH:mm"
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    val text = formatter.format(Date(arrivalMillis)) + "h"

    return ArrivalDisplay(text, true)
}

private fun remainingTime(arrivalEpochSeconds: Long): String {
    val nowMs = System.currentTimeMillis()
    val arrivalMs = arrivalEpochSeconds * 1000
    val diffMs = arrivalMs - nowMs

    return if (diffMs <= 40000) {
        "Llegando"
    } else {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMs)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(diffMs) % 60
        if (minutes > 0) "$minutes min ${seconds}s"
        else "${seconds}s"
    }
}
