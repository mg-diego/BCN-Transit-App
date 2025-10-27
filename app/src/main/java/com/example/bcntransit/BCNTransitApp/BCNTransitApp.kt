    package com.bcntransit.app

    import androidx.compose.foundation.layout.padding
    import androidx.compose.material3.Scaffold
    import androidx.compose.runtime.*
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.platform.LocalContext
    import com.bcntransit.app.api.ApiClient
    import com.bcntransit.app.data.enums.BottomTab
    import com.bcntransit.app.model.transport.LineDto
    import com.bcntransit.app.model.transport.StationDto
    import com.bcntransit.app.screens.BottomNavigationBar
    import com.bcntransit.app.screens.map.MapScreen
    import com.bcntransit.app.screens.search.BusLinesScreen
    import com.bcntransit.app.screens.search.SearchScreen
    import androidx.lifecycle.viewmodel.compose.viewModel
    import androidx.navigation.NavType
    import androidx.navigation.compose.NavHost
    import androidx.navigation.compose.composable
    import androidx.navigation.compose.currentBackStackEntryAsState
    import androidx.navigation.compose.rememberNavController
    import androidx.navigation.navArgument
    import com.bcntransit.app.BCNTransitApp.Screens.navigation.Screen
    import com.bcntransit.app.data.enums.TransportType
    import com.bcntransit.app.screens.favorites.FavoritesScreen
    import com.bcntransit.app.screens.search.LineListScreen
    import com.bcntransit.app.screens.search.RoutesScreen

    import com.bcntransit.app.BCNTransitApp.Screens.navigation.Screen.Favorites.typeParam
    import com.bcntransit.app.BCNTransitApp.Screens.navigation.Screen.Favorites.lineCodeParam
    import com.bcntransit.app.BCNTransitApp.Screens.navigation.Screen.Favorites.stationCodeParam
    import com.bcntransit.app.screens.search.StationListScreen
    import com.bcntransit.app.screens.search.stations.BicingScreen
    import com.bcntransit.app.screens.settings.SettingsScreen
    import com.bcntransit.app.util.getAndroidId
    import com.bcntransit.app.widget.RegisterViewModel
    import com.example.bcntransit.BCNTransitApp.Screens.settings.AboutScreen
    import com.example.bcntransit.BCNTransitApp.Screens.settings.PrivacyPolicyScreen
    import com.example.bcntransit.BCNTransitApp.Screens.settings.TermsAndConditionsScreen

    @Composable
    fun BCNTransitApp() {
        val navController = rememberNavController()
        val registerViewModel: RegisterViewModel = viewModel()
        val currentUserId = getAndroidId(LocalContext.current)

        // Observa el destino actual del NavHost
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        // Deriva el BottomTab actual a partir de la ruta
        val currentBottomTab = when (currentRoute) {
            Screen.Map.route -> BottomTab.MAP
            Screen.Search.route -> BottomTab.SEARCH
            Screen.Favorites.route -> BottomTab.FAVORITES
            Screen.Settings.route -> BottomTab.SETTINGS
            else -> BottomTab.MAP
        }

        val showBottomBar = currentRoute in listOf(
            Screen.Map.route,
            Screen.Search.route,
            Screen.Favorites.route,
        )

        LaunchedEffect(Unit) {
            registerViewModel.registerUser(currentUserId)
        }

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
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

                                BottomTab.SETTINGS -> navController.navigate(Screen.Settings.route) {
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
                        BusLinesScreen(
                            onLineClick = { line: LineDto ->
                                navController.navigate(
                                    Screen.SearchLine.viewLine(typeArg, line.code)
                                )
                            },
                            onBackClick = { navController.popBackStack() } )
                    } else if (transportType == TransportType.BICING) {
                        BicingScreen()
                    } else {
                        LineListScreen(
                            transportType = transportType,
                            apiService = ApiClient.from(transportType),
                            onLineClick = { line: LineDto ->
                                navController.navigate(
                                    Screen.SearchLine.viewLine(typeArg, line.code)
                                )
                            },
                            onBackClick = { navController.popBackStack() }
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
                    val lineCodeArg =
                        backStack.arguments?.getString(lineCodeParam) ?: return@composable
                    val transportType = TransportType.from(typeArg)
                    StationListScreen(
                        lineCode = lineCodeArg,
                        transportType = TransportType.from(typeArg),
                        apiService = ApiClient.from(transportType),
                        onStationClick = { station: StationDto ->
                            navController.navigate(
                                Screen.SearchStation.viewRoutes(typeArg, lineCodeArg, station.code)
                            )
                        },
                        onBackClick = { navController.popBackStack() }
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
                    val lineCodeArg =
                        backStack.arguments?.getString(lineCodeParam) ?: return@composable
                    val stationCodeParam =
                        backStack.arguments?.getString(stationCodeParam) ?: return@composable
                    val transportType = TransportType.from(typeArg)
                    RoutesScreen(
                        stationCode = stationCodeParam,
                        lineCode = lineCodeArg,
                        apiService = ApiClient.from(transportType),
                        onConnectionClick = { stationCode: String, lineCode: String ->
                            navController.navigate(
                                Screen.SearchStation.viewRoutes(typeArg, lineCode, stationCode)
                            )
                        }
                    )
                }

                composable(Screen.Favorites.route) {
                    FavoritesScreen(
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
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        onBackClick = { navController.popBackStack() },
                        onNavigateToAbout = { navController.navigate(Screen.About.route) },
                        onNavigateToPrivacy = { navController.navigate(Screen.Privacy.route) },
                        onNavigateToTermsAndConditions = { navController.navigate(Screen.Terms.route) }
                    )
                }
                composable(Screen.About.route) {
                    AboutScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }
                composable(Screen.Privacy.route) {
                    PrivacyPolicyScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }
                composable(Screen.Terms.route) {
                    TermsAndConditionsScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }
    }


