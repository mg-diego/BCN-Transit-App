package com.bcntransit.app.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.PedalBike
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bcntransit.app.BCNTransitApp.Screens.map.FullScreenMap
import com.bcntransit.app.BCNTransitApp.components.MiniMap
import com.bcntransit.app.R
import com.bcntransit.app.api.ApiClient
import com.bcntransit.app.api.BicingApiService
import com.bcntransit.app.data.enums.TransportType
import com.bcntransit.app.model.FavoriteDto
import com.bcntransit.app.screens.search.bicing.BicingStationViewModel
import com.bcntransit.app.util.getAndroidId
import com.example.bcntransit.BCNTransitApp.components.CustomFloatingActionButton
import com.example.bcntransit.BCNTransitApp.components.CustomTopBar
import kotlinx.coroutines.launch

@Composable
fun BicingStationScreen(
    stationId: String,
    bicingApiService: BicingApiService,
    onBackClick: () -> Unit
) {
    val viewModel: BicingStationViewModel = viewModel(
        key = "bicing-$stationId",
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return BicingStationViewModel(bicingApiService, stationId) as T
            }
        }
    )

    val currentUserId = getAndroidId(LocalContext.current)
    val station by viewModel.stationState.collectAsState()
    val loading by viewModel.loading.collectAsState()

    var showFullMap by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isFavorite by remember { mutableStateOf(false) }
    var isLoadingFavorite by remember { mutableStateOf(false) }

    // Check Favorito inicial
    LaunchedEffect(station, currentUserId) {
        if (station != null) {
            try {
                isFavorite = ApiClient.userApiService.userHasFavorite(
                    userId = currentUserId,
                    type = TransportType.BICING.type,
                    itemId = station!!.id
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    if (loading || station == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = colorResource(R.color.medium_red))
        }
        return
    }

    val selectedStation = station!!

    Box(modifier = Modifier.fillMaxSize()) {
        if (showFullMap) {
            FullScreenMap(
                transportType = TransportType.BICING.type,
                latitude = selectedStation.latitude,
                longitude = selectedStation.longitude,
                accesses = emptyList(), // Bicing no suele tener info de ascensores
                onDismiss = { showFullMap = false }
            )
        } else {
            Scaffold(
                topBar = {
                    CustomTopBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 16.dp)
                            ) {
                                // Icono Bicing
                                Icon(
                                    painter = painterResource(R.drawable.bicing), // Asegúrate de tener este recurso
                                    contentDescription = null,
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(50.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    // Nombre (Calle + Número)
                                    Text(
                                        text = "${selectedStation.streetName}, ${selectedStation.streetNumber}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    // Subtítulo con estado
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "(${selectedStation.id})  ·  ",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        val isOpen = selectedStation.status == 1
                                        val alertText = if (isOpen) "En servicio" else "Fuera de servicio"
                                        val alertColor = if (isOpen) colorResource(R.color.dark_green) else colorResource(R.color.red)

                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(alertColor, shape = CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            alertText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Botón Favorito
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            try {
                                                isLoadingFavorite = true
                                                if (isFavorite) {
                                                    ApiClient.userApiService.deleteUserFavorite(
                                                        currentUserId,
                                                        TransportType.BICING.type,
                                                        selectedStation.id
                                                    )
                                                    isFavorite = false
                                                } else {
                                                    ApiClient.userApiService.addUserFavorite(
                                                        currentUserId,
                                                        favorite = FavoriteDto(
                                                            USER_ID = currentUserId,
                                                            TYPE = TransportType.BICING.type,
                                                            LINE_CODE = "BICING",
                                                            LINE_NAME = "Bicing",
                                                            LINE_NAME_WITH_EMOJI = "🚲 Bicing",
                                                            STATION_CODE = selectedStation.id,
                                                            STATION_NAME = "${selectedStation.streetName}, ${selectedStation.streetNumber}",
                                                            STATION_GROUP_CODE = "",
                                                            coordinates = listOf(selectedStation.latitude, selectedStation.longitude)
                                                        )
                                                    )
                                                    isFavorite = true
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            } finally {
                                                isLoadingFavorite = false
                                            }
                                        }
                                    }
                                ) {
                                    if (isLoadingFavorite) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = colorResource(R.color.medium_red))
                                    } else {
                                        Icon(
                                            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                            contentDescription = "Favorito",
                                            tint = colorResource(R.color.red)
                                        )
                                    }
                                }
                            }
                        },
                        onBackClick = onBackClick,
                        height = 80.dp
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        // SECCIÓN 1: Disponibilidad General
                        item {
                            Text("Disponibilidad", style = MaterialTheme.typography.titleMedium)
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Tarjeta de BICIS
                                AvailabilityCard(
                                    modifier = Modifier.weight(1f),
                                    title = "Bicis",
                                    count = selectedStation.bikes,
                                    icon = Icons.Default.DirectionsBike,
                                    color = colorResource(R.color.red)
                                )

                                // Tarjeta de SLOTS
                                AvailabilityCard(
                                    modifier = Modifier.weight(1f),
                                    title = "Anclajes",
                                    count = selectedStation.slots,
                                    icon = Icons.Default.LocalParking,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // SECCIÓN 2: Tipos de Bici
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Tipos de Bici Disponibles", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Eléctricas
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFFFFC107)) // Amber
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Eléctricas", modifier = Modifier.weight(1f))
                                        Text("${selectedStation.electrical_bikes}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    }

                                    Divider(modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth())

                                    // Mecánicas
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.PedalBike, contentDescription = null, tint = Color.Gray)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Mecánicas", modifier = Modifier.weight(1f))
                                        Text("${selectedStation.mechanical_bikes}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                            }
                        }

                        // SECCIÓN 3: Ubicación (MiniMap)
                        item {
                            Text("Ubicación", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
                        }
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            ) {
                                MiniMap(
                                    transportType = TransportType.BICING.type,
                                    latitude = selectedStation.latitude,
                                    longitude = selectedStation.longitude,
                                    accesses = emptyList(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .padding(top = 8.dp)
                                )
                                CustomFloatingActionButton(
                                    onClick = { showFullMap = true },
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(16.dp),
                                    imageVector = Icons.Filled.OpenInFull,
                                    contentDescription = "Abrir mapa"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
