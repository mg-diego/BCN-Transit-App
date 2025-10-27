package com.bcntransit.app.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bcntransit.app.R
import com.bcntransit.app.data.enums.SearchOption
import com.example.bcntransit.BCNTransitApp.components.CustomTopBar

@Composable
fun SearchScreen(
    onTypeSelected: (String) -> Unit
) {
    val searchItems = listOf(
        Triple("Metro", "Ver líneas y estaciones de metro", R.drawable.metro),
        Triple("Bus", "Ver líneas y paradas de bus", R.drawable.bus),
        Triple("Tram", "Ver líneas y paradas de tram", R.drawable.tram),
        Triple("Rodalies", "Ver líneas y estaciones de Rodalies", R.drawable.rodalies),
        Triple("FGC", "Ver líneas y estaciones de FGC", R.drawable.fgc),
    )

    Scaffold(
        topBar = {
            CustomTopBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Route,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = Color.Unspecified
                        )
                        Text(
                            text = "Líneas",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                },
                onBackClick = { },
                showBackButton = false
            )
        },
        content = { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                item {
                    Text(
                        text = "Selecciona el tipo de transporte para explorar sus líneas y estaciones disponibles.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                item {
                    Text(
                        text = "Tipos de transporte disponibles",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                itemsIndexed(searchItems) { index, item ->
                    SearchItem(
                        iconRes = item.third,
                        title = item.first,
                        description = item.second,
                        onClick = {
                            val option = SearchOption.entries[index]
                            onTypeSelected(option.name)
                        }
                    )
                }
            }
        }
    )
}
