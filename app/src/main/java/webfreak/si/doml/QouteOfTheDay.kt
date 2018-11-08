package webfreak.si.doml

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random

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
            val daysAlive = prefs.getLong(Const.DAYS_ALIVE,0)
            val queue = Volley.newRequestQueue(context)
            val url = "https://webfreak.si/daysofmylifequotes.json"
            val stringReq = StringRequest(
                Request.Method.GET, url,
                Response.Listener<String> { response ->
                    val strResp = response.toString()
                    val jsonObj = JSONObject(strResp)
                    val jsonArray: JSONArray = jsonObj.getJSONArray("quote")
                    val randQuote = jsonArray.getJSONObject(Random.nextInt(0, jsonArray.length()-1))
                    prefs.edit().putString(Const.DAILY_QUOTE, daysAlive.toString() + "|" + randQuote.get("name").toString()).apply()
                    val widgetText =  java.lang.String.format(context.getString(R.string.widget_title), daysAlive)

                    // Construct the RemoteViews object
                    val views = RemoteViews(context.packageName, R.layout.qoute_of_the_day)
                    views.setTextViewText(R.id.appwidget_text, widgetText)
                    views.setTextViewText(R.id.dailyQuote, randQuote.get("name").toString())
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                },
                Response.ErrorListener {})
            queue.add(stringReq)
        }
    }
}

