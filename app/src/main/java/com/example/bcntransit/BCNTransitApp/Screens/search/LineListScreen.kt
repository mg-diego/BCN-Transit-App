package com.example.bcntransit.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.bcntransit.api.ApiService
import com.example.bcntransit.data.enums.TransportType

import com.example.bcntransit.model.LineDto

@Composable
fun LineListScreen(
    transportType: TransportType,
    apiService: ApiService,
    onLineClick: (LineDto) -> Unit
) {
    val context = LocalContext.current
    var lines by remember { mutableStateOf<List<LineDto>>(emptyList()) }
    var loadingLines by remember { mutableStateOf(true) }
    var errorStations by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        loadingLines = true
        try {
            lines = apiService.getLines()
        } catch (e: Exception) {
            errorStations = e.message
        } finally {
            loadingLines = false
        }
    }

    Column {
        // Cabecera con sombra
        Box(Modifier.fillMaxWidth()) {
            val drawableId = remember(transportType) {
                context.resources.getIdentifier(transportType.type, "drawable", context.packageName)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp)
            ) {
                Icon(
                    painter = painterResource(drawableId),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(42.dp)
                )
                Spacer(Modifier.width(16.dp))
                Text("Líneas", style = MaterialTheme.typography.titleLarge)
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        when {
            loadingLines -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            errorStations != null -> Text("Error: $errorStations", color = Color.Red)
            else -> LazyColumn(modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(lines) { line ->
                    LineCard(line) {
                        onLineClick(line)
                    }
                }
            }
        }
    }
}
