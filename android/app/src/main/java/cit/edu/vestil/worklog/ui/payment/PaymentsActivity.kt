package cit.edu.vestil.worklog.ui.payment

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cit.edu.vestil.worklog.R
import cit.edu.vestil.worklog.databinding.ActivityPaymentsBinding

class PaymentsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPaymentsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
