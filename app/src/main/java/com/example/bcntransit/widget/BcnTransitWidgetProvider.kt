package com.example.bcntransit.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.bcntransit.MainActivity
import com.example.bcntransit.R

class BcnTransitWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Se llama cuando se debe actualizar el widget
        for (widgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    private fun updateWidget(
        context: Context,
        manager: AppWidgetManager,
        widgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_bcn_transit)

        // Ejemplo: datos estáticos o desde SharedPreferences/Room
        views.setTextViewText(R.id.txt_station, "Pl. Catalunya")
        views.setTextViewText(R.id.txt_status, "Próximo tren 5 min")

        // Acción al pulsar el widget
        val intent = Intent(context, MainActivity::class.java)
        val pending = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        views.setOnClickPendingIntent(R.id.txt_station, pending)

        manager.updateAppWidget(widgetId, views)
    }
}