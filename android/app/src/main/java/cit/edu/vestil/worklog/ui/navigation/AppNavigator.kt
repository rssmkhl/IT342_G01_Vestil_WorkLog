package cit.edu.vestil.worklog.ui.navigation

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.Button
import cit.edu.vestil.worklog.R
import cit.edu.vestil.worklog.data.preferences.UserPreferences
import cit.edu.vestil.worklog.ui.admin.AdminDashboardActivity
import cit.edu.vestil.worklog.ui.auth.LoginActivity
import cit.edu.vestil.worklog.ui.client.ClientsActivity
import cit.edu.vestil.worklog.ui.dashboard.DashboardActivity
import cit.edu.vestil.worklog.ui.payment.PaymentsActivity
import cit.edu.vestil.worklog.ui.worklog.WorkLogsActivity

object AppNavigator {
    const val DASHBOARD = "dashboard"
    const val CLIENTS = "clients"
    const val WORK_LOGS = "worklogs"
    const val PAYMENTS = "payments"
    const val ADMIN = "admin"

    fun setup(root: View, activity: Activity, currentRoute: String) {
        bindButton(root, R.id.btnNavDashboard, currentRoute == DASHBOARD) {
            openIfNeeded(activity, currentRoute, DASHBOARD, DashboardActivity::class.java)
        }
        bindButton(root, R.id.btnNavClients, currentRoute == CLIENTS) {
            openIfNeeded(activity, currentRoute, CLIENTS, ClientsActivity::class.java)
        }
        bindButton(root, R.id.btnNavWorkLogs, currentRoute == WORK_LOGS) {
            openIfNeeded(activity, currentRoute, WORK_LOGS, WorkLogsActivity::class.java)
        }
        bindButton(root, R.id.btnNavPayments, currentRoute == PAYMENTS) {
            openIfNeeded(activity, currentRoute, PAYMENTS, PaymentsActivity::class.java)
        }

        val adminButton = root.findViewById<Button?>(R.id.btnNavAdmin)
        if (UserPreferences.getUserRole() == "ADMIN") {
            adminButton?.visibility = View.VISIBLE
            adminButton?.isSelected = currentRoute == ADMIN
            adminButton?.setOnClickListener {
                openIfNeeded(activity, currentRoute, ADMIN, AdminDashboardActivity::class.java)
            }
        } else {
            adminButton?.visibility = View.GONE
        }

        root.findViewById<Button?>(R.id.btnNavLogout)?.setOnClickListener {
            UserPreferences.clear()
            activity.startActivity(Intent(activity, LoginActivity::class.java))
            activity.finishAffinity()
        }
    }

    private fun bindButton(root: View, id: Int, selected: Boolean, onClick: () -> Unit) {
        root.findViewById<Button?>(id)?.apply {
            isSelected = selected
            setOnClickListener { onClick() }
        }
    }

    private fun openIfNeeded(
        activity: Activity,
        currentRoute: String,
        targetRoute: String,
        targetActivity: Class<out Activity>
    ) {
        if (currentRoute == targetRoute) return
        activity.startActivity(Intent(activity, targetActivity))
        activity.finish()
    }
}
