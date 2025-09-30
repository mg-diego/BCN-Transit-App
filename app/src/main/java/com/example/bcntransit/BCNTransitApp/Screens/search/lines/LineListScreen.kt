package com.example.bcntransit.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.bcntransit.BCNTransitApp.components.InlineErrorBanner
import com.example.bcntransit.R
import com.example.bcntransit.api.ApiService
import com.example.bcntransit.data.enums.TransportType

import com.example.bcntransit.model.LineDto

@Composable
fun LineListScreen(
    transportType: TransportType,
    apiService: ApiService,
    onLineClick: (LineDto) -> Unit
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
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        when {
            uiState.loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colorResource(R.color.medium_red))
            }
            uiState.error != null -> { InlineErrorBanner(uiState.error!!) }
            else -> androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.lines) { line ->
                    LineCard(line) {
                        onLineClick(line)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}
