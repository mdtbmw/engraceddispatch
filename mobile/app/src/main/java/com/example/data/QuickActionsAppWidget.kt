package com.example.data

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import java.text.NumberFormat
import java.util.Locale

class QuickActionsAppWidget : AppWidgetProvider() {

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
            val thisWidget = ComponentName(context, QuickActionsAppWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    companion object {
        const val ACTION_REFRESH_WIDGET = "com.example.ACTION_REFRESH_QUICK_WIDGET"

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.quick_actions_widget_layout)

            val prefs = context.getSharedPreferences("engraced_dispatch_prefs", Context.MODE_PRIVATE)
            val walletBalance = prefs.getFloat("widget_wallet_balance", 0.0f)

            // Format as Naira
            val formattedBalance = "₦" + String.format(Locale.US, "%,.2f", walletBalance)
            views.setTextViewText(R.id.quick_widget_wallet_balance, formattedBalance)

            // 1. Express booking intent
            val expressIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("shortcut_route", "ExpressBooking")
            }
            val expressPendingIntent = PendingIntent.getActivity(
                context,
                301,
                expressIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_action_express, expressPendingIntent)

            // 2. Batch booking intent
            val batchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("shortcut_route", "BatchBooking")
            }
            val batchPendingIntent = PendingIntent.getActivity(
                context,
                302,
                batchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_action_batch, batchPendingIntent)

            // 3. AI assistant intent
            val assistantIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("shortcut_route", "CustomerAssistant")
            }
            val assistantPendingIntent = PendingIntent.getActivity(
                context,
                303,
                assistantIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_action_assistant, assistantPendingIntent)

            // Click on background opens main app screen
            val mainIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val mainPendingIntent = PendingIntent.getActivity(
                context,
                304,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.quick_widget_background, mainPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun updateWidgetData(context: Context, walletBalance: Float) {
            val prefs = context.getSharedPreferences("engraced_dispatch_prefs", Context.MODE_PRIVATE)
            prefs.edit().putFloat("widget_wallet_balance", walletBalance).apply()

            // Trigger update broadcast
            val intent = Intent(context, QuickActionsAppWidget::class.java).apply {
                action = ACTION_REFRESH_WIDGET
            }
            context.sendBroadcast(intent)
        }
    }
}
