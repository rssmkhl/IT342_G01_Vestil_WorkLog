package cit.edu.vestil.worklog.ui.client

import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.core.view.isVisible
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import cit.edu.vestil.worklog.data.api.RetrofitClient
import cit.edu.vestil.worklog.data.model.Client
import cit.edu.vestil.worklog.databinding.ActivityClientsBinding
import cit.edu.vestil.worklog.ui.common.ApiErrorParser
import cit.edu.vestil.worklog.ui.common.RowRenderer
import cit.edu.vestil.worklog.ui.common.SessionNavigator
import cit.edu.vestil.worklog.ui.navigation.AppNavigator
import kotlinx.coroutines.launch

class ClientsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityClientsBinding
    private var editingClientId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppNavigator.setup(binding.root, this, AppNavigator.CLIENTS)
        binding.btnSaveClient.setOnClickListener { saveClient() }
        binding.btnCancelEdit.setOnClickListener { resetForm() }

        loadClients()
    }

    private fun loadClients() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getClients()
                if (response.isSuccessful) {
                    renderClients(response.body().orEmpty())
                } else {
                    if (!SessionNavigator.handleUnauthorized(this@ClientsActivity, response.code())) {
                        showError(ApiErrorParser.getErrorMessage(response, getString(cit.edu.vestil.worklog.R.string.unable_to_load_clients)))
                    }
                }
            } catch (e: Exception) {
                showError(ApiErrorParser.getThrowableMessage(e, getString(cit.edu.vestil.worklog.R.string.unable_to_load_clients)))
            }
        }
    }

    private fun saveClient() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val company = binding.etCompany.text.toString().trim()
        val notes = binding.etNotes.text.toString().trim()

        if (name.isEmpty() || email.isEmpty()) {
            showError(getString(cit.edu.vestil.worklog.R.string.please_fill_all_fields))
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(getString(cit.edu.vestil.worklog.R.string.invalid_email))
            return
        }

        binding.tvError.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSaveClient.isEnabled = false

        lifecycleScope.launch {
            try {
                val activeResponse = if (editingClientId != null) {
                    RetrofitClient.apiService.updateClient(editingClientId!!, clientPayload(name, email, phone, company, notes))
                } else {
                    RetrofitClient.apiService.createClient(clientPayload(name, email, phone, company, notes))
                }
                if (activeResponse.isSuccessful) {
                    showMessage(
                        if (editingClientId != null) {
                            getString(cit.edu.vestil.worklog.R.string.client_updated)
                        } else {
                            getString(cit.edu.vestil.worklog.R.string.client_saved)
                        }
                    )
                    resetForm()
                    loadClients()
                } else {
                    if (!SessionNavigator.handleUnauthorized(this@ClientsActivity, activeResponse.code())) {
                        showError(ApiErrorParser.getErrorMessage(activeResponse, "Please enter valid client details."))
                    }
                }
            } catch (e: Exception) {
                showError(ApiErrorParser.getThrowableMessage(e, "Please enter valid client details."))
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnSaveClient.isEnabled = true
            }
        }
    }

    private fun renderClients(clients: List<Client>) {
        binding.clientsListContainer.removeAllViews()
        binding.tvEmptyState.isVisible = clients.isEmpty()

        clients.forEach { client ->
            RowRenderer.addRow(
                binding.clientsListContainer,
                client.name,
                client.email,
                listOfNotNull(
                    client.company ?: getString(cit.edu.vestil.worklog.R.string.independent),
                    client.phone,
                    client.notes
                ).joinToString(" • "),
                actions = listOf(
                    RowRenderer.RowAction(getString(cit.edu.vestil.worklog.R.string.edit)) {
                        populateForm(client)
                    },
                    RowRenderer.RowAction(getString(cit.edu.vestil.worklog.R.string.delete)) {
                        deleteClient(client)
                    }
                )
            )
        }
    }

    private fun deleteClient(client: Client) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.deleteClient(client.id ?: return@launch)
                if (response.isSuccessful) {
                    if (editingClientId == client.id) {
                        resetForm()
                    }
                    showMessage(getString(cit.edu.vestil.worklog.R.string.client_deleted))
                    loadClients()
                } else if (!SessionNavigator.handleUnauthorized(this@ClientsActivity, response.code())) {
                    showError(ApiErrorParser.getErrorMessage(response, "Unable to delete this client."))
                }
            } catch (e: Exception) {
                showError(ApiErrorParser.getThrowableMessage(e, "Unable to delete this client."))
            }
        }
    }

    private fun populateForm(client: Client) {
        editingClientId = client.id
        binding.etName.setText(client.name)
        binding.etEmail.setText(client.email)
        binding.etPhone.setText(client.phone.orEmpty())
        binding.etCompany.setText(client.company.orEmpty())
        binding.etNotes.setText(client.notes.orEmpty())
        binding.btnSaveClient.text = getString(cit.edu.vestil.worklog.R.string.update_client)
        binding.btnCancelEdit.visibility = View.VISIBLE
    }

    private fun resetForm() {
        editingClientId = null
        binding.etName.text?.clear()
        binding.etEmail.text?.clear()
        binding.etPhone.text?.clear()
        binding.etCompany.text?.clear()
        binding.etNotes.text?.clear()
        binding.btnSaveClient.text = getString(cit.edu.vestil.worklog.R.string.save_client)
        binding.btnCancelEdit.visibility = View.GONE
    }

    private fun clientPayload(
        name: String,
        email: String,
        phone: String,
        company: String,
        notes: String
    ) = Client(
        name = name,
        email = email,
        phone = phone.ifBlank { null },
        company = company.ifBlank { null },
        notes = notes.ifBlank { null }
    )

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
