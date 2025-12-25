package com.bcntransit.app.screens.search

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bcntransit.app.BCNTransitApp.Screens.search.lines.BusLineItem
import com.bcntransit.app.BCNTransitApp.components.InlineErrorBanner
import com.bcntransit.app.R
import com.bcntransit.app.api.ApiClient
import com.bcntransit.app.api.ApiService
import com.bcntransit.app.data.enums.TransportType
import com.bcntransit.app.model.transport.LineDto
import com.example.bcntransit.BCNTransitApp.components.CustomTopBar

@Composable
fun BusLinesScreen(
    onLineClick: (LineDto) -> Unit,
    onBackClick: () -> Unit
) {
    val viewModel: BusLinesViewModel = viewModel(
        factory = BusLinesViewModelFactory(ApiClient.busApiService)
    )
    val lines by viewModel.lines.collectAsState()
    val loadingLines by viewModel.loadingLines.collectAsState()
    val errorLines by viewModel.errorLines.collectAsState()
    val expandedStates by viewModel.expandedStates.collectAsState()
    val context = LocalContext.current

    Scaffold(
        // Usamos surface normal para que se funda con la lista
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            CustomTopBar(
                onBackClick = onBackClick,
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val drawableId = remember(TransportType.BUS) {
                            context.resources.getIdentifier(
                                TransportType.BUS.type,
                                "drawable",
                                context.packageName
                            )
                        }
                        Icon(
                            painter = painterResource(drawableId),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(38.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Líneas de Bus",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                loadingLines -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colorResource(R.color.medium_red))
                    }
                }

                errorLines != null -> {
                    InlineErrorBanner(errorLines!!)
                }

                else -> {
                    val groupedByCategory = remember(lines) {
                        lines.groupBy { viewModel.mapToCustomCategory(it) }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        groupedByCategory.forEach { (category, linesInCategory) ->
                            // 1. Cabecera de Categoría (Estilo Flat)
                            item {
                                CategoryHeaderFlat(
                                    title = category,
                                    count = linesInCategory.size,
                                    isExpanded = expandedStates[category] == true,
                                    onToggle = { viewModel.toggleCategory(category) }
                                )
                            }

                            // 2. Elementos de la lista
                            if (expandedStates[category] == true) {
                                item {
                                    Column(modifier = Modifier.animateContentSize()) {
                                        linesInCategory.forEach { line ->
                                            BusLineItem(
                                                line = line,
                                                onClick = { onLineClick(line) },
                                                drawableId = viewModel.mapLineToDrawableId(line, context)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Componente visual para la cabecera de la categoría
// Estilo: Fondo gris suave, texto en negrita, flecha a la derecha
@Composable
fun CategoryHeaderFlat(
    title: String,
    count: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f), // Gris suave
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$title ($count)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Colapsar" else "Expandir",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    // Divisor fino para separar cabecera de items
    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
}

// ... BusLineItem function goes here (código de arriba) ...

// ... ViewModelFactory ...
class BusLinesViewModelFactory(
    private val apiService: ApiService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BusLinesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BusLinesViewModel(apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}