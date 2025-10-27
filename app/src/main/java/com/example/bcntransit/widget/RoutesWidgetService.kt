package com.bcntransit.app.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.bcntransit.app.R
import com.bcntransit.app.api.ApiClient
import com.bcntransit.app.data.enums.TransportType
import com.bcntransit.app.model.transport.RouteDto
import com.bcntransit.app.utils.formatArrivalTime
import kotlinx.coroutines.runBlocking

class RoutesWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return RoutesFactory(applicationContext, intent)
    }
}

class RoutesFactory(private val context: Context, intent: Intent) : RemoteViewsService.RemoteViewsFactory {
    private val widgetId: Int = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)

    private val routes = mutableListOf<RouteDto>()

    override fun onCreate() {}

    override fun onDataSetChanged() {
        val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        val stationCode = prefs.getString("widget_${widgetId}_station_code", "")
        val lineCode = prefs.getString("widget_${widgetId}_line_code", "")
        val type = prefs.getString("widget_${widgetId}_type", "")
        routes.clear()
        runBlocking {
            val apiRoutes = ApiClient.from(TransportType.from(type ?: ""))
                .getStationRoutes(stationCode ?: "")
                .let { list -> if (type == TransportType.BUS.type) list else list.filter { it.line_code == lineCode } }

            routes.addAll(apiRoutes)
        }
    }

    override fun getCount() = routes.size

    override fun getViewAt(position: Int): RemoteViews {
        val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        val type = prefs.getString("widget_${widgetId}_type", "")

        val route = routes[position]
        return RemoteViews(context.packageName, R.layout.widget_route_item).apply {
            val lineDrawableName = "${type}_${route.line_name.lowercase().replace(" ", "_")}"
            val lineDrawableId = context.resources.getIdentifier(lineDrawableName, "drawable", context.packageName)

            setImageViewResource(R.id.img_line_id, lineDrawableId)
            setTextViewText(R.id.txt_direction_name, route.destination)
            setTextViewText(R.id.txt_time_1, route.next_trips.getOrNull(0)?.arrival_time?.let { formatArrivalTime(it).text } ?: "--")
            setTextViewText(R.id.txt_time_2, route.next_trips.getOrNull(1)?.arrival_time?.let { formatArrivalTime(it).text } ?: "--")
        }
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount() = 1
    override fun getItemId(position: Int) = position.toLong()
    override fun hasStableIds() = true
    override fun onDestroy() { routes.clear() }
}
