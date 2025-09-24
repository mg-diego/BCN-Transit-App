package com.example.bcntransit.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.example.bcntransit.R
import com.example.bcntransit.api.ApiService
import com.example.bcntransit.data.enums.TransportType
import com.example.bcntransit.model.LineDto
import com.example.bcntransit.model.StationDto

@Composable
fun TransportStationScreen(
    transportType: TransportType,
    selectedLine: LineDto?,
    selectedStation: StationDto?,
    apiService: ApiService,
    onLineSelected: (LineDto) -> Unit,
    onStationSelected: (StationDto?) -> Unit,
    isLoading: Boolean = false,
    currentUserId: String
) {

    Box(modifier = Modifier.fillMaxSize()) {
        // ======== CARGA DESDE FAVORITOS ========
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colorResource(R.color.medium_red))
            }
        }

        // ======== CAPA DE RUTAS A PANTALLA COMPLETA ========
        else if (selectedStation != null) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Barra superior con botón de volver
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { onStationSelected(null) }) {
                            Text("← Volver")
                        }
                    }

                    RoutesScreen(
                        station = selectedStation,
                        lineCode = selectedLine?.code ?: "",
                        apiService = apiService,
                        onStationSelected = { st -> onStationSelected(st) },
                        onLineSelected = { ln -> onLineSelected(ln) }
                    )
                }
            }
        }

        // ======== LISTADO DE LÍNEAS O ESTACIONES ========
        else if (selectedLine == null) {
            LineListScreen(
                transportType = transportType,
                apiService = apiService,
                onLineClick = onLineSelected
            )
        } else {
            StationListScreen(
                selectedLine,
                apiService = apiService,
                currentUserId = currentUserId,
                onStationClick = { st -> onStationSelected(st) }
            )
        }
    }
}
