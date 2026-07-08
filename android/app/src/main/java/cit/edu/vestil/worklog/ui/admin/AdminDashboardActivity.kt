package cit.edu.vestil.worklog.ui.admin

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import cit.edu.vestil.worklog.data.api.RetrofitClient
import cit.edu.vestil.worklog.data.model.AdminPaymentRow
import cit.edu.vestil.worklog.data.model.AdminSummary
import cit.edu.vestil.worklog.data.model.AdminWorkLogRow
import cit.edu.vestil.worklog.data.model.User
import cit.edu.vestil.worklog.data.preferences.UserPreferences
import cit.edu.vestil.worklog.databinding.ActivityAdminDashboardBinding
import cit.edu.vestil.worklog.ui.common.RowRenderer
import cit.edu.vestil.worklog.ui.navigation.AppNavigator
import kotlinx.coroutines.launch

class AdminDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppNavigator.setup(binding.root, this, AppNavigator.ADMIN)

        if (UserPreferences.getUserRole() != "ADMIN") {
            binding.tvAdminAccessNotice.visibility = View.VISIBLE
            binding.adminContent.visibility = View.GONE
            return
        }

        loadAdminData()
    }

    private fun loadAdminData() {
        lifecycleScope.launch {
            try {
                val summaryResponse = RetrofitClient.apiService.getAdminSummary()
                val usersResponse = RetrofitClient.apiService.getAllUsers()
                val workLogsResponse = RetrofitClient.apiService.getAdminWorkLogs()
                val paymentsResponse = RetrofitClient.apiService.getAdminPayments()

                if (
                    summaryResponse.isSuccessful &&
                    usersResponse.isSuccessful &&
                    workLogsResponse.isSuccessful &&
                    paymentsResponse.isSuccessful
                ) {
                    bindSummary(summaryResponse.body() ?: AdminSummary())
                    bindUsers(usersResponse.body().orEmpty())
                    bindWorkLogs(workLogsResponse.body().orEmpty())
                    bindPayments(paymentsResponse.body().orEmpty())
                } else {
                    showError("Unable to load admin data.")
                }
            } catch (e: Exception) {
                showError("Unable to load admin data.")
            }
        }
    }

    private fun bindSummary(summary: AdminSummary) {
        binding.tvTotalUsers.text = summary.totalUsers.toString()
        binding.tvTotalClients.text = summary.totalClients.toString()
        binding.tvTotalWorkLogs.text = summary.totalWorkLogs.toString()
        binding.tvPendingPayments.text = summary.pendingPayments.toString()
    }

    private fun bindUsers(users: List<User>) {
        binding.usersContainer.removeAllViews()
        val currentUserId = UserPreferences.getUserId()
        users.forEach { user ->
            val meta = "${user.email} • ${user.role}"
            if (user.id == currentUserId) {
                RowRenderer.addRow(binding.usersContainer, user.fullName, user.username, meta)
            } else {
                RowRenderer.addRow(
                    binding.usersContainer,
                    user.fullName,
                    user.username,
                    meta,
                    getString(cit.edu.vestil.worklog.R.string.delete)
                ) {
                    deleteUser(user)
                }
            }
        }
    }

    private fun bindWorkLogs(workLogs: List<AdminWorkLogRow>) {
        binding.adminWorkLogsContainer.removeAllViews()
        workLogs.forEach { entry ->
            val subtitle =
                "${entry.userName ?: getString(cit.edu.vestil.worklog.R.string.unknown_user)} • ${entry.clientName ?: getString(cit.edu.vestil.worklog.R.string.no_client)}"
            val meta =
                "${entry.project ?: getString(cit.edu.vestil.worklog.R.string.general_project)} • ${entry.date ?: getString(cit.edu.vestil.worklog.R.string.no_date)} • ${entry.hours ?: 0.0}h • ${entry.status ?: getString(cit.edu.vestil.worklog.R.string.in_progress)}"
            RowRenderer.addRow(binding.adminWorkLogsContainer, entry.title, subtitle, meta)
        }
    }

    private fun bindPayments(payments: List<AdminPaymentRow>) {
        binding.adminPaymentsContainer.removeAllViews()
        payments.forEach { payment ->
            val title = "$${payment.amount}"
            val subtitle =
                "${payment.userName ?: getString(cit.edu.vestil.worklog.R.string.unknown_user)} • ${payment.clientName ?: getString(cit.edu.vestil.worklog.R.string.no_client)}"
            val meta =
                "${payment.method ?: getString(cit.edu.vestil.worklog.R.string.cash)} • ${payment.status ?: getString(cit.edu.vestil.worklog.R.string.pending)} • ${payment.paymentDate ?: getString(cit.edu.vestil.worklog.R.string.no_date)} • ${payment.reference ?: getString(cit.edu.vestil.worklog.R.string.no_reference)}"
            RowRenderer.addRow(binding.adminPaymentsContainer, title, subtitle, meta)
        }
    }

    private fun deleteUser(user: User) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.deleteUser(user.id)
                if (response.isSuccessful) {
                    showMessage("User account deleted.")
                    loadAdminData()
                } else {
                    showError("Unable to delete this user.")
                }
            } catch (e: Exception) {
                showError("Unable to delete this user.")
            }
        }
    }

    private fun showMessage(message: String) {
        binding.tvMessage.text = message
        binding.tvMessage.visibility = View.VISIBLE
        binding.tvError.visibility = View.GONE
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }
}
