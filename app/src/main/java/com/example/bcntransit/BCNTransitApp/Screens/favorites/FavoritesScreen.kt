package com.example.bcntransit.screens.favorites

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.bcntransit.R
import com.example.bcntransit.data.enums.CustomColors
import com.example.bcntransit.model.FavoriteDto
import com.example.bcntransit.api.ApiClient
import kotlinx.coroutines.launch

@Composable
fun FavoritesScreen(
    currentUserId: String,
    onFavoriteSelected: (FavoriteDto) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
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

    Column(modifier = Modifier.fillMaxSize()) {
        // Cabecera
        Box(Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.metro),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(42.dp)
                )
                Spacer(Modifier.width(16.dp))
                Text("Favoritos", style = MaterialTheme.typography.titleLarge)
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        when {
            loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            error != null -> Text(
                text = "Error: $error",
                color = Color.Red,
                modifier = Modifier.padding(16.dp)
            )

            else -> LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(favorites, key = { _, fav -> fav.STATION_CODE }) { index, fav ->
                    var visible by remember { mutableStateOf(true) }

                    AnimatedVisibility(
                        visible = visible,
                        exit = fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                            targetOffsetX = { it }, animationSpec = tween(300)
                        )
                    ) {
                        FavoriteCard(
                            fav,
                            onClick = { onFavoriteSelected(fav) },
                            onDelete = {
                                // Animar primero
                                visible = false
                                // Esperar un poco para la animación
                                coroutineScope.launch {
                                    kotlinx.coroutines.delay(300)
                                    try {
                                        ApiClient.userApiService.deleteUserFavorite(
                                            currentUserId,
                                            type = fav.TYPE,
                                            itemId = fav.STATION_CODE
                                        )
                                        favorites = ApiClient.userApiService.getUserFavorites(currentUserId)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteCard(
    fav: FavoriteDto,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val context = LocalContext.current
            val drawableName =
                "${fav.TYPE}_${fav.LINE_NAME?.lowercase()?.replace(" ", "_")}"
            val drawableId = remember(fav.LINE_NAME) {
                context.resources.getIdentifier(drawableName, "drawable", context.packageName)
                    .takeIf { it != 0 }
                    ?: context.resources.getIdentifier(fav.TYPE, "drawable", context.packageName)
            }

            Icon(
                painter = painterResource(drawableId),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(42.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    fav.STATION_NAME,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${fav.TYPE.uppercase()} (${fav.STATION_CODE})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CustomColors.GRAY.color
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column {
                IconButton(onClick = { onDelete() }) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Eliminar favorito",
                        tint = CustomColors.RED.color
                    )
                }
            }
        }
    }
}
