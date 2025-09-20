package com.example.bcntransit.screens.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.bcntransit.model.FavoriteDto
import com.example.bcntransit.R
import com.example.bcntransit.data.enums.CustomColors

@Composable
fun FavoritesScreen(
    favorites: List<FavoriteDto>,
    loading: Boolean,
    error: String?,
    onFavoriteSelected: (FavoriteDto) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {

        // Cabecera con icono y sombra inferior
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
                    painter = painterResource(R.drawable.metro), // tu drawable de favoritos
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
            ) {
                CircularProgressIndicator()
            }

            error != null -> Text(
                text = "Error: $error",
                color = Color.Red,
                modifier = Modifier.padding(16.dp)
            )

            else -> LazyColumn(
                modifier = Modifier.padding(
                    top = 8.dp, start = 16.dp, end = 16.dp, bottom = 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(favorites) { fav ->
                    FavoriteCard(fav) {
                        onFavoriteSelected(fav)
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteCard(fav: FavoriteDto, onClick: () -> Unit ) {
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
                "${fav.type}_${fav.line_name?.lowercase()?.replace(" ", "_")}"
            val drawableId = remember(fav.line_name) {
                context.resources.getIdentifier(drawableName, "drawable", context.packageName)
                    .takeIf { it != 0 } ?: context.resources.getIdentifier(fav.type, "drawable", context.packageName)
            }

            Icon(
                painter = painterResource(drawableId),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(42.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(fav.station_name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${fav.type.uppercase()} (${fav.station_code})", style = MaterialTheme.typography.bodyMedium, color = CustomColors.GRAY.color)
                }
            }
        }
    }
}