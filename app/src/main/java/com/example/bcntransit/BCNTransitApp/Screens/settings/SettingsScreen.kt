package com.bcntransit.app.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bcntransit.app.R
import com.bcntransit.app.util.LanguageManager
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

    // --- LÓGICA DE IDIOMA ---
    var showLanguageDialog by remember { mutableStateOf(false) }

    // Obtenemos el idioma actual
    val currentLangCode = remember { LanguageManager.getCurrentLanguage(context) }

    // Mapeo visual
    val currentLanguageName = when (currentLangCode) {
        "es" -> "Español"
        "ca" -> "Català"
        "en" -> "English"
        else -> "Español"
    }

    // Diálogo emergente
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguageCode = currentLangCode,
            onDismiss = { showLanguageDialog = false },
            onLanguageSelected = { code ->
                showLanguageDialog = false
                LanguageManager.setLocale(context, code)
            }
        )
    }

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
            // --- CONTENIDO PRINCIPAL ---
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

                SettingsClickableItem(
                    title = "Idioma",
                    description = currentLanguageName,
                    onClick = { showLanguageDialog = true }
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

            // --- FOOTER ---
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
                    SocialMediaButton(label = "GitHub", onClick = { /* TODO */ })
                    SocialMediaButton(label = "Twitter", onClick = { /* TODO */ })
                    SocialMediaButton(label = "Web", onClick = { /* TODO */ })
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

// ------------------------------------------
// COMPONENTES AUXILIARES (UI)
// ------------------------------------------

@Composable
fun LanguageSelectionDialog(
    currentLanguageCode: String,
    onDismiss: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    val languages = listOf("es" to "Español", "ca" to "Català", "en" to "English")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Seleccionar idioma") },
        text = {
            Column {
                languages.forEach { (code, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (code == currentLanguageCode),
                                onClick = { onLanguageSelected(code) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (code == currentLanguageCode),
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = name, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = colorResource(R.color.medium_red),
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
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        CustomSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingsClickableItem(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SettingsNavigationItem(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SocialMediaButton(label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick) { Text(text = label) }
}