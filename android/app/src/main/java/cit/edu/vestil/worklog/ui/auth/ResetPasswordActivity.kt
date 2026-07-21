package cit.edu.vestil.worklog.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import cit.edu.vestil.worklog.R
import cit.edu.vestil.worklog.data.api.RetrofitClient
import cit.edu.vestil.worklog.data.model.ResetPasswordRequest
import cit.edu.vestil.worklog.databinding.ActivityResetPasswordBinding
import cit.edu.vestil.worklog.ui.common.ApiErrorParser
import kotlinx.coroutines.launch

class ResetPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResetPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnResetPassword.setOnClickListener { resetPassword() }
        binding.tvBackToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun resetPassword() {
        val token = binding.etToken.text.toString().trim()
        val newPassword = binding.etNewPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        if (token.isEmpty()) {
            Toast.makeText(this, getString(R.string.enter_reset_token), Toast.LENGTH_SHORT).show()
            return
        }
        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, getString(R.string.please_fill_all_fields), Toast.LENGTH_SHORT).show()
            return
        }
        if (newPassword != confirmPassword) {
            Toast.makeText(this, getString(R.string.passwords_not_match), Toast.LENGTH_SHORT).show()
            return
        }

        binding.tvMessage.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
        binding.btnResetPassword.isEnabled = false

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.resetPassword(
                    ResetPasswordRequest(
                        token = token,
                        newPassword = newPassword,
                        confirmPassword = confirmPassword
                    )
                )
                if (response.isSuccessful) {
                    showMessage(response.body()?.message?.takeIf { it.isNotBlank() } ?: getString(R.string.password_reset_success))
                    binding.etToken.text?.clear()
                    binding.etNewPassword.text?.clear()
                    binding.etConfirmPassword.text?.clear()
                } else {
                    showMessage(ApiErrorParser.getErrorMessage(response, getString(R.string.password_reset_failed)))
                }
            } catch (e: Exception) {
                showMessage(ApiErrorParser.getThrowableMessage(e, getString(R.string.password_reset_failed)))
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnResetPassword.isEnabled = true
            }
        }
    }

    private fun showMessage(message: String) {
        binding.tvMessage.text = message
        binding.tvMessage.visibility = View.VISIBLE
    }
}
