package com.example.bcntransit

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import com.example.bcntransit.model.FavoriteDto
import com.example.bcntransit.ui.theme.BCNTransitTheme
import com.example.bcntransit.widget.BcnTransitWidgetProvider
import com.example.bcntransit.widget.WidgetConfigurationScreen

class WidgetConfigurationActivity : ComponentActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setResult(RESULT_CANCELED)

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
                WidgetConfigurationScreen(
                    onFavoriteSelected = { favorite ->
                        configurarWidget(favorite)
                    },
                    onCancel = {
                        finish()
                    }
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun configurarWidget(favorite: FavoriteDto) {
        val context = this

        // Guardamos la configuración
        val prefs = context.getSharedPreferences("widget_prefs", MODE_PRIVATE)
        prefs.edit().apply {
            putString("widget_${appWidgetId}_favorite_id", favorite.STATION_CODE)
            putString("widget_${appWidgetId}_station_name", favorite.STATION_NAME)
            putString("widget_${appWidgetId}_station_code", favorite.STATION_CODE)
            putString("widget_${appWidgetId}_line_name", favorite.LINE_NAME)
            putString("widget_${appWidgetId}_line_code", favorite.LINE_CODE)
            putString("widget_${appWidgetId}_type", favorite.TYPE)
            apply()
        }

        // Llamamos a la función unificada de actualización
        val appWidgetManager = AppWidgetManager.getInstance(context)
        BcnTransitWidgetProvider().refreshOrUpdateWidget(context, appWidgetManager, appWidgetId)

        // Devolvemos resultado
        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        setResult(RESULT_OK, resultValue)
        finish()
    }
}