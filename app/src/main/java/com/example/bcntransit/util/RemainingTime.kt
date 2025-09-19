import java.util.concurrent.TimeUnit

fun remainingTime(arrivalEpochSeconds: Long): String {
    val nowMs = System.currentTimeMillis()
    val arrivalMs = arrivalEpochSeconds * 1000
    val diffMs = arrivalMs - nowMs

    return if (diffMs <= 40000) {
        "Llegando"
    } else {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMs)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(diffMs) % 60
        // Ajusta formato como prefieras
        if (minutes > 0) "$minutes min ${seconds}s"
        else "${seconds}s"
    }
}
