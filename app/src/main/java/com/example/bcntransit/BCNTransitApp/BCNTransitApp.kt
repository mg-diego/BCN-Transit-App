    package com.example.bcntransit

    import androidx.compose.foundation.layout.padding
    import androidx.compose.material3.Scaffold
    import androidx.compose.runtime.*
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.platform.LocalContext
    import com.example.bcntransit.api.ApiClient
    import com.example.bcntransit.data.enums.BottomTab
    import com.example.bcntransit.model.transport.LineDto
    import com.example.bcntransit.model.transport.StationDto
    import com.example.bcntransit.screens.BottomNavigationBar
    import com.example.bcntransit.screens.PlaceholderScreen
    import com.example.bcntransit.screens.map.MapScreen
    import com.example.bcntransit.screens.search.BusLinesScreen
    import com.example.bcntransit.screens.search.SearchScreen
    import android.provider.Settings
    import androidx.navigation.NavType
    import androidx.navigation.compose.NavHost
    import androidx.navigation.compose.composable
    import androidx.navigation.compose.currentBackStackEntryAsState
    import androidx.navigation.compose.rememberNavController
    import androidx.navigation.navArgument
    import com.example.bcntransit.BCNTransitApp.Screens.navigation.Screen
    import com.example.bcntransit.data.enums.TransportType
    import com.example.bcntransit.screens.favorites.FavoritesScreen
    import com.example.bcntransit.screens.search.LineListScreen
    import com.example.bcntransit.screens.search.RoutesScreen

    import com.example.bcntransit.BCNTransitApp.Screens.navigation.Screen.Favorites.typeParam
    import com.example.bcntransit.BCNTransitApp.Screens.navigation.Screen.Favorites.lineCodeParam
    import com.example.bcntransit.BCNTransitApp.Screens.navigation.Screen.Favorites.stationCodeParam
    import com.example.bcntransit.screens.search.StationListScreen
    import com.example.bcntransit.screens.search.stations.BicingScreen

    @Composable
    fun BCNTransitApp() {
        val navController = rememberNavController()

        // Observa el destino actual del NavHost
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        // Deriva el BottomTab actual a partir de la ruta
        val currentBottomTab = when (currentRoute) {
            Screen.Map.route -> BottomTab.MAP
            Screen.Search.route -> BottomTab.SEARCH
            Screen.Favorites.route -> BottomTab.FAVORITES
            Screen.User.route -> BottomTab.USER
            else -> BottomTab.MAP
        }

        val context = LocalContext.current
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

        Scaffold(
            bottomBar = {
                BottomNavigationBar(
                    selectedTab = currentBottomTab,
                    onTabSelected = { tab ->
                        when (tab) {
                            BottomTab.MAP -> navController.navigate(Screen.Map.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = false
                                    saveState = false
                                }
                                restoreState = false
                                launchSingleTop = true
                            }
                            BottomTab.SEARCH -> navController.navigate(Screen.Search.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = false
                                    saveState = false
                                }
                                restoreState = false
                                launchSingleTop = true
                            }
                            BottomTab.FAVORITES -> navController.navigate(Screen.Favorites.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = false
                                    saveState = false
                                }
                                restoreState = false
                                launchSingleTop = true
                            }
                            BottomTab.USER -> navController.navigate(Screen.User.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = false
                                    saveState = false
                                }
                                restoreState = false
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Map.route,
                modifier = Modifier.padding(padding)
            ) {
                composable(Screen.Map.route) {
                    MapScreen(
                        onViewLine = { type: String, lineCode: String ->
                            navController.navigate(
                                Screen.SearchLine.viewLine(type, lineCode)
                            )
                        },
                        onViewStation = { type: String, lineCode: String, stationCode: String ->
                            navController.navigate(
                                Screen.SearchStation.viewRoutes(type, lineCode, stationCode)
                            )
                        }
                    )
                }

                composable(Screen.Search.route) {
                    SearchScreen(
                        onTypeSelected = { type ->
                            navController.navigate(
                                Screen.SearchType.viewType(type)
                            )
                        }
                    )
                }
                /** LISTA DE LÍNEAS */
                composable(
                    route = Screen.SearchType.route,
                    arguments = listOf(navArgument(typeParam) { type = NavType.StringType })
                ) { backStack ->
                    val typeArg = backStack.arguments?.getString(typeParam) ?: return@composable
                    val transportType = TransportType.from(typeArg)
                    if (transportType == TransportType.BUS) {
                        BusLinesScreen(onLineClick = { line: LineDto ->
                            navController.navigate(
                                Screen.SearchLine.viewLine(typeArg, line.code)
                            )
                        })
                    } else if (transportType == TransportType.BICING){
                        BicingScreen()
                    } else {
                        LineListScreen(
                            transportType = transportType,
                            apiService = ApiClient.from(transportType),
                            onLineClick = { line: LineDto ->
                                navController.navigate(
                                    Screen.SearchLine.viewLine(typeArg, line.code)
                                )
                            }
                        )
                    }

                }
                /** LISTA DE ESTACIONES */
                composable(
                    route = Screen.SearchLine.route,
                    arguments = listOf(
                        navArgument(typeParam) { type = NavType.StringType },
                        navArgument(lineCodeParam) { type = NavType.StringType }
                    )
                ) { backStack ->
                    val typeArg = backStack.arguments?.getString(typeParam) ?: return@composable
                    val lineCodeArg = backStack.arguments?.getString(lineCodeParam) ?: return@composable
                    val transportType = TransportType.from(typeArg)
                    StationListScreen (
                        lineCode = lineCodeArg,
                        transportType = TransportType.from(typeArg),
                        apiService = ApiClient.from(transportType),
                        currentUserId = androidId,
                        onStationClick = { station: StationDto ->
                            navController.navigate(
                                Screen.SearchStation.viewRoutes(typeArg, lineCodeArg, station.code)
                            )
                        }
                    )
                }
                /** RUTAS DE UNA ESTACIÓN */
                composable(
                    route = Screen.SearchStation.route,
                    arguments = listOf(
                        navArgument(typeParam) { type = NavType.StringType },
                        navArgument(lineCodeParam) { type = NavType.StringType },
                        navArgument(stationCodeParam) { type = NavType.StringType }
                    )
                ) { backStack ->
                    val typeArg = backStack.arguments?.getString(typeParam) ?: return@composable
                    val lineCodeArg = backStack.arguments?.getString(lineCodeParam) ?: return@composable
                    val stationCodeParam = backStack.arguments?.getString(stationCodeParam) ?: return@composable
                    val transportType = TransportType.from(typeArg)
                    RoutesScreen(
                        stationCode = stationCodeParam,
                        lineCode = lineCodeArg,
                        apiService = ApiClient.from(transportType),
                        currentUserId = androidId,
                        onConnectionClick = { stationCode: String, lineCode: String ->
                            navController.navigate(
                                Screen.SearchStation.viewRoutes(typeArg, lineCode, stationCode)
                            )
                        }
                    )
                }

                composable(Screen.Favorites.route) {
                    FavoritesScreen(
                        currentUserId = androidId,
                        onFavoriteSelected = { fav ->
                            navController.navigate(
                                Screen.SearchStation.viewRoutes(
                                    type = fav.TYPE,
                                    lineCode = fav.LINE_CODE,
                                    stationCode = fav.STATION_CODE
                                )
                            )
                        }
                    )
                }
                composable(Screen.User.route) {
                    PlaceholderScreen("Usuario")}
            }
        }
    }

