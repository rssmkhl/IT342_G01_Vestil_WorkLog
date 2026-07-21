package cit.edu.vestil.worklog.ui.common

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import cit.edu.vestil.worklog.data.preferences.UserPreferences
import cit.edu.vestil.worklog.ui.auth.LoginActivity

object SessionNavigator {
    const val EXTRA_MESSAGE = "extra_message"

    fun handleUnauthorized(activity: Activity, statusCode: Int, message: String = "Your session has expired. Please log in again."): Boolean {
        if (statusCode != 401 && statusCode != 403) {
            return false
        }

        UserPreferences.clear()
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
        val intent = Intent(activity, LoginActivity::class.java).apply {
            putExtra(EXTRA_MESSAGE, message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        activity.startActivity(intent)
        activity.finish()
        return true
    }
}
