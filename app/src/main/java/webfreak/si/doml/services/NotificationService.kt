package webfreak.si.doml.services

import android.content.Context
import com.google.firebase.messaging.FirebaseMessagingService
import webfreak.si.doml.Const
import webfreak.si.doml.PreferenceHelper

class NotificationService: FirebaseMessagingService() {
    private var ctx: Context? = null

    override fun onCreate() {
        ctx = this
    }
    override fun onNewToken(token: String?) {
        ctx?.let {
            val prefs = PreferenceHelper.defaultPrefs(it)
            prefs.edit().putString(Const.TOKEN, token).apply()
        }
    }
}