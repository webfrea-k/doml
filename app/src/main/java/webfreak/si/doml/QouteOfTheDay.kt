package webfreak.si.doml

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import android.app.PendingIntent
import android.content.Intent

/**
 * Implementation of App Widget functionality.
 */
class QouteOfTheDay : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    companion object {
        internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val prefs = PreferenceHelper.defaultPrefs(context)
            val daysAlive = Static.getDaysAlive(context)
            val queue = Volley.newRequestQueue(context)
            val currentQuote = prefs.getString(Const.DAILY_QUOTE,"0| ")
            val oldDaysAlive = currentQuote?.split("|")?.first()
            val ord = ordinal(daysAlive)
            val widgetText =  java.lang.String.format(context.getString(R.string.widget_title), ord)
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

            val url = "https://us-central1-days-of-my-life-57a3c.cloudfunctions.net/getQuote?daysAlive=$daysAlive"
            val stringReq = StringRequest(
                Request.Method.GET, url,
                Response.Listener<String> { response ->
                    val strResp = response.toString()
                    prefs.edit().putString(Const.DAILY_QUOTE, "$daysAlive|$strResp").apply()
                    val views = RemoteViews(context.packageName, R.layout.widget_layout)
                    views.setOnClickPendingIntent(R.id.dailyQuote, pendingIntent)
                    views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent)
                    views.setTextViewText(R.id.appwidget_text, widgetText)
                    views.setTextViewText(R.id.dailyQuote, strResp)
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                },
                Response.ErrorListener {})

            oldDaysAlive?.let {
                if (it.toInt() != daysAlive) {
                    queue.add(stringReq)
                } else {
                    prefs.getString(Const.DAILY_QUOTE, "0| ")?.split("|")?.last()?.let { quote ->
                        val views = RemoteViews(context.packageName, R.layout.widget_layout)
                        views.setOnClickPendingIntent(R.id.dailyQuote, pendingIntent)
                        views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent)
                        views.setTextViewText(R.id.appwidget_text, widgetText)
                        views.setTextViewText(R.id.dailyQuote, quote)
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                }
            }
        }

        private fun ordinal(i: Int): String {
            val suffixes = arrayOf("th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th")
            return when (i % 100) {
                11, 12, 13 -> {
                    i.toString() + "th"
                }
                else -> i.toString() + suffixes[i % 10]
            }
        }
    }
}

