package cit.edu.vestil.worklog.ui.client

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import cit.edu.vestil.worklog.data.api.RetrofitClient
import cit.edu.vestil.worklog.data.model.Client
import cit.edu.vestil.worklog.databinding.ActivityClientsBinding
import cit.edu.vestil.worklog.ui.common.RowRenderer
import cit.edu.vestil.worklog.ui.navigation.AppNavigator
import kotlinx.coroutines.launch

class ClientsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityClientsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppNavigator.setup(binding.root, this, AppNavigator.CLIENTS)
        binding.btnSaveClient.setOnClickListener { saveClient() }

        loadClients()
    }

    private fun loadClients() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getClients()
                if (response.isSuccessful) {
                    renderClients(response.body().orEmpty())
                } else {
                    showMessage("Unable to load clients right now.")
                }
            } catch (e: Exception) {
                showMessage("Unable to load clients right now.")
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
            showMessage("Please enter valid client details.")
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSaveClient.isEnabled = false

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.createClient(
                    Client(
                        name = name,
                        email = email,
                        phone = phone.ifBlank { null },
                        company = company.ifBlank { null },
                        notes = notes.ifBlank { null }
                    )
                )
                if (response.isSuccessful) {
                    binding.etName.text?.clear()
                    binding.etEmail.text?.clear()
                    binding.etPhone.text?.clear()
                    binding.etCompany.text?.clear()
                    binding.etNotes.text?.clear()
                    showMessage("Client added successfully.")
                    loadClients()
                } else {
                    showMessage("Please enter valid client details.")
                }
            } catch (e: Exception) {
                showMessage("Please enter valid client details.")
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
                client.company ?: getString(cit.edu.vestil.worklog.R.string.independent)
            )
        }
    }

    private fun showMessage(message: String) {
        binding.tvMessage.text = message
        binding.tvMessage.visibility = View.VISIBLE
    }
}
