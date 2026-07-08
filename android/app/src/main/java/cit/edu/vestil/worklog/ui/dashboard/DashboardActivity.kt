package cit.edu.vestil.worklog.ui.dashboard

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import cit.edu.vestil.worklog.R
import cit.edu.vestil.worklog.data.api.RetrofitClient
import cit.edu.vestil.worklog.data.preferences.UserPreferences
import cit.edu.vestil.worklog.databinding.ActivityDashboardBinding
import cit.edu.vestil.worklog.ui.auth.LoginActivity
import cit.edu.vestil.worklog.ui.client.ClientsActivity
import cit.edu.vestil.worklog.ui.payment.PaymentsActivity
import cit.edu.vestil.worklog.ui.worklog.WorkLogAdapter
import cit.edu.vestil.worklog.ui.worklog.WorkLogsActivity
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvUserName.text = UserPreferences.getUserName()

        binding.btnLogout.setOnClickListener { logout() }
        binding.cardClients.setOnClickListener {
            startActivity(Intent(this, ClientsActivity::class.java))
        }
        binding.cardWorkLogs.setOnClickListener {
            startActivity(Intent(this, WorkLogsActivity::class.java))
        }
        binding.cardPayments.setOnClickListener {
            startActivity(Intent(this, PaymentsActivity::class.java))
        }

        binding.rvRecentWorkLogs.layoutManager = LinearLayoutManager(this)

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
                    binding.rvRecentWorkLogs.adapter = WorkLogAdapter(summary.recentWorkLogs)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun logout() {
        UserPreferences.clear()
        startActivity(Intent(this, LoginActivity::class.java))
        finishAffinity()
    }
}
