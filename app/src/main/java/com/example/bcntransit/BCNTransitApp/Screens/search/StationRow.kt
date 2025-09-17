package com.example.bcntransit.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.bcntransit.model.StationDto
import androidx.core.graphics.toColorInt

@Composable
fun StationRow(
    station: StationDto,
    isFirst: Boolean,
    isLast: Boolean,
    lineColor: String,
    onClick: () -> Unit
) {
    val circleSize = 20.dp
    val lineWidth = 4.dp
    val rowHeight = 70.dp
    val halfCircle = circleSize / 2

    val parsedColor = Color(
        android.graphics.Color.parseColor(
            if (lineColor.startsWith("#")) lineColor else "#$lineColor"
        )
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(rowHeight)
            .clickable { onClick() }
            .padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(32.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(lineWidth)
                    .fillMaxHeight()
                    .padding(
                        top = if (isFirst) halfCircle else 0.dp,
                        bottom = if (isLast) halfCircle else 0.dp
                    )
                    .background(parsedColor)
                    .align(Alignment.Center)
            )
            Box(
                modifier = Modifier
                    .size(circleSize)
                    .background(parsedColor, CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // ---- Cambiado: peso en la Column, no en los Text ----
        Column(
            modifier = Modifier.weight(1f)   // ocupa todo el espacio intermedio
        ) {
            val alertText = if (station.has_alerts) "Incidencias" else "Servicio normal"
            val alertColor = if (station.has_alerts) Color.Red else Color.Green

            Text(
                text = station.name_with_emoji ?: station.name,
                style = MaterialTheme.typography.titleMedium
            )
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(alertColor, shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(alertText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Este IconButton quedará pegado al borde derecho
        IconButton(onClick = { /* TODO: marcar favorito */ }) {
            Icon(
                imageVector = Icons.Outlined.Star,
                contentDescription = "Favorito",
                tint = Color.Red
            )
        }
    }
}


