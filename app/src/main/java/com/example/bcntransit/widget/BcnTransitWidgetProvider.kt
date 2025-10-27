package com.bcntransit.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import com.bcntransit.app.R
import com.bcntransit.app.WidgetConfigurationActivity
import com.bcntransit.app.api.ApiClient
import com.bcntransit.app.data.enums.TransportType
import com.bcntransit.app.utils.formatArrivalTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class BcnTransitWidgetProvider : AppWidgetProvider() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (widgetId in appWidgetIds) {
            refreshOrUpdateWidget(context, appWidgetManager, widgetId)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "com.bcntransit.app.REFRESH_WIDGET") {
            val widgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)
            widgetIds?.forEach { widgetId ->
                refreshOrUpdateWidget(context, AppWidgetManager.getInstance(context), widgetId)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshOrUpdateWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
        val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)

        // Mostramos "actualizando"
        val loadingViews = RemoteViews(context.packageName, R.layout.widget_bcn_transit).apply {
            setViewVisibility(R.id.img_refresh, View.GONE)
            setViewVisibility(R.id.list_routes, View.GONE)
            setViewVisibility(R.id.txt_empty, View.VISIBLE)
            setTextViewText(R.id.txt_empty, "Actualizando rutas...")
            setViewVisibility(R.id.progressBar, View.VISIBLE)
        }
        manager.updateAppWidget(widgetId, loadingViews)

        CoroutineScope(Dispatchers.IO).launch {
            val stationCode = prefs.getString("widget_${widgetId}_station_code", "")
            val lineCode = prefs.getString("widget_${widgetId}_line_code", "")
            val type = prefs.getString("widget_${widgetId}_type", "")

            try {
                val routes = ApiClient.from(TransportType.from(type ?: ""))
                    .getStationRoutes(stationCode ?: "")
                    .let { list -> if (type == TransportType.BUS.type) list else list.filter { it.line_code == lineCode } }

                // Guardamos rutas dinámicas en SharedPreferences
                prefs.edit().apply {
                    routes.forEachIndexed { index, route ->
                        putString("widget_${widgetId}_route_${index}_destination", route.destination)
                        putString("widget_${widgetId}_route_${index}_line_name", route.line_name)
                        putString(
                            "widget_${widgetId}_route_${index}_times",
                            route.next_trips.joinToString(",") { formatArrivalTime(it.arrival_time).text }
                        )
                    }
                    putInt("widget_${widgetId}_route_count", routes.size)
                    apply()
                }
            } catch (e: Exception) {
                // opcional: manejar error
            }

            withContext(Dispatchers.Main) {
                // Actualizamos widget completo
                val views = RemoteViews(context.packageName, R.layout.widget_bcn_transit)

                // Configuramos ListView
                val serviceIntent = Intent(context, RoutesWidgetService::class.java).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                    data = android.net.Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
                }
                views.setRemoteAdapter(R.id.list_routes, serviceIntent)
                views.setEmptyView(R.id.list_routes, R.id.txt_empty)

                // Textos principales
                val stationName = prefs.getString("widget_${widgetId}_station_name", "Sin configurar")
                views.setTextViewText(R.id.txt_station_name, stationName)
                views.setTextViewText(
                    R.id.txt_updated,
                    "Actualizado: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss'h'"))}"
                )
                stationName?.let {
                    views.setTextViewTextSize(
                        R.id.txt_station_name,
                        TypedValue.COMPLEX_UNIT_SP,
                        if (it.length > 20) 16f else 20f
                    )
                }

                // Icono tipo transporte
                val typeDrawableName = type?.lowercase()?.replace(" ", "_")
                val typeDrawableId = context.resources.getIdentifier(
                    typeDrawableName, "drawable", context.packageName
                ).takeIf { it != 0 } ?: context.resources.getIdentifier(
                    "$type", "drawable", context.packageName
                )
                views.setImageViewResource(R.id.img_type, typeDrawableId)

                // Botón refresh
                val refreshIntent = Intent(context, BcnTransitWidgetProvider::class.java).apply {
                    action = "com.bcntransit.app.REFRESH_WIDGET"
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(widgetId))
                }
                val refreshPendingIntent = PendingIntent.getBroadcast(
                    context,
                    widgetId,
                    refreshIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                views.setOnClickPendingIntent(R.id.img_refresh, refreshPendingIntent)

                // Botón configuración
                val configIntent = Intent(context, WidgetConfigurationActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                    data = android.net.Uri.parse("widget://configure/$widgetId")
                }
                val configPending = PendingIntent.getActivity(
                    context, widgetId, configIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.txt_station_name, configPending)

                // Visibilidad final
                views.setViewVisibility(R.id.img_refresh, View.VISIBLE)
                views.setViewVisibility(R.id.list_routes, View.VISIBLE)
                views.setViewVisibility(R.id.txt_empty, View.GONE)
                views.setViewVisibility(R.id.progressBar, View.GONE)

                manager.updateAppWidget(widgetId, views)
                manager.notifyAppWidgetViewDataChanged(widgetId, R.id.list_routes)
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            appWidgetIds.forEach { widgetId ->
                remove("widget_${widgetId}_favorite_id")
                remove("widget_${widgetId}_station")
                remove("widget_${widgetId}_line")
            }
            apply()
        }
    }
}
