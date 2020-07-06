package com.pru.notes.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.pru.notes.R
import com.pru.notes.activities.SplashScreenActivity

class NotesWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
      val it: Iterator<Int>? =  appWidgetIds?.iterator()
        while (it != null && it.hasNext()){
            val intent = Intent(context,SplashScreenActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context,0,intent,0)
            val remoteViews = RemoteViews(context?.packageName, R.layout.widget_notes)
            remoteViews.setOnClickPendingIntent(R.id.add_note_widget,pendingIntent)
            appWidgetManager?.updateAppWidget(it.next(),remoteViews)
        }
    }
}