package com.example.bcntransit.widget

import android.provider.Settings
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import com.example.bcntransit.BCNTransitApp.components.InlineErrorBanner
import com.example.bcntransit.R
import com.example.bcntransit.api.ApiClient
import com.example.bcntransit.model.FavoriteDto

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
                title = { Text("Selecciona un favorito") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, "Cancelar")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (loading) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(color = colorResource(R.color.medium_red)) }
                    }
            } else if (error != null) {
                item { InlineErrorBanner(message = error ?: "") }
            } else {
                items(favorites) { favorite ->
                    ListItem(
                        headlineContent = { Text(favorite.STATION_NAME) },
                        supportingContent = { Text("Línea ${favorite.LINE_NAME}") },
                        modifier = Modifier.clickable(
                            indication = LocalIndication.current,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = { onFavoriteSelected(favorite) }
                        )
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}