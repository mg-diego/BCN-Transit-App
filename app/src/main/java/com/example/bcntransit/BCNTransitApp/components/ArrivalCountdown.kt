package com.example.bcntransit.BCNTransitApp.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.delay
import remainingTime
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun ArrivalCountdown(arrivalEpochSeconds: Long, index: Int) {
    var remaining by remember { mutableStateOf(remainingTime(arrivalEpochSeconds)) }
    var showExactTime by remember { mutableStateOf(false) }
    var showDate by remember { mutableStateOf(false) }

    LaunchedEffect(arrivalEpochSeconds) {
        while (true) {
            val nowMillis = System.currentTimeMillis()
            val arrivalMillis = arrivalEpochSeconds * 1000

            remaining = remainingTime(arrivalEpochSeconds)
            showExactTime = arrivalMillis - nowMillis > TimeUnit.HOURS.toMillis(1)

            // comprobar si es el mismo día
            val nowCal = Calendar.getInstance()
            val arrivalCal = Calendar.getInstance().apply { timeInMillis = arrivalMillis }
            showDate = nowCal.get(Calendar.YEAR) != arrivalCal.get(Calendar.YEAR) ||
                    nowCal.get(Calendar.DAY_OF_YEAR) != arrivalCal.get(Calendar.DAY_OF_YEAR)

            delay(1000)
        }
    }

    val displayText = if (showExactTime) {
        val pattern = if (showDate) "dd/MM HH:mm" else "HH:mm"
        val formatter = remember(pattern) { SimpleDateFormat(pattern, Locale.getDefault()) }
        formatter.format(Date(arrivalEpochSeconds * 1000)) + "h"
    } else {
        remaining
    }

    Text(
        text = displayText,
        style = if(index == 0) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
        fontStyle = if (!showExactTime) FontStyle.Italic else FontStyle.Normal,
        fontWeight = if (remaining == "Llegando") { FontWeight.Bold } else { FontWeight.Normal }
    )
}