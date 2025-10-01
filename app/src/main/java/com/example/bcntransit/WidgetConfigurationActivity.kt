package com.example.bcntransit

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.bcntransit.api.ApiClient
import com.example.bcntransit.data.enums.TransportType
import com.example.bcntransit.model.FavoriteDto
import com.example.bcntransit.ui.theme.BCNTransitTheme
import com.example.bcntransit.widget.BcnTransitWidgetProvider
import com.example.bcntransit.widget.WidgetConfigurationScreen
import kotlinx.coroutines.launch
import remainingTime

class WidgetConfigurationActivity : ComponentActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setResult(Activity.RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            BCNTransitTheme {
                val scope = rememberCoroutineScope()
                WidgetConfigurationScreen(
                    onFavoriteSelected = { favorite ->
                        scope.launch {
                            configurarWidget(favorite)
                        }
                    },
                    onCancel = {
                        finish()
                    }
                )
            }
        }
    }

    private suspend fun configurarWidget(favorite: FavoriteDto) {
        val context = this

        val apiService = ApiClient.from(TransportType.from(favorite.TYPE))

        val routes = apiService.getStationRoutes(favorite.STATION_CODE).let { list ->
            if (favorite.TYPE == TransportType.BUS.type) list
            else list.filter { it.line_code == favorite.LINE_CODE }
        }

        val direction_1_route_1 = routes.getOrNull(0)?.next_trips?.getOrNull(0)?.arrival_time?.let { remainingTime(it) } ?: "--"
        val direction_1_route_2 = routes.getOrNull(0)?.next_trips?.getOrNull(1)?.arrival_time?.let { remainingTime(it) } ?: "--"
        val direction_2_route_1 = routes.getOrNull(1)?.next_trips?.getOrNull(0)?.arrival_time?.let { remainingTime(it) } ?: "--"
        val direction_2_route_2 = routes.getOrNull(1)?.next_trips?.getOrNull(1)?.arrival_time?.let { remainingTime(it) } ?: "--"

        val prefs = context.getSharedPreferences("widget_prefs", MODE_PRIVATE)
        prefs.edit().apply {
            putString("widget_${appWidgetId}_favorite_id", favorite.STATION_CODE)
            putString("widget_${appWidgetId}_station_name", favorite.STATION_NAME)
            putString("widget_${appWidgetId}_station_code", favorite.STATION_CODE)
            putString("widget_${appWidgetId}_line_name", favorite.LINE_NAME)
            putString("widget_${appWidgetId}_line_code", favorite.LINE_CODE)
            putString("widget_${appWidgetId}_type", favorite.TYPE)
            putString("widget_${appWidgetId}_direction_1", routes[0].destination)
            putString("widget_${appWidgetId}_direction_1_route_1", direction_1_route_1)
            putString("widget_${appWidgetId}_direction_1_route_2", direction_1_route_2)
            putString("widget_${appWidgetId}_direction_2", routes[1].destination)
            putString("widget_${appWidgetId}_direction_2_route_1", direction_2_route_1)
            putString("widget_${appWidgetId}_direction_2_route_2", direction_2_route_2)
            apply()
        }

        val appWidgetManager = AppWidgetManager.getInstance(context)
        BcnTransitWidgetProvider.updateWidget(context, appWidgetManager, appWidgetId)

        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }
}


data class Favorite(
    val id: String,
    val stationName: String,
    val line: String
)