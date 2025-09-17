package com.example.bcntransit.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import com.example.bcntransit.model.LineDto

@Composable
fun LineListScreen(
    lines: List<LineDto>,
    loading: Boolean,
    error: String?,
    onLineClick: (LineDto) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text("Líneas", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        when {
            loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            error != null -> Text("Error: $error", color = Color.Red)
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(lines) { line ->
                    LineCard(line) {
                        onLineClick(line) // llama al callback
                    }
                }
            }
        }
    }
}
