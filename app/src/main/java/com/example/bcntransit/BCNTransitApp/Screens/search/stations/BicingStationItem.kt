package com.bcntransit.app.screens.search.stations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bcntransit.app.model.transport.BicingStationDto

@Composable
fun BicingStationItem(station: BicingStationDto, filter: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        //elevation = CardDefaults.cardElevation(4.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(station.streetName + " " + station.streetNumber,
                    style = MaterialTheme.typography.titleMedium)
                // Opcional: distancia si tienes lat/lon del usuario
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (filter) {
                "Todas" -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "   - Bicis eléctricas: ${station.electrical_bikes}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "   - Bicis mecánicas: ${station.mechanical_bikes}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "   - Slots libres: ${station.slots}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                "Slots" -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "   - Slots libres: ${station.slots}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                "Eléctricas" -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "   - Bicis eléctricas: ${station.electrical_bikes}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                "Mecánicas" -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "   - Bicis mecánicas: ${station.mechanical_bikes}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
