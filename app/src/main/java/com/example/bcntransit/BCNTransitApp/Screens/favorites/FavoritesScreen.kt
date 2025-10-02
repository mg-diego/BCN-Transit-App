package com.example.bcntransit.screens.favorites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.bcntransit.R
import com.example.bcntransit.model.FavoriteDto
import com.example.bcntransit.api.ApiClient
import com.example.bcntransit.screens.map.getDrawableIdByName
import com.example.bcntransit.util.getAndroidId
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Header()

            when {
                loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colorResource(R.color.medium_red))
                }
                error != null -> Text("Error: $error", color = Color.Red, modifier = Modifier.padding(16.dp))
                else -> LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(favorites, key = { _, fav -> fav.STATION_CODE }) { _, fav ->
                        FavoriteCard(
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
                }
            }
        }
    }
}

@Composable
private fun Header() {
    Box(Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(42.dp))
            Spacer(Modifier.width(16.dp))
            Text("Favoritos", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
fun FavoriteCard(
    fav: FavoriteDto,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    // Estado para mostrar u ocultar el diálogo
    var showDialog by remember { mutableStateOf(false) }

    // Diálogo de confirmación
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Eliminar favorito") },
            text = { Text("¿Seguro que deseas eliminar este favorito?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        onDelete()
                    }
                ) {
                    Text(
                        "Eliminar",
                        color = MaterialTheme.colorScheme.error,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(IntrinsicSize.Min), // ajusta la altura al contenido
            verticalAlignment = Alignment.CenterVertically
        ) {
            val context = LocalContext.current
            val drawableName = "${fav.TYPE}_${fav.LINE_NAME?.lowercase()?.replace(" ", "_")}"
            val drawableId = remember(fav.LINE_NAME) {
                context.resources.getIdentifier(drawableName, "drawable", context.packageName)
                    .takeIf { it != 0 } ?: getDrawableIdByName(context, fav.TYPE)
            }

            Icon(
                painter = painterResource(drawableId),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(42.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    fav.STATION_NAME,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${fav.TYPE.uppercase()} (${fav.STATION_CODE})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorResource(R.color.gray),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = { showDialog = true },
                modifier = Modifier.align(Alignment.CenterVertically) // asegura que el botón esté centrado
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Eliminar favorito",
                    tint = colorResource(R.color.red)
                )
            }
        }
    }
}


