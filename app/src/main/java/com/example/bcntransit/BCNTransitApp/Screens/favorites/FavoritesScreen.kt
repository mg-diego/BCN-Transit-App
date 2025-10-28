package com.bcntransit.app.screens.favorites

import android.widget.Space
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bcntransit.app.R
import com.bcntransit.app.model.FavoriteDto
import com.bcntransit.app.api.ApiClient
import com.bcntransit.app.util.getAndroidId
import com.example.bcntransit.BCNTransitApp.Screens.favorites.FavoriteItem
import com.example.bcntransit.BCNTransitApp.components.CustomTopBar
import kotlinx.coroutines.launch

@Composable
fun FavoritesScreen(
    onFavoriteSelected: (FavoriteDto) -> Unit
) {

    val currentUserId = getAndroidId(LocalContext.current)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var favorites by remember { mutableStateOf<List<FavoriteDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentUserId) {
        loading = true
        error = null
        try {
            favorites = ApiClient.userApiService.getUserFavorites(currentUserId)
        } catch (e: Exception) {
            e.printStackTrace()
            error = e.message
        } finally {
            loading = false
        }
    }

    Scaffold(
        topBar = {
            CustomTopBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = Color.Unspecified
                        )
                        Text(
                            text = "Favoritos",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                },
                onBackClick = { },
                showBackButton = false
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            when {
                loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colorResource(R.color.medium_red))
                }
                error != null -> Text("Error: $error", color = Color.Red, modifier = Modifier.padding(16.dp))
                else -> LazyColumn(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    item {
                        Text(
                            text = "Aquí encontrarás tus estaciones y líneas favoritas para acceder rápidamente a sus horarios y rutas.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                    }
                    val groupedFavorites = favorites.groupBy { it.TYPE }
                    groupedFavorites.forEach { (type, favoritesOfType) ->
                        // Header del tipo
                        item {
                            Text(
                                text = type.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        // Items del tipo
                        itemsIndexed(
                            items = favoritesOfType,
                            key = { _, fav -> "${fav.TYPE}_${fav.STATION_CODE}" }
                        ) { _, fav ->
                            FavoriteItem(
                                fav = fav,
                                onClick = { onFavoriteSelected(fav) },
                                onDelete = {
                                    coroutineScope.launch {
                                        try {
                                            loading = true
                                            val deleted = ApiClient.userApiService.deleteUserFavorite(
                                                currentUserId,
                                                fav.TYPE,
                                                fav.STATION_CODE
                                            )
                                            if (deleted) {
                                                favorites = ApiClient.userApiService.getUserFavorites(currentUserId)
                                                snackbarHostState.showSnackbar("Favorito eliminado")
                                            } else snackbarHostState.showSnackbar("Error al eliminar")
                                        } catch (e: Exception) {
                                            snackbarHostState.showSnackbar("Error al eliminar")
                                        } finally { loading = false }
                                    }
                                }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}


