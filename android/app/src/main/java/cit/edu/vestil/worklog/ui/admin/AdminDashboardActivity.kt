package cit.edu.vestil.worklog.ui.admin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cit.edu.vestil.worklog.databinding.ActivityAdminDashboardBinding

class AdminDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
