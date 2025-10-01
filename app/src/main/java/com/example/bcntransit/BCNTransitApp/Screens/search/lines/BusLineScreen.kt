package com.example.bcntransit.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bcntransit.BCNTransitApp.Screens.search.lines.BusLineCard
import com.example.bcntransit.BCNTransitApp.components.CategoryCollapsable
import com.example.bcntransit.BCNTransitApp.components.InlineErrorBanner
import com.example.bcntransit.R
import com.example.bcntransit.api.ApiClient
import com.example.bcntransit.api.ApiService
import com.example.bcntransit.data.enums.TransportType
import com.example.bcntransit.model.transport.LineDto

@Composable
fun BusLinesScreen(
    onLineClick: (LineDto) -> Unit
) {

    val viewModel: BusLinesViewModel = viewModel(
        factory = BusLinesViewModelFactory(ApiClient.busApiService)
    )
    val lines by viewModel.lines.collectAsState()
    val loadingLines by viewModel.loadingLines.collectAsState()
    val errorLines by viewModel.errorLines.collectAsState()
    val expandedStates by viewModel.expandedStates.collectAsState()
    val context = LocalContext.current

    LazyColumn {
        item {
            // Cabecera con sombra
            Box(Modifier.fillMaxWidth()) {
                val drawableId = remember(TransportType.BUS) {
                    context.resources.getIdentifier(
                        TransportType.BUS.type,
                        "drawable",
                        context.packageName
                    )
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
        }

        when {
            loadingLines -> item {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colorResource(R.color.medium_red))
                }
            }

            errorLines != null -> item { InlineErrorBanner(errorLines!!) }
            else -> {
                val groupedByCategory = lines.groupBy { viewModel.mapToCustomCategory(it) }
                groupedByCategory.forEach { (category, linesInCategory) ->
                    item {
                        CategoryCollapsable(
                            category = category,
                            isExpanded = expandedStates[category] == true,
                            onToggle = { viewModel.toggleCategory(category) }
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                linesInCategory.forEach { line ->
                                    BusLineCard(
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
