package cit.edu.vestil.worklog.ui.client

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cit.edu.vestil.worklog.R
import cit.edu.vestil.worklog.databinding.ActivityClientsBinding

class ClientsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityClientsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
