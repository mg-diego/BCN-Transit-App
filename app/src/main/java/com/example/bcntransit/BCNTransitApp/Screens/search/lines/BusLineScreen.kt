package com.bcntransit.app.screens.search

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
import com.bcntransit.app.BCNTransitApp.Screens.search.lines.BusLineCard
import com.bcntransit.app.BCNTransitApp.components.CategoryCollapsable
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
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Text("Líneas", style = MaterialTheme.typography.titleLarge)
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
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
