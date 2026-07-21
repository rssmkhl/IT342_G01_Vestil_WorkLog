package cit.edu.vestil.worklog.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import cit.edu.vestil.worklog.R
import cit.edu.vestil.worklog.data.api.RetrofitClient
import cit.edu.vestil.worklog.data.model.ForgotPasswordRequest
import cit.edu.vestil.worklog.databinding.ActivityForgotPasswordBinding
import cit.edu.vestil.worklog.ui.common.ApiErrorParser
import kotlinx.coroutines.launch

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSendReset.setOnClickListener { requestReset() }
        binding.tvResetPassword.setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }
        binding.tvBackToLogin.setOnClickListener { finish() }
    }

    private fun requestReset() {
        val email = binding.etEmail.text.toString().trim()
        if (email.isEmpty()) {
            Toast.makeText(this, getString(R.string.please_fill_all_fields), Toast.LENGTH_SHORT).show()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, getString(R.string.invalid_email), Toast.LENGTH_SHORT).show()
            return
        }

        binding.tvMessage.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSendReset.isEnabled = false

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.forgotPassword(ForgotPasswordRequest(email))
                val message = if (response.isSuccessful) {
                    response.body()?.message?.takeIf { it.isNotBlank() } ?: getString(R.string.password_reset_requested)
                } else {
                    ApiErrorParser.getErrorMessage(response, getString(R.string.password_reset_requested))
                }
                showMessage(message)
            } catch (e: Exception) {
                showMessage(ApiErrorParser.getThrowableMessage(e, getString(R.string.password_reset_requested)))
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnSendReset.isEnabled = true
            }
        }
    }

    private fun showMessage(message: String) {
        binding.tvMessage.text = message
        binding.tvMessage.visibility = View.VISIBLE
    }
}
