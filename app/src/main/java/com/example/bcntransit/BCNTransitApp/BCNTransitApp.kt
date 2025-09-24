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
import com.example.bcntransit.data.enums.TransportType
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
    var isLoading by remember { mutableStateOf(false) }

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
                BottomTab.MAP -> MapScreen(
                    context,
                    onStationSelected = { station, lineCode ->
                        isLoading = true
                        selectedStation = null
                        selectedLine = null

                        currentSearchScreen = when (station.transport_type.lowercase()) {
                            "metro" -> SearchOption.METRO
                            "tram" -> SearchOption.TRAM
                            "rodalies" -> SearchOption.RODALIES
                            "fgc" -> SearchOption.FGC
                            "bus" -> SearchOption.BUS
                            else -> null
                        }

                        coroutineScope.launch {
                            try {
                                val apiService = when (station.transport_type.lowercase()) {
                                    "metro" -> ApiClient.metroApiService
                                    "tram" -> ApiClient.tramApiService
                                    "rodalies" -> ApiClient.rodaliesApiService
                                    "fgc" -> ApiClient.fgcApiService
                                    "bus" -> ApiClient.busApiService
                                    else -> ApiClient.metroApiService
                                }

                                selectedLine = apiService.getLines().find { it.code == lineCode }
                                selectedStation = apiService.getStationsByLine(selectedLine?.code ?: "").find { it.name == station.name }

                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                isLoading = false
                                selectedTab = BottomTab.SEARCH
                            }
                        }
                    },
                    onLineSelected = { line ->
                        isLoading = true
                        selectedStation = null
                        selectedLine = null

                        currentSearchScreen = when (line.transport_type.lowercase()) {
                            "metro" -> SearchOption.METRO
                            "tram" -> SearchOption.TRAM
                            "rodalies" -> SearchOption.RODALIES
                            "fgc" -> SearchOption.FGC
                            "bus" -> SearchOption.BUS
                            else -> null
                        }

                        coroutineScope.launch {
                            try {
                                val apiService = when (line.transport_type.lowercase()) {
                                    "metro" -> ApiClient.metroApiService
                                    "tram" -> ApiClient.tramApiService
                                    "rodalies" -> ApiClient.rodaliesApiService
                                    "fgc" -> ApiClient.fgcApiService
                                    "bus" -> ApiClient.busApiService
                                    else -> ApiClient.metroApiService
                                }

                                selectedLine = apiService.getLines().find { it.code == line.code }

                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                isLoading = false
                                selectedTab = BottomTab.SEARCH
                            }
                        }
                    }
                )
                BottomTab.SEARCH -> {
                    if (currentSearchScreen == null) {
                        SearchScreen { currentSearchScreen = it }
                    } else {
                        when (currentSearchScreen) {
                            SearchOption.METRO -> TransportStationScreen(
                                transportType = TransportType.METRO,
                                selectedLine = selectedLine,
                                selectedStation = selectedStation,
                                apiService = ApiClient.metroApiService,
                                onLineSelected = { selectedLine = it },
                                onStationSelected = { selectedStation = it },
                                isLoading = isLoading,
                                currentUserId = androidId
                            )
                            SearchOption.TRAM -> TransportStationScreen(
                                transportType = TransportType.TRAM,
                                selectedLine = selectedLine,
                                selectedStation = selectedStation,
                                apiService = ApiClient.tramApiService,
                                onLineSelected = { selectedLine = it },
                                onStationSelected = { selectedStation = it },
                                isLoading = isLoading,
                                currentUserId = androidId
                            )
                            SearchOption.RODALIES -> TransportStationScreen(
                                transportType = TransportType.RODALIES,
                                selectedLine = selectedLine,
                                selectedStation = selectedStation,
                                apiService = ApiClient.rodaliesApiService,
                                onLineSelected = { selectedLine = it },
                                onStationSelected = { selectedStation = it },
                                isLoading = isLoading,
                                currentUserId = androidId
                            )
                            SearchOption.FGC -> TransportStationScreen(
                                transportType = TransportType.FGC,
                                selectedLine = selectedLine,
                                selectedStation = selectedStation,
                                apiService = ApiClient.fgcApiService,
                                onLineSelected = { selectedLine = it },
                                onStationSelected = { selectedStation = it },
                                isLoading = isLoading,
                                currentUserId = androidId
                            )
                            SearchOption.BUS -> BusLinesScreen(
                                selectedLine = selectedLine,
                                selectedStation = selectedStation,
                                apiService = ApiClient.busApiService,
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
                        isLoading = true
                        selectedStation = null
                        selectedLine = null

                        currentSearchScreen = when (fav.TYPE.lowercase()) {
                            "metro" -> SearchOption.METRO
                            "tram" -> SearchOption.TRAM
                            "rodalies" -> SearchOption.RODALIES
                            "fgc" -> SearchOption.FGC
                            "bus" -> SearchOption.BUS
                            else -> null
                        }

                        coroutineScope.launch {
                            try {
                                val apiService = when (fav.TYPE.lowercase()) {
                                    "metro" -> ApiClient.metroApiService
                                    "tram" -> ApiClient.tramApiService
                                    "rodalies" -> ApiClient.rodaliesApiService
                                    "fgc" -> ApiClient.fgcApiService
                                    "bus" -> ApiClient.busApiService
                                    else -> ApiClient.metroApiService
                                }

                                selectedLine = apiService.getLines().find { it.code == fav.LINE_CODE }
                                selectedStation = apiService.getStationsByLine(selectedLine?.code ?: "").find { it.code == fav.STATION_CODE }

                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                isLoading = false
                                selectedTab = BottomTab.SEARCH
                            }
                        }
                    }
                )
                BottomTab.USER -> PlaceholderScreen("Usuario")
            }
        }
    }
}
