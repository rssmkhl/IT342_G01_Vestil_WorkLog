package cit.edu.vestil.worklog.ui.worklog

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cit.edu.vestil.worklog.R
import cit.edu.vestil.worklog.databinding.ActivityWorkLogsBinding

class WorkLogsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWorkLogsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkLogsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
