package com.example.bcntransit.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.bcntransit.data.enums.SettingsOption
import com.example.bcntransit.util.UserIdentifier

@Composable
fun SettingsScreen(onNavigate: (SettingsOption) -> Unit) {
    val userId = UserIdentifier.getUserId(LocalContext.current) // si quieres usarlo

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Usuario", style = MaterialTheme.typography.headlineMedium)

        SettingsCard(
            icon = Icons.Default.Notifications,
            title = "Notificaciones",
            description = "Configura tus alertas y notificaciones"
        ) { onNavigate(SettingsOption.NOTIFICATIONS) }

        SettingsCard(
            icon = Icons.Default.Face,
            title = "Idioma",
            description = "Selecciona el idioma de la app"
        ) { onNavigate(SettingsOption.LANGUAGE) }

        SettingsCard(
            icon = Icons.Default.Build,
            title = "Ayuda",
            description = "Consulta la guía de uso"
        ) { onNavigate(SettingsOption.HELP) }
    }
}

