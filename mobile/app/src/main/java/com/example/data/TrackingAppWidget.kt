package com.example.data

import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R

class TrackingAppWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH_WIDGET) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, TrackingAppWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    companion object {
        const val ACTION_REFRESH_WIDGET = "com.example.ACTION_REFRESH_WIDGET"

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.tracking_widget_layout)

            val prefs = context.getSharedPreferences("engraced_dispatch_prefs", Context.MODE_PRIVATE)
            val parcelId = prefs.getString("widget_parcel_id", null)
            val status = prefs.getString("widget_status", "No active bookings in transit.")
            val progress = prefs.getInt("widget_progress", 0)

            if (parcelId != null) {
                views.setTextViewText(R.id.widget_parcel_id, "Parcel #$parcelId")
                views.setTextViewText(R.id.widget_status, status)
                views.setProgressBar(R.id.widget_progress, 100, progress, false)
                views.setViewVisibility(R.id.widget_progress, android.view.View.VISIBLE)
            } else {
                views.setTextViewText(R.id.widget_parcel_id, "No Active Shipments")
                views.setTextViewText(R.id.widget_status, "Premium Dispatch & Logistics client active.")
                views.setViewVisibility(R.id.widget_progress, android.view.View.GONE)
            }

            // Book Dispatch PendingIntent
            val bookIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("action", "book")
            }
            val bookPendingIntent = PendingIntent.getActivity(
                context,
                200,
                bookIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_button_book, bookPendingIntent)

            // Click on background opens app main screen
            val mainIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val mainPendingIntent = PendingIntent.getActivity(
                context,
                201,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_background, mainPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun updateWidgetData(context: Context, parcelId: String?, statusText: String, progressPercent: Int) {
            val prefs = context.getSharedPreferences("engraced_dispatch_prefs", Context.MODE_PRIVATE)
            prefs.edit().apply {
                if (parcelId == null) {
                    remove("widget_parcel_id")
                    remove("widget_status")
                    remove("widget_progress")
                } else {
                    putString("widget_parcel_id", parcelId)
                    putString("widget_status", statusText)
                    putInt("widget_progress", progressPercent)
                }
                apply()
            }

            // Trigger actual update of all widgets on home screen
            val intent = Intent(context, TrackingAppWidget::class.java).apply {
                action = ACTION_REFRESH_WIDGET
            }
            context.sendBroadcast(intent)
        }
    }
}
