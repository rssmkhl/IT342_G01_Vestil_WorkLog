package cit.edu.vestil.worklog.ui.admin

import android.app.AlertDialog
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import cit.edu.vestil.worklog.data.api.RetrofitClient
import cit.edu.vestil.worklog.data.model.AdminClientRow
import cit.edu.vestil.worklog.data.model.AdminCreateUserRequest
import cit.edu.vestil.worklog.data.model.AdminPaymentRow
import cit.edu.vestil.worklog.data.model.AdminSummary
import cit.edu.vestil.worklog.data.model.AdminUpdateUserRequest
import cit.edu.vestil.worklog.data.model.AdminWorkLogRow
import cit.edu.vestil.worklog.data.model.AuditLogEntry
import cit.edu.vestil.worklog.data.model.User
import cit.edu.vestil.worklog.data.preferences.UserPreferences
import cit.edu.vestil.worklog.databinding.ActivityAdminDashboardBinding
import cit.edu.vestil.worklog.ui.common.ApiErrorParser
import cit.edu.vestil.worklog.ui.common.RowRenderer
import cit.edu.vestil.worklog.ui.common.SessionNavigator
import cit.edu.vestil.worklog.ui.navigation.AppNavigator
import kotlinx.coroutines.launch

class AdminDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminDashboardBinding
    private val userRoles = listOf("ADMIN", "FREELANCER", "USER")

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

        binding.btnCreateUser.setOnClickListener { showUserDialog() }
        loadAdminData()
    }

    private fun loadAdminData() {
        lifecycleScope.launch {
            try {
                val summaryResponse = RetrofitClient.apiService.getAdminSummary()
                val usersResponse = RetrofitClient.apiService.getAllUsers()
                val workLogsResponse = RetrofitClient.apiService.getAdminWorkLogs()
                val clientsResponse = RetrofitClient.apiService.getAdminClients()
                val paymentsResponse = RetrofitClient.apiService.getAdminPayments()
                val auditLogsResponse = RetrofitClient.apiService.getAuditLogs()

                if (
                    summaryResponse.isSuccessful &&
                    usersResponse.isSuccessful &&
                    workLogsResponse.isSuccessful &&
                    clientsResponse.isSuccessful &&
                    paymentsResponse.isSuccessful &&
                    auditLogsResponse.isSuccessful
                ) {
                    bindSummary(summaryResponse.body() ?: AdminSummary())
                    bindUsers(usersResponse.body().orEmpty())
                    bindWorkLogs(workLogsResponse.body().orEmpty())
                    bindClients(clientsResponse.body().orEmpty())
                    bindPayments(paymentsResponse.body().orEmpty())
                    bindAuditLogs(auditLogsResponse.body().orEmpty())
                } else {
                    val unauthorized = listOf(
                        summaryResponse.code(),
                        usersResponse.code(),
                        workLogsResponse.code(),
                        clientsResponse.code(),
                        paymentsResponse.code(),
                        auditLogsResponse.code()
                    ).any { SessionNavigator.handleUnauthorized(this@AdminDashboardActivity, it, getString(cit.edu.vestil.worklog.R.string.session_expired)) }
                    if (!unauthorized) {
                        showError(getString(cit.edu.vestil.worklog.R.string.unable_to_load_admin_data))
                    }
                }
            } catch (e: Exception) {
                showError(ApiErrorParser.getThrowableMessage(e, getString(cit.edu.vestil.worklog.R.string.unable_to_load_admin_data)))
            }
        }
    }

    private fun bindSummary(summary: AdminSummary) {
        binding.tvTotalUsers.text = summary.totalFreelancers.toString()
        binding.tvTotalClients.text = summary.activeClients.toString()
        binding.tvTotalWorkLogs.text = summary.workLogsThisWeek.toString()
        binding.tvPendingPayments.text = summary.pendingPayments.toString()
    }

    private fun bindUsers(users: List<User>) {
        binding.usersContainer.removeAllViews()
        val currentUserId = UserPreferences.getUserId()
        users.forEach { user ->
            val meta = listOfNotNull(
                user.email,
                user.role,
                user.status?.let { getString(cit.edu.vestil.worklog.R.string.status_label, it) },
                user.lastLogin?.let { getString(cit.edu.vestil.worklog.R.string.last_login_label, it) }
            ).joinToString(" • ")
            val actions = mutableListOf(
                RowRenderer.RowAction(getString(cit.edu.vestil.worklog.R.string.edit)) {
                    showUserDialog(user)
                },
                RowRenderer.RowAction(getString(cit.edu.vestil.worklog.R.string.reset_password_short)) {
                    resetPassword(user)
                }
            )
            if (user.id != currentUserId) {
                actions += RowRenderer.RowAction(getString(cit.edu.vestil.worklog.R.string.toggle_status)) {
                    toggleStatus(user)
                }
                actions += RowRenderer.RowAction(getString(cit.edu.vestil.worklog.R.string.delete)) {
                    deleteUser(user)
                }
            }
            RowRenderer.addRow(binding.usersContainer, user.fullName, user.username, meta, actions)
        }
    }

    private fun bindWorkLogs(workLogs: List<AdminWorkLogRow>) {
        binding.adminWorkLogsContainer.removeAllViews()
        workLogs.forEach { entry ->
            val subtitle =
                "${entry.userName ?: getString(cit.edu.vestil.worklog.R.string.unknown_user)} • ${entry.clientName ?: getString(cit.edu.vestil.worklog.R.string.no_client)}"
            val meta =
                "${entry.project ?: getString(cit.edu.vestil.worklog.R.string.general_project)} • ${entry.date ?: getString(cit.edu.vestil.worklog.R.string.no_date)} • ${entry.hours ?: 0.0}h • ${entry.status ?: getString(cit.edu.vestil.worklog.R.string.in_progress)}"
            RowRenderer.addRow(
                binding.adminWorkLogsContainer,
                entry.title,
                subtitle,
                meta,
                actions = listOf(
                    RowRenderer.RowAction(getString(cit.edu.vestil.worklog.R.string.approve)) {
                        reviewWorkLog(entry, true)
                    },
                    RowRenderer.RowAction(getString(cit.edu.vestil.worklog.R.string.reject)) {
                        reviewWorkLog(entry, false)
                    }
                )
            )
        }
    }

    private fun bindClients(clients: List<AdminClientRow>) {
        binding.adminClientsContainer.removeAllViews()
        clients.forEach { client ->
            val subtitle = "${client.userName ?: getString(cit.edu.vestil.worklog.R.string.unknown_user)} • ${client.email}"
            val meta = listOfNotNull(client.company, client.phone, client.notes, client.status).joinToString(" • ")
            RowRenderer.addRow(
                binding.adminClientsContainer,
                client.name,
                subtitle,
                meta,
                actions = listOf(
                    RowRenderer.RowAction(getString(cit.edu.vestil.worklog.R.string.archive)) {
                        archiveClient(client)
                    }
                )
            )
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
            RowRenderer.addRow(
                binding.adminPaymentsContainer,
                title,
                subtitle,
                meta,
                actions = listOf(
                    RowRenderer.RowAction(getString(cit.edu.vestil.worklog.R.string.approve)) {
                        reviewPayment(payment, true)
                    },
                    RowRenderer.RowAction(getString(cit.edu.vestil.worklog.R.string.reject)) {
                        reviewPayment(payment, false)
                    }
                )
            )
        }
    }

    private fun bindAuditLogs(auditLogs: List<AuditLogEntry>) {
        binding.auditLogsContainer.removeAllViews()
        binding.tvAuditEmptyState.visibility = if (auditLogs.isEmpty()) View.VISIBLE else View.GONE
        auditLogs.forEach { entry ->
            RowRenderer.addRow(
                binding.auditLogsContainer,
                entry.action ?: getString(cit.edu.vestil.worklog.R.string.activity_feed),
                entry.targetRecord ?: getString(cit.edu.vestil.worklog.R.string.no_reference),
                "${entry.adminName ?: getString(cit.edu.vestil.worklog.R.string.unknown_user)} • ${entry.timestamp ?: getString(cit.edu.vestil.worklog.R.string.no_date)}"
            )
        }
    }

    private fun showUserDialog(user: User? = null) {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 0)
        }
        val fullNameField = EditText(this).apply {
            hint = getString(cit.edu.vestil.worklog.R.string.full_name)
            setText(user?.fullName.orEmpty())
        }
        val usernameField = EditText(this).apply {
            hint = getString(cit.edu.vestil.worklog.R.string.username)
            setText(user?.username.orEmpty())
        }
        val emailField = EditText(this).apply {
            hint = getString(cit.edu.vestil.worklog.R.string.email)
            setText(user?.email.orEmpty())
        }
        val passwordField = EditText(this).apply {
            hint = getString(cit.edu.vestil.worklog.R.string.temp_password_optional)
            visibility = if (user == null) View.VISIBLE else View.GONE
        }
        val roleSpinner = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@AdminDashboardActivity,
                android.R.layout.simple_spinner_dropdown_item,
                userRoles
            )
            setSelection(userRoles.indexOf(user?.role ?: "FREELANCER").coerceAtLeast(0))
        }
        container.addView(fullNameField)
        container.addView(usernameField)
        container.addView(emailField)
        container.addView(roleSpinner)
        container.addView(passwordField)

        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(cit.edu.vestil.worklog.R.string.admin_user_form_title))
            .setView(container)
            .setPositiveButton(getString(cit.edu.vestil.worklog.R.string.save_user), null)
            .setNegativeButton(getString(cit.edu.vestil.worklog.R.string.cancel_edit), null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val fullName = fullNameField.text.toString().trim()
                val username = usernameField.text.toString().trim()
                val email = emailField.text.toString().trim()
                val password = passwordField.text.toString().trim()
                val role = roleSpinner.selectedItem?.toString().orEmpty()

                if (fullName.isBlank() || username.isBlank() || email.isBlank()) {
                    showError(getString(cit.edu.vestil.worklog.R.string.please_fill_all_fields))
                    return@setOnClickListener
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    showError(getString(cit.edu.vestil.worklog.R.string.invalid_email))
                    return@setOnClickListener
                }

                lifecycleScope.launch {
                    try {
                        val response = if (user == null) {
                            RetrofitClient.apiService.createAdminUser(
                                AdminCreateUserRequest(
                                    fullName = fullName,
                                    username = username,
                                    email = email,
                                    role = role,
                                    password = password.ifBlank { null }
                                )
                            )
                        } else {
                            RetrofitClient.apiService.updateAdminUser(
                                user.id,
                                AdminUpdateUserRequest(
                                    fullName = fullName,
                                    username = username,
                                    email = email,
                                    role = role
                                )
                            )
                        }
                        if (response.isSuccessful) {
                            showMessage(
                                if (user == null) {
                                    getString(cit.edu.vestil.worklog.R.string.user_saved)
                                } else {
                                    getString(cit.edu.vestil.worklog.R.string.user_updated)
                                }
                            )
                            dialog.dismiss()
                            loadAdminData()
                        } else if (!SessionNavigator.handleUnauthorized(this@AdminDashboardActivity, response.code(), getString(cit.edu.vestil.worklog.R.string.session_expired))) {
                            showError(ApiErrorParser.getErrorMessage(response, "Unable to save this user."))
                        }
                    } catch (e: Exception) {
                        showError(ApiErrorParser.getThrowableMessage(e, "Unable to save this user."))
                    }
                }
            }
        }
        dialog.show()
    }

    private fun resetPassword(user: User) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.resetAdminUserPassword(user.id)
                if (response.isSuccessful) {
                    showMessage(getString(cit.edu.vestil.worklog.R.string.reset_password_result, response.body().orEmpty()))
                } else if (!SessionNavigator.handleUnauthorized(this@AdminDashboardActivity, response.code(), getString(cit.edu.vestil.worklog.R.string.session_expired))) {
                    showError(ApiErrorParser.getErrorMessage(response, "Unable to reset this password."))
                }
            } catch (e: Exception) {
                showError(ApiErrorParser.getThrowableMessage(e, "Unable to reset this password."))
            }
        }
    }

    private fun toggleStatus(user: User) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.toggleAdminUserStatus(user.id)
                if (response.isSuccessful) {
                    showMessage(getString(cit.edu.vestil.worklog.R.string.user_updated))
                    loadAdminData()
                } else if (!SessionNavigator.handleUnauthorized(this@AdminDashboardActivity, response.code(), getString(cit.edu.vestil.worklog.R.string.session_expired))) {
                    showError(ApiErrorParser.getErrorMessage(response, "Unable to update this user."))
                }
            } catch (e: Exception) {
                showError(ApiErrorParser.getThrowableMessage(e, "Unable to update this user."))
            }
        }
    }

    private fun deleteUser(user: User) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.deleteUser(user.id)
                if (response.isSuccessful) {
                    showMessage(getString(cit.edu.vestil.worklog.R.string.admin_user_disabled))
                    loadAdminData()
                } else {
                    if (!SessionNavigator.handleUnauthorized(this@AdminDashboardActivity, response.code(), getString(cit.edu.vestil.worklog.R.string.session_expired))) {
                        showError(ApiErrorParser.getErrorMessage(response, "Unable to update this user."))
                    }
                }
            } catch (e: Exception) {
                showError(ApiErrorParser.getThrowableMessage(e, "Unable to update this user."))
            }
        }
    }

    private fun reviewWorkLog(entry: AdminWorkLogRow, approve: Boolean) {
        lifecycleScope.launch {
            try {
                val response = if (approve) {
                    RetrofitClient.apiService.approveAdminWorkLog(entry.id)
                } else {
                    RetrofitClient.apiService.rejectAdminWorkLog(entry.id)
                }
                if (response.isSuccessful) {
                    showMessage(
                        if (approve) {
                            getString(cit.edu.vestil.worklog.R.string.work_log_approved)
                        } else {
                            getString(cit.edu.vestil.worklog.R.string.work_log_rejected)
                        }
                    )
                    loadAdminData()
                } else if (!SessionNavigator.handleUnauthorized(this@AdminDashboardActivity, response.code(), getString(cit.edu.vestil.worklog.R.string.session_expired))) {
                    showError(ApiErrorParser.getErrorMessage(response, "Unable to review this work log."))
                }
            } catch (e: Exception) {
                showError(ApiErrorParser.getThrowableMessage(e, "Unable to review this work log."))
            }
        }
    }

    private fun reviewPayment(payment: AdminPaymentRow, approve: Boolean) {
        lifecycleScope.launch {
            try {
                val response = if (approve) {
                    RetrofitClient.apiService.approveAdminPayment(payment.id)
                } else {
                    RetrofitClient.apiService.rejectAdminPayment(payment.id)
                }
                if (response.isSuccessful) {
                    showMessage(
                        if (approve) {
                            getString(cit.edu.vestil.worklog.R.string.payment_approved)
                        } else {
                            getString(cit.edu.vestil.worklog.R.string.payment_rejected)
                        }
                    )
                    loadAdminData()
                } else if (!SessionNavigator.handleUnauthorized(this@AdminDashboardActivity, response.code(), getString(cit.edu.vestil.worklog.R.string.session_expired))) {
                    showError(ApiErrorParser.getErrorMessage(response, "Unable to review this payment."))
                }
            } catch (e: Exception) {
                showError(ApiErrorParser.getThrowableMessage(e, "Unable to review this payment."))
            }
        }
    }

    private fun archiveClient(client: AdminClientRow) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.archiveAdminClient(client.id)
                if (response.isSuccessful) {
                    showMessage(getString(cit.edu.vestil.worklog.R.string.client_archived))
                    loadAdminData()
                } else if (!SessionNavigator.handleUnauthorized(this@AdminDashboardActivity, response.code(), getString(cit.edu.vestil.worklog.R.string.session_expired))) {
                    showError(ApiErrorParser.getErrorMessage(response, "Unable to archive this client."))
                }
            } catch (e: Exception) {
                showError(ApiErrorParser.getThrowableMessage(e, "Unable to archive this client."))
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
