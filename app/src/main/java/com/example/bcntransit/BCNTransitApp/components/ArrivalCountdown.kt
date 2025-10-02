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
import com.example.bcntransit.utils.formatArrivalTime
import kotlinx.coroutines.delay

@Composable
fun ArrivalCountdown(arrivalEpochSeconds: Long, index: Int) {
    var display by remember { mutableStateOf(formatArrivalTime(arrivalEpochSeconds)) }

    LaunchedEffect(arrivalEpochSeconds) {
        while (true) {
            display = formatArrivalTime(arrivalEpochSeconds)
            delay(1000)
        }
    }

    Text(
        text = display.text,
        style = if(index == 0) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
        fontStyle = if (!display.showExactTime) FontStyle.Italic else FontStyle.Normal,
        fontWeight = if (display.text == "Llegando") FontWeight.Bold else FontWeight.Normal
    )
}