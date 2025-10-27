package com.example.bcntransit.BCNTransitApp.Screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bcntransit.BCNTransitApp.components.CustomTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            CustomTopBar(
                title = { Text("Política de privacidad") },
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Política de privacidad",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = """
                    Última actualización: 27 de octubre de 2025

                    BCNTransit respeta tu privacidad y se compromete a proteger tus datos personales. Esta política describe qué información recopilamos, cómo la usamos y qué derechos tienes como usuario.
                """.trimIndent(),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Justify
            )

            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle("1. Información que recopilamos")
            SectionBody(
                """
                • Identificador del dispositivo (Android ID), usado de forma anónima para mejorar la estabilidad de la aplicación.  
                • Preferencias de configuración, como el uso del tema oscuro o la recepción de notificaciones.  
                • Datos agregados de uso (por ejemplo, frecuencia de apertura de la app o errores).  
                BCNTransit **no recopila datos personales sensibles**, ubicaciones en tiempo real ni información de contacto.
                """
            )

            SectionTitle("2. Uso de la información")
            SectionBody(
                """
                Los datos se utilizan exclusivamente para:  
                • Mostrar información en tiempo real del transporte público de Barcelona.  
                • Enviar notificaciones sobre incidencias o alertas relevantes.  
                • Mejorar el rendimiento y la experiencia de usuario.
                """
            )

            SectionTitle("3. Compartición de datos")
            SectionBody(
                """
                BCNTransit no comparte ni vende información personal a terceros.  
                Algunos datos técnicos pueden ser procesados por servicios de terceros (por ejemplo, Firebase Cloud Messaging para notificaciones), siempre bajo sus propias políticas de privacidad.
                """
            )

            SectionTitle("4. Seguridad")
            SectionBody(
                """
                Implementamos medidas técnicas y organizativas para proteger tus datos frente a accesos no autorizados, pérdida o alteración.
                """
            )

            SectionTitle("5. Tus derechos")
            SectionBody(
                """
                Puedes solicitar en cualquier momento la eliminación de tus datos almacenados localmente desinstalando la aplicación.  
                BCNTransit no almacena datos identificables en servidores externos.
                """
            )

            SectionTitle("6. Cambios en esta política")
            SectionBody(
                """
                Esta política puede actualizarse ocasionalmente. Te notificaremos los cambios importantes mediante una actualización dentro de la aplicación o en el sitio web oficial.
                """
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Si tienes dudas sobre esta política, puedes contactarnos a través de nuestros canales oficiales (GitHub o web).",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Justify
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "© 2025 BCNTransit. Todos los derechos reservados.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SectionBody(text: String) {
    Text(
        text = text.trimIndent(),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Justify,
        modifier = Modifier.padding(bottom = 16.dp)
    )
}