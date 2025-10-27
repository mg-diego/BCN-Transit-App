package com.bcntransit.app.BCNTransitApp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Banner de error para mostrar fallos de API incrustado en la pantalla.
 *
 * @param message Mensaje de error a mostrar.
 * @param onRetry Callback opcional al pulsar "Reintentar".
 * @param modifier Modifier opcional para personalizar posición.
 */
@Composable
fun InlineErrorBanner(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFFFCDD2), shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Error,
            contentDescription = "Error",
            tint = Color(0xFFD32F2F),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFD32F2F),
            modifier = Modifier.weight(1f)
        )
        if (onRetry != null) {
            TextButton(onClick = onRetry) {
                Text("Reintentar", color = Color(0xFFD32F2F))
            }
        }
    }
}
