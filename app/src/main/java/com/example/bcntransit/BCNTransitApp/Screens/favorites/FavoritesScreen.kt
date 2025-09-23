package com.example.bcntransit.screens.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
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
    val snackbarHostState = remember { SnackbarHostState() }

    var favorites by remember { mutableStateOf<List<FavoriteDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var deletingFavorite by remember { mutableStateOf(false) }
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
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    containerColor = Color(0xFF323232),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    action = {
                        data.visuals.actionLabel?.let { actionLabel ->
                            TextButton(onClick = { data.performAction() }) {
                                Text(actionLabel, color = Color.Yellow)
                            }
                        }
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color.Green,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(data.visuals.message)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
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
                        imageVector = Icons.Filled.Star,
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
                                            type = fav.TYPE,
                                            itemId = fav.STATION_CODE
                                        )
                                        if (deleted) {
                                            favorites = ApiClient.userApiService.getUserFavorites(currentUserId)
                                            snackbarHostState.showSnackbar("Favorito eliminado")
                                        } else {
                                            snackbarHostState.showSnackbar("Error al eliminar favorito")
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        snackbarHostState.showSnackbar("Error al eliminar favorito")
                                    } finally {
                                        loading = false
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
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
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
            val drawableName = "${fav.TYPE}_${fav.LINE_NAME?.lowercase()?.replace(" ", "_")}"
            val drawableId = remember(fav.LINE_NAME) {
                context.resources.getIdentifier(drawableName, "drawable", context.packageName) .takeIf { it != 0 }
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
                Text( fav.STATION_NAME, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text( "${fav.TYPE.uppercase()} (${fav.STATION_CODE})", style = MaterialTheme.typography.bodyMedium, color = CustomColors.GRAY.color )
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = { showDialog = true }   // ← Abre el diálogo en lugar de borrar directo
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Eliminar favorito",
                    tint = CustomColors.RED.color
                )
            }
        }
    }
}
