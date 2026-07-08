package cit.edu.vestil.worklog

import android.app.Application
import cit.edu.vestil.worklog.data.preferences.UserPreferences

class WorklogApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        UserPreferences.init(this)
    }
}
