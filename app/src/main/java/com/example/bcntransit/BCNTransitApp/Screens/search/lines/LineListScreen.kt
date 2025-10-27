package com.bcntransit.app.screens.search

import android.annotation.SuppressLint
import android.text.Html
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import com.bcntransit.app.BCNTransitApp.components.InlineErrorBanner
import com.bcntransit.app.api.ApiService
import com.bcntransit.app.data.enums.TransportType
import com.bcntransit.app.model.transport.LineDto
import com.example.bcntransit.BCNTransitApp.components.CustomTopBar
import com.bcntransit.app.R
import com.example.bcntransit.BCNTransitApp.components.ExpandableItem

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LineListScreen(
    transportType: TransportType,
    apiService: ApiService,
    onLineClick: (LineDto) -> Unit,
    onBackClick: () -> Unit
) {
    val viewModel: LineListViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        key = "$transportType-${apiService.hashCode()}",
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return LineListViewModel(apiService, transportType) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()
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
                        val drawableId = remember(transportType) {
                            context.resources.getIdentifier(transportType.type, "drawable", context.packageName)
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
                },
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFDD5555)) // medium_red
                }

                uiState.error != null -> InlineErrorBanner(uiState.error!!)

                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    item {
                        Text(
                            text = "Líneas disponibles",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(uiState.lines) { line ->
                        LineItem(line) { onLineClick(line) }
                    }
                }
            }
        }
    }
}
