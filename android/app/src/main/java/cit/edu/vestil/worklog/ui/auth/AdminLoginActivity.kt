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
import cit.edu.vestil.worklog.data.model.LoginRequest
import cit.edu.vestil.worklog.data.preferences.UserPreferences
import cit.edu.vestil.worklog.databinding.ActivityAdminLoginBinding
import cit.edu.vestil.worklog.ui.admin.AdminDashboardActivity
import cit.edu.vestil.worklog.ui.common.ApiErrorParser
import cit.edu.vestil.worklog.ui.common.SessionNavigator
import kotlinx.coroutines.launch

class AdminLoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (UserPreferences.isLoggedIn() && UserPreferences.getUserRole() == "ADMIN") {
            startActivity(Intent(this, AdminDashboardActivity::class.java))
            finish()
            return
        }

        binding = ActivityAdminLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent.getStringExtra(SessionNavigator.EXTRA_MESSAGE)?.let { showMessage(it) }

        binding.btnLogin.setOnClickListener { login() }
        binding.tvUserLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun login() {
        val usernameEmail = binding.etUsernameEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (usernameEmail.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.please_fill_all_fields), Toast.LENGTH_SHORT).show()
            return
        }

        if (usernameEmail.contains("@") && !Patterns.EMAIL_ADDRESS.matcher(usernameEmail).matches()) {
            Toast.makeText(this, getString(R.string.invalid_email), Toast.LENGTH_SHORT).show()
            return
        }

        binding.tvMessage.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.login(LoginRequest(usernameEmail, password))
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    if (authResponse.role != "ADMIN") {
                        showMessage(getString(R.string.use_user_login))
                        return@launch
                    }

                    UserPreferences.saveAuthData(
                        authResponse.token,
                        authResponse.id,
                        authResponse.fullName,
                        authResponse.username,
                        authResponse.email,
                        authResponse.role
                    )
                    startActivity(Intent(this@AdminLoginActivity, AdminDashboardActivity::class.java))
                    finish()
                } else {
                    if (SessionNavigator.handleUnauthorized(this@AdminLoginActivity, response.code(), getString(R.string.session_expired))) {
                        return@launch
                    }
                    showMessage(ApiErrorParser.getErrorMessage(response, getString(R.string.invalid_credentials)))
                }
            } catch (e: Exception) {
                showMessage(ApiErrorParser.getThrowableMessage(e, getString(R.string.invalid_credentials)))
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true
            }
        }
    }

    private fun showMessage(message: String) {
        binding.tvMessage.text = message
        binding.tvMessage.visibility = View.VISIBLE
    }
}
