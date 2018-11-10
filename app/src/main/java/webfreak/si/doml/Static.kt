package webfreak.si.doml

import android.content.Context
import org.joda.time.DateTime
import org.joda.time.Days


class Static {
    companion object {
        fun getDaysAlive(ctx: Context) : Int {
            val prefs = PreferenceHelper.defaultPrefs(ctx)
            val birthDate = DateTime(prefs.getLong(Const.BIRTHDAY,0))
            val currentDate = DateTime().withTimeAtStartOfDay().plusDays(1)
            return Days.daysBetween(birthDate, currentDate).days
        }
    }
}

