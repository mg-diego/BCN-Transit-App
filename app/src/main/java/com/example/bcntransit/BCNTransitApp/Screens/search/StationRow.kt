package com.example.bcntransit.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.example.bcntransit.R
import com.example.bcntransit.api.ApiClient
import com.example.bcntransit.model.StationDto
import com.example.bcntransit.model.FavoriteDto
import kotlinx.coroutines.launch

@Composable
fun StationRow(
    station: StationDto,
    isFirst: Boolean,
    isLast: Boolean,
    lineColor: Color,
    lineType: String,
    currentUserId: String,
    onClick: () -> Unit
) {
    val circleSize = 20.dp
    val lineWidth = 4.dp
    val rowHeight = 70.dp
    val halfCircle = circleSize / 2

    var isFavorite by remember { mutableStateOf(false) }
    var isLoadingFavorite by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    LaunchedEffect(station.code, currentUserId) {
        try {
            isFavorite = ApiClient.userApiService.userHasFavorite(
                userId = currentUserId,
                type = lineType,
                itemId = station.code
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(rowHeight)
            .clickable { onClick() }
            .padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(32.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(lineWidth)
                    .fillMaxHeight()
                    .padding(
                        top = if (isFirst) halfCircle else 0.dp,
                        bottom = if (isLast) halfCircle else 0.dp
                    )
                    .background(lineColor)
                    .align(Alignment.Center)
            )
            Box(
                modifier = Modifier
                    .size(circleSize)
                    .background(lineColor, CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            val alertText = if (station.has_alerts) "Incidencias" else "Servicio normal"
            val alertColor = if (station.has_alerts) colorResource(R.color.medium_red) else colorResource(R.color.dark_green)

            Text(
                text = station.name_with_emoji ?: station.name,
                style = MaterialTheme.typography.titleMedium
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(alertColor, shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(alertText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Este IconButton quedará pegado al borde derecho
        IconButton(
            onClick = {
                scope.launch {
                    if (isFavorite) {
                        try {
                            isLoadingFavorite = true
                            ApiClient.userApiService.deleteUserFavorite(currentUserId, lineType, station.code)
                            isFavorite = false
                        } catch(e: Exception) {
                            e.printStackTrace()
                        } finally {
                            isLoadingFavorite = false
                        }
                    } else {
                        try {
                            isLoadingFavorite = true
                            ApiClient.userApiService.addUserFavorite(
                                currentUserId,
                                favorite = FavoriteDto(
                                        USER_ID = currentUserId,
                                        TYPE = lineType,
                                        LINE_CODE = station.line_code,
                                        LINE_NAME = station.line_name,
                                        LINE_NAME_WITH_EMOJI = station.line_name_with_emoji ?: "",
                                        STATION_CODE = station.code,
                                        STATION_NAME = station.name,
                                        STATION_GROUP_CODE = station.CODI_GRUP_ESTACIO.toString(),
                                        coordinates = listOf(station.latitude, station.longitude)
                                    )
                                )
                            isFavorite = true
                        } catch(e: Exception) {
                            e.printStackTrace()
                        } finally {
                            isLoadingFavorite = false
                        }
                    }
                }
            }
        ) {
            if (isLoadingFavorite) {
                CircularProgressIndicator()
            } else {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Favorito",
                    tint = colorResource(R.color.red)
                )
            }
        }
    }
}


