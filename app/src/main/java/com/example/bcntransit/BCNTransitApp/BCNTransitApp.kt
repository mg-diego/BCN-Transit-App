package com.example.bcntransit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.example.bcntransit.screens.BottomNavigationBar
import com.example.bcntransit.screens.PlaceholderScreen
import com.example.bcntransit.screens.map.MapScreen
import com.example.bcntransit.screens.search.BusLinesScreen
import com.example.bcntransit.screens.search.SearchScreen
import com.example.bcntransit.screens.search.TransportStationScreen
import android.provider.Settings
import com.example.bcntransit.screens.favorites.FavoritesScreen
import kotlinx.coroutines.launch

@Composable
fun BCNTransitApp(onDataLoaded: () -> Unit) {
    val context = LocalContext.current

    // Estados de datos
    var selectedTab by remember { mutableStateOf(BottomTab.MAP) }
    var currentSearchScreen by remember { mutableStateOf<SearchOption?>(null) }
    var selectedLine by remember { mutableStateOf<LineDto?>(null) }
    var selectedStation by remember { mutableStateOf<StationDto?>(null) }
    val androidId = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ANDROID_ID
    )

    // Carga inicial de datos
    LaunchedEffect(Unit) {
        try {
            onDataLoaded()
            // registrar usuario primero (si no depende del resto, también podría ir en paralelo)
            ApiClient.userApiService.registerUser(androidId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val coroutineScope = rememberCoroutineScope()
    var loadingFavorite by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                modifier = Modifier.fillMaxWidth(),
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    selectedTab = tab
                    if (tab == BottomTab.SEARCH) {
                        // Reseteamos selección al entrar en búsqueda
                        currentSearchScreen = null
                        selectedLine = null
                        selectedStation = null
                    }
                }
            )
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
                                selectedLine = selectedLine,
                                selectedStation = selectedStation,
                                loadLines = { ApiClient.metroApiService.getMetroLines() },
                                loadStationsByLine = { ApiClient.metroApiService.getMetroStationsByLine(it) },
                                loadStationRoutes = { ApiClient.metroApiService.getMetroStationRoutes(it) },
                                onLineSelected = { selectedLine = it },
                                onStationSelected = { selectedStation = it },
                                loadingFavorite = loadingFavorite,
                                currentUserId = androidId
                            )
                            SearchOption.TRAM -> TransportStationScreen(
                                selectedLine = selectedLine,
                                selectedStation = selectedStation,
                                loadLines = { ApiClient.tramApiService.getTramLines() },
                                loadStationsByLine = { ApiClient.tramApiService.getTramStopsByLine(it) },
                                loadStationRoutes = { ApiClient.tramApiService.getTramStopRoutes(it) },
                                onLineSelected = { selectedLine = it },
                                onStationSelected = { selectedStation = it },
                                loadingFavorite = loadingFavorite,
                                currentUserId = androidId
                            )
                            SearchOption.RODALIES -> TransportStationScreen(
                                selectedLine = selectedLine,
                                selectedStation = selectedStation,
                                loadLines = { ApiClient.rodaliesApiService.getRodaliesLines() },
                                loadStationsByLine = { ApiClient.rodaliesApiService.getRodaliesStationsByLine(it) },
                                loadStationRoutes = { ApiClient.rodaliesApiService.getRodaliesStationRoutes(it) },
                                onLineSelected = { selectedLine = it },
                                onStationSelected = { selectedStation = it },
                                loadingFavorite = loadingFavorite,
                                currentUserId = androidId
                            )
                            SearchOption.FGC -> TransportStationScreen(
                                selectedLine = selectedLine,
                                selectedStation = selectedStation,
                                loadLines = { ApiClient.fgcApiService.getFgcLines() },
                                loadStationsByLine = { ApiClient.fgcApiService.getFgcStationsByLine(it) },
                                loadStationRoutes = { ApiClient.fgcApiService.getFgcStationRoutes(it) },
                                onLineSelected = { selectedLine = it },
                                onStationSelected = { selectedStation = it },
                                loadingFavorite = loadingFavorite,
                                currentUserId = androidId
                            )
                            SearchOption.BUS -> BusLinesScreen(
                                selectedLine = selectedLine,
                                selectedStation = selectedStation,
                                loadLines = { ApiClient.busApiService.getBusLines() },
                                loadStationsByLine = { ApiClient.busApiService.getBusStopsByLine(it) },
                                loadStationRoutes = { ApiClient.busApiService.getBusStopRoutes(it) },
                                onLineSelected = { selectedLine = it },
                                onStationSelected = { selectedStation = it },
                                currentUserId = androidId
                            )
                            SearchOption.BICING -> PlaceholderScreen("Bicing")
                            null -> {}
                        }
                    }
                }
                BottomTab.FAVORITES -> FavoritesScreen(
                    currentUserId = androidId,
                    onFavoriteSelected = { fav ->
                        loadingFavorite = true
                        selectedTab = BottomTab.SEARCH
                        currentSearchScreen = when (fav.TYPE.lowercase()) {
                            "metro" -> SearchOption.METRO
                            "tram" -> SearchOption.TRAM
                            "rodalies" -> SearchOption.RODALIES
                            "fgc" -> SearchOption.FGC
                            "bus" -> SearchOption.BUS
                            else -> null
                        }

                        selectedStation = null
                        selectedLine = null

                        coroutineScope.launch {
                            try {
                                val lines = when (fav.TYPE.lowercase()) {
                                    "metro" -> ApiClient.metroApiService.getMetroLines()
                                    "tram" -> ApiClient.tramApiService.getTramLines()
                                    "rodalies" -> ApiClient.rodaliesApiService.getRodaliesLines()
                                    "fgc" -> ApiClient.fgcApiService.getFgcLines()
                                    "bus" -> ApiClient.busApiService.getBusLines()
                                    else -> emptyList()
                                }

                                val favLine = lines.find { it.code == fav.LINE_CODE }

                                val stations = when (fav.TYPE.lowercase()) {
                                    "metro" -> favLine?.code?.let { ApiClient.metroApiService.getMetroStationsByLine(it) } ?: emptyList()
                                    "tram" -> favLine?.code?.let { ApiClient.tramApiService.getTramStopsByLine(it) } ?: emptyList()
                                    "rodalies" -> favLine?.code?.let { ApiClient.rodaliesApiService.getRodaliesStationsByLine(it) } ?: emptyList()
                                    "fgc" -> favLine?.code?.let { ApiClient.fgcApiService.getFgcStationsByLine(it) } ?: emptyList()
                                    "bus" -> favLine?.code?.let { ApiClient.busApiService.getBusStopsByLine(it) } ?: emptyList()
                                    else -> emptyList()
                                }
                                selectedStation = stations.find { it.code == fav.STATION_CODE }
                                selectedLine = favLine

                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                loadingFavorite = false
                            }
                        }
                    }
                )
                BottomTab.USER -> PlaceholderScreen("Usuario")
            }
        }
    }
}
