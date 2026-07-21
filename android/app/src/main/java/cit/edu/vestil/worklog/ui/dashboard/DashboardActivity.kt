package cit.edu.vestil.worklog.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import cit.edu.vestil.worklog.data.api.RetrofitClient
import cit.edu.vestil.worklog.data.preferences.UserPreferences
import cit.edu.vestil.worklog.databinding.ActivityDashboardBinding
import cit.edu.vestil.worklog.ui.admin.AdminDashboardActivity
import cit.edu.vestil.worklog.ui.common.ApiErrorParser
import cit.edu.vestil.worklog.ui.common.SessionNavigator
import cit.edu.vestil.worklog.ui.client.ClientsActivity
import cit.edu.vestil.worklog.ui.payment.PaymentsActivity
import cit.edu.vestil.worklog.ui.worklog.WorkLogsActivity
import cit.edu.vestil.worklog.ui.navigation.AppNavigator
import android.widget.Toast
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userName = UserPreferences.getUserName().orEmpty().ifBlank { "User" }
        binding.tvUserName.text = "Welcome, $userName!"
        AppNavigator.setup(binding.root, this, AppNavigator.DASHBOARD)

        binding.cardClients.setOnClickListener {
            startActivity(Intent(this, ClientsActivity::class.java))
        }
        binding.cardWorkLogs.setOnClickListener {
            startActivity(Intent(this, WorkLogsActivity::class.java))
        }
        binding.cardPayments.setOnClickListener {
            startActivity(Intent(this, PaymentsActivity::class.java))
        }
        if (UserPreferences.getUserRole() == "ADMIN") {
            binding.cardAdminShortcut.visibility = View.VISIBLE
            binding.cardAdminShortcut.setOnClickListener {
                startActivity(Intent(this, AdminDashboardActivity::class.java))
            }
        }

        loadDashboardSummary()
    }

    private fun loadDashboardSummary() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getDashboardSummary()
                if (response.isSuccessful && response.body() != null) {
                    val summary = response.body()!!
                    binding.tvTotalClients.text = summary.totalClients.toString()
                    binding.tvTotalWorkLogs.text = summary.totalWorkLogs.toString()
                    binding.tvTotalPayments.text = "$${summary.totalPayments}"
                } else if (!SessionNavigator.handleUnauthorized(this@DashboardActivity, response.code())) {
                    Toast.makeText(
                        this@DashboardActivity,
                        ApiErrorParser.getErrorMessage(response, getString(cit.edu.vestil.worklog.R.string.unable_to_load_dashboard)),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@DashboardActivity,
                    ApiErrorParser.getThrowableMessage(e, getString(cit.edu.vestil.worklog.R.string.unable_to_load_dashboard)),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
