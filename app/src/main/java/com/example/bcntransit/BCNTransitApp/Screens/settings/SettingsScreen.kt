package com.bcntransit.app.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bcntransit.app.util.getAndroidId
import com.example.bcntransit.BCNTransitApp.Screens.settings.SettingsViewModel
import com.example.bcntransit.BCNTransitApp.Screens.settings.SettingsViewModelFactory
import com.example.bcntransit.BCNTransitApp.components.CustomSwitch
import com.example.bcntransit.BCNTransitApp.components.CustomTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToTermsAndConditions: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(context, getAndroidId(context))
    )
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            CustomTopBar(
                title = { Text("Configuración") },
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Contenido principal
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Sección: Notificaciones
                SectionHeader("🔔 NOTIFICACIONES")

                SettingsSwitchItem(
                    title = "Recibir alertas",
                    description = "Notificaciones push sobre el estado del transporte",
                    checked = state.receiveAlerts,
                    onCheckedChange = { viewModel.toggleReceiveAlerts(it) }
                )

                Divider(modifier = Modifier.padding(horizontal = 16.dp))

                // Sección: Preferencias
                SectionHeader("⚙️ PREFERENCIAS")

                SettingsSwitchItem(
                    title = "Tema oscuro",
                    description = "Usar tema oscuro en la aplicación",
                    checked = state.darkTheme,
                    onCheckedChange = { viewModel.toggleDarkTheme(it) }
                )

                Divider(modifier = Modifier.padding(horizontal = 16.dp))

                // Sección: Información
                SectionHeader("ℹ️ INFORMACIÓN")

                SettingsNavigationItem(
                    title = "Acerca de",
                    onClick = onNavigateToAbout
                )

                SettingsNavigationItem(
                    title = "Política de privacidad",
                    onClick = onNavigateToPrivacy
                )

                SettingsNavigationItem(
                    title = "Términos y condiciones",
                    onClick = onNavigateToTermsAndConditions
                )
            }

            // Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Redes sociales
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    SocialMediaButton(
                        label = "GitHub",
                        onClick = { /* TODO: Abrir GitHub */ }
                    )
                    SocialMediaButton(
                        label = "Twitter",
                        onClick = { /* TODO: Abrir Twitter */ }
                    )
                    SocialMediaButton(
                        label = "Web",
                        onClick = { /* TODO: Abrir Web */ }
                    )
                }


                Text(
                    text = "Identificador:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = getAndroidId(context),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Versión 1.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End
                )

            }
        }
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
fun SettingsSwitchItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        CustomSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingsNavigationItem(
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SocialMediaButton(
    label: String,
    onClick: () -> Unit
) {
    TextButton(onClick = onClick) {
        Text(text = label)
    }
}