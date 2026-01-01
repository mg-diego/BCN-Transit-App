package com.bcntransit.app.widget

import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bcntransit.app.BCNTransitApp.components.InlineErrorBanner
import com.bcntransit.app.R
import com.bcntransit.app.api.ApiClient
import com.bcntransit.app.data.enums.TransportType
import com.bcntransit.app.model.FavoriteDto
import com.bcntransit.app.screens.map.getDrawableIdByName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetConfigurationScreen(
    onFavoriteSelected: (FavoriteDto) -> Unit,
    onCancel: () -> Unit
) {
    var favorites by remember { mutableStateOf<List<FavoriteDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val currentUserId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

    val groupedFavorites = remember(favorites) {
        favorites
            .filter { it.TYPE.lowercase() != TransportType.BICING.type }
            .groupBy { it.TYPE.uppercase() }
    }

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
            TopAppBar(
                title = {
                    Column {
                        Text("Configurar Widget", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Selecciona uno de tus favoritos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, "Cancelar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colorResource(R.color.medium_red))
                }
            } else if (error != null) {
                Box(modifier = Modifier.padding(16.dp)) {
                    InlineErrorBanner(message = error!!)
                }
            } else if (favorites.isEmpty()) {
                // ESTADO VACÍO
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    groupedFavorites.forEach { (type, favoritesOfType) ->
                        stickyHeader {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = type,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }

                        items(favoritesOfType) { favorite ->
                            FavoriteWidgetRow(
                                favorite = favorite,
                                onClick = { onFavoriteSelected(favorite) }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 72.dp),
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteWidgetRow(
    favorite: FavoriteDto,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val type = favorite.TYPE.lowercase()
    val lineName = favorite.LINE_NAME?.lowercase()?.replace(" ", "_")

    val drawableName =
        "${type}_${lineName}"
    val drawableId = remember(favorite) {
        context.resources.getIdentifier(drawableName, "drawable", context.packageName)
            .takeIf { it != 0 } ?: getDrawableIdByName(context, type)
    }


    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        leadingContent = {
            // Icono grande y claro
            Icon(
                painter = painterResource(id = drawableId),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(40.dp)
            )
        },
        headlineContent = {
            Text(
                text = favorite.STATION_NAME,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        supportingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                favorite.STATION_CODE.ifEmpty { favorite.LINE_NAME }?.let {
                    Text(
                        text = "(" + it + ")",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        trailingContent = {
            // Indicador visual de selección (flechita o radio)
            // En este caso, nada o un chevron queda bien, pero limpio es mejor.
        }
    )
}
