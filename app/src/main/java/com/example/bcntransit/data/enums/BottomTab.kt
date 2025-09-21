package com.example.bcntransit.data.enums

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomTab(val label: String, val icon: ImageVector) {
    MAP("Mapa", Icons.Default.Map),
    SEARCH("Líneas", Icons.Default.Route),
    FAVORITES("Favoritos", Icons.Filled.Star),
    USER("Usuario", Icons.Default.AccountCircle)
}
