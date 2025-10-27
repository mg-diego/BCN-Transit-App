package com.bcntransit.app.BCNTransitApp.Screens.navigation

import com.bcntransit.app.BCNTransitApp.Screens.navigation.Screen.Favorites.lineCodeParam
import com.bcntransit.app.BCNTransitApp.Screens.navigation.Screen.Favorites.stationCodeParam
import com.bcntransit.app.BCNTransitApp.Screens.navigation.Screen.Favorites.typeParam

sealed class Screen(val route: String) {
    object Map : Screen("map")
    object Favorites : Screen("favorites")
    object Settings : Screen("settings")
    object About : Screen("about")

    object Privacy : Screen("privacy")

    object Terms : Screen("terms")

    val typeParam: String = "type"
    val lineCodeParam: String = "lineCode"
    val stationCodeParam: String = "stationCode"

    object Search : Screen("search")
    object SearchType : Screen("search/{$typeParam}") {
        fun viewType(type: String) = "search/$type"
    }
    object SearchLine : Screen("search/{$typeParam}/{$lineCodeParam}") {
        fun viewLine(type: String, lineCode: String) = "search/$type/$lineCode"
    }
    object SearchStation : Screen("search/{$typeParam}/{$lineCodeParam}/{$stationCodeParam}") {
        fun viewRoutes(type: String, lineCode: String, stationCode: String) =
            "search/$type/$lineCode/$stationCode"
    }
}