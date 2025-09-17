package com.example.bcntransit.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bcntransit.R
import com.example.bcntransit.data.enums.SearchOption

@Composable
fun SearchScreen(onNavigate: (SearchOption) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Buscar", style = MaterialTheme.typography.headlineMedium)

        val searchItems = listOf(
            Triple("Metro", "Ver líneas y estaciones de metro", R.drawable.metro),
            Triple("Bus", "Ver líneas y paradas de bus", R.drawable.bus),
            Triple("Tram", "Ver líneas y paradas de tram", R.drawable.tram),
            Triple("Rodalies", "Ver líneas y estaciones de Rodalies", R.drawable.rodalies),
            Triple("FGC", "Ver líneas y estaciones de FGC", R.drawable.fgc),
            Triple("Bicing", "Ver estaciones de Bicing", R.drawable.bicing)
        )

        searchItems.forEachIndexed { index, item ->
            SearchCard(
                iconRes = item.third,
                title = item.first,
                description = item.second,
                onClick = { onNavigate(SearchOption.values()[index]) }
            )
        }
    }
}
