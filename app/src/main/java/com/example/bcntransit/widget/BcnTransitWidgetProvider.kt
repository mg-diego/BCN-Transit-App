package com.example.bcntransit.widget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.TypedValue
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import com.example.bcntransit.MainActivity
import com.example.bcntransit.R
import com.example.bcntransit.api.ApiClient
import com.example.bcntransit.data.enums.TransportType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import remainingTime
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class BcnTransitWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (widgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == "com.example.bcntransit.REFRESH_WIDGET") {
            val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, 0)
            if (widgetId != 0) {
                // 🚀 Lanza la actualización desde la API
                CoroutineScope(Dispatchers.IO).launch {
                    val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
                    val stationCode = prefs.getString("widget_${widgetId}_station_code", "")
                    val lineCode = prefs.getString("widget_${widgetId}_line_code", "")
                    val type = prefs.getString("widget_${widgetId}_type", "")

                    val routes = ApiClient.from(TransportType.from(type ?: "")).getStationRoutes(stationCode ?: "").let { list ->
                        if (type == TransportType.BUS.type) list
                        else list.filter { it.line_code == lineCode }
                    }

                    val direction_1_route_1 = routes.getOrNull(0)?.next_trips?.getOrNull(0)?.arrival_time?.let { remainingTime(it) } ?: "--"
                    val direction_1_route_2 = routes.getOrNull(0)?.next_trips?.getOrNull(1)?.arrival_time?.let { remainingTime(it) } ?: "--"
                    val direction_2_route_1 = routes.getOrNull(1)?.next_trips?.getOrNull(0)?.arrival_time?.let { remainingTime(it) } ?: "--"
                    val direction_2_route_2 = routes.getOrNull(1)?.next_trips?.getOrNull(1)?.arrival_time?.let { remainingTime(it) } ?: "--"

                    prefs.edit().apply {
                        putString("widget_${widgetId}_direction_1_route_1", direction_1_route_1)
                        putString("widget_${widgetId}_direction_1_route_2", direction_1_route_2)
                        putString("widget_${widgetId}_direction_2_route_1", direction_2_route_1)
                        putString("widget_${widgetId}_direction_2_route_2", direction_2_route_2)
                        apply()
                    }

                    // 🔄 Actualiza el widget en el hilo principal
                    withContext(Dispatchers.Main) {
                        val manager = AppWidgetManager.getInstance(context)
                        updateWidget(context, manager, widgetId)
                    }
                }
            }
        }
    }
    

    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        @SuppressLint("RemoteViewLayout")
        fun updateWidget(
            context: Context,
            manager: AppWidgetManager,
            widgetId: Int
        ) {
            val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)

            // Lee la configuración guardada
            val stationName = prefs.getString("widget_${widgetId}_station_name", "Sin configurar")
            val lineName = prefs.getString("widget_${widgetId}_line_name", "")
            val type = prefs.getString("widget_${widgetId}_type", "")

            val direction1 = prefs.getString("widget_${widgetId}_direction_1", "")
            val direction1_route1 = prefs.getString("widget_${widgetId}_direction_1_route_1", "")
            val direction1_route2 = prefs.getString("widget_${widgetId}_direction_1_route_2", "")

            val direction2 = prefs.getString("widget_${widgetId}_direction_2", "")
            val direction2_route1 = prefs.getString("widget_${widgetId}_direction_2_route_1", "")
            val direction2_route2 = prefs.getString("widget_${widgetId}_direction_2_route_2", "")

            val views = RemoteViews(context.packageName, R.layout.widget_bcn_transit)

            // Actualiza los datos del widget
            views.setTextViewText(R.id.txt_station_name, stationName)
            stationName?.length?.let { views.setTextViewTextSize(R.id.txt_station_name, TypedValue.COMPLEX_UNIT_SP,if(it > 10) 16f else 20f) }
            views.setTextViewText(R.id.txt_updated, "Actualizado: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss'h'"))}")

            val lineDrawableName = "${type}_${lineName?.lowercase()?.replace(" ", "_")}"
            val lineDrawableId = context.resources.getIdentifier(lineDrawableName, "drawable", context.packageName)
                    .takeIf { it != 0 } ?: context.resources.getIdentifier("$type", "drawable", context.packageName)
            views.setImageViewResource(R.id.img_line_id, lineDrawableId)

            // Aquí puedes obtener los tiempos reales desde tu API
            views.setTextViewText(R.id.txt_direction_name_1, direction1)
            views.setTextViewText(R.id.txt_time_d1_1, direction1_route1)
            views.setTextViewText(R.id.txt_time_d1_2, direction1_route2)

            views.setTextViewText(R.id.txt_direction_name_2, direction2)
            views.setTextViewText(R.id.txt_time_d2_1, direction2_route1)
            views.setTextViewText(R.id.txt_time_d2_2, direction2_route2)

            // 👇 Aquí el botón refresh dispara la acción REFRESH_WIDGET
            val refreshIntent = Intent(context, BcnTransitWidgetProvider::class.java).apply {
                action = "com.example.bcntransit.REFRESH_WIDGET"
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetId)
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context,
                widgetId,
                refreshIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.img_refresh, refreshPendingIntent)

            manager.updateAppWidget(widgetId, views)

            // Acción al pulsar el widget
            val intent = Intent(context, MainActivity::class.java)
            val pending = PendingIntent.getActivity(
                context, widgetId, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.txt_station_name, pending)

            manager.updateAppWidget(widgetId, views)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        for (widgetId in appWidgetIds) {
            editor.remove("widget_${widgetId}_favorite_id")
            editor.remove("widget_${widgetId}_station")
            editor.remove("widget_${widgetId}_line")
        }
        editor.apply()
    }
}