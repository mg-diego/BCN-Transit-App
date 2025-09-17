package com.example.bcntransit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.bcntransit.api.ApiClient
import com.example.bcntransit.data.enums.BottomTab
import com.example.bcntransit.data.enums.SearchOption
import com.example.bcntransit.model.LineDto
import com.example.bcntransit.model.StationDto
import com.example.bcntransit.navigation.BottomNavigationBar
import com.example.bcntransit.screens.PlaceholderScreen
import com.example.bcntransit.screens.map.MapScreen
import com.example.bcntransit.screens.search.SearchScreen
import com.example.bcntransit.screens.search.TransportStationScreen

@Composable
fun BCNTransitApp(onDataLoaded: () -> Unit) {
    val context = LocalContext.current

    // Estados de datos
    var metroLines by remember { mutableStateOf<List<LineDto>>(emptyList()) }
    var metroStations by remember { mutableStateOf<List<StationDto>>(emptyList()) }
    var tramLines by remember { mutableStateOf<List<LineDto>>(emptyList()) }
    var rodaliesLines by remember { mutableStateOf<List<LineDto>>(emptyList()) }
    var fgcLines by remember { mutableStateOf<List<LineDto>>(emptyList()) }

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    var selectedTab by remember { mutableStateOf(BottomTab.MAP) }
    var currentSearchScreen by remember { mutableStateOf<SearchOption?>(null) }
    var selectedLine by remember { mutableStateOf<LineDto?>(null) }
    var selectedStation by remember { mutableStateOf<StationDto?>(null) }

    // Carga inicial de datos
    LaunchedEffect(Unit) {
        try {
            metroLines = ApiClient.metroApiService.getMetroLines()
            tramLines = ApiClient.tramApiService.getTramLines()
            rodaliesLines = ApiClient.rodaliesApiService.getRodaliesLines()
            fgcLines = ApiClient.fgcApiService.getFgcLines()
            metroStations = ApiClient.metroApiService.getMetroStations()
        } catch (e: Exception) {
            e.printStackTrace()
            error = e.message
        } finally {
            loading = false
            onDataLoaded()
        }
    }

    // Carga bajo demanda de cada red
    fun loadMetroLines() = loadLines { metroLines = it }
    fun loadTramLines() = loadLines { tramLines = it }
    fun loadRodalies() = loadLines { rodaliesLines = it }
    fun loadFgcLines() = loadLines { fgcLines = it }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(selectedTab) { tab ->
                selectedTab = tab
                if (tab == BottomTab.SEARCH) {
                    // Reseteamos selección al entrar en búsqueda
                    currentSearchScreen = null
                    selectedLine = null
                    selectedStation = null
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                BottomTab.MAP -> MapScreen(context)
                BottomTab.SEARCH -> {
                    if (currentSearchScreen == null) {
                        SearchScreen { currentSearchScreen = it }
                    } else {
                        when (currentSearchScreen) {
                            SearchOption.METRO -> TransportStationScreen(
                                lines = metroLines,
                                selectedLine = selectedLine,
                                selectedStation = selectedStation,
                                loadLines = { loadMetroLines() },
                                loadStationsByLine = { ApiClient.metroApiService.getMetroStationsByLine(it) },
                                loadStationRoutes = { ApiClient.metroApiService.getMetroStationRoutes(it) },
                                onLineSelected = { selectedLine = it },
                                onStationSelected = { selectedStation = it }
                            )
                            SearchOption.TRAM -> TransportStationScreen(
                                lines = tramLines,
                                selectedLine = selectedLine,
                                selectedStation = selectedStation,
                                loadLines = { loadTramLines() },
                                loadStationsByLine = { ApiClient.tramApiService.getTramStopsByLine(it) },
                                loadStationRoutes = { ApiClient.tramApiService.getTramStopRoutes(it) },
                                onLineSelected = { selectedLine = it },
                                onStationSelected = { selectedStation = it }
                            )
                            SearchOption.RODALIES -> TransportStationScreen(
                                lines = rodaliesLines,
                                selectedLine = selectedLine,
                                selectedStation = selectedStation,
                                loadLines = { loadRodalies() },
                                loadStationsByLine = { ApiClient.rodaliesApiService.getRodaliesStationsByLine(it) },
                                loadStationRoutes = { ApiClient.rodaliesApiService.getRodaliesStationRoutes(it) },
                                onLineSelected = { selectedLine = it },
                                onStationSelected = { selectedStation = it }
                            )
                            SearchOption.FGC -> TransportStationScreen(
                                lines = fgcLines,
                                selectedLine = selectedLine,
                                selectedStation = selectedStation,
                                loadLines = { loadFgcLines() },
                                loadStationsByLine = { ApiClient.fgcApiService.getFgcStationsByLine(it) },
                                loadStationRoutes = { ApiClient.fgcApiService.getFgcStationRoutes(it) },
                                onLineSelected = { selectedLine = it },
                                onStationSelected = { selectedStation = it }
                            )
                            SearchOption.BUS -> PlaceholderScreen("Bus")
                            SearchOption.BICING -> PlaceholderScreen("Bicing")
                            null -> {}
                        }
                    }
                }
                BottomTab.FAVORITES -> PlaceholderScreen("Favoritos")
                BottomTab.USER -> PlaceholderScreen("Usuario")
            }
        }
    }
}

private fun loadLines(onLoaded: (List<LineDto>) -> Unit) {
    try {
    } catch (e: Exception) {
        e.printStackTrace()
    }
}