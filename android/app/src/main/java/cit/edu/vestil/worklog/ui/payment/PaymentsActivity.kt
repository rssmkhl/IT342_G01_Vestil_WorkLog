package cit.edu.vestil.worklog.ui.payment

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import cit.edu.vestil.worklog.data.api.RetrofitClient
import cit.edu.vestil.worklog.data.model.Client
import cit.edu.vestil.worklog.data.model.Payment
import cit.edu.vestil.worklog.databinding.ActivityPaymentsBinding
import cit.edu.vestil.worklog.ui.common.RowRenderer
import cit.edu.vestil.worklog.ui.navigation.AppNavigator
import kotlinx.coroutines.launch

class PaymentsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPaymentsBinding
    private var clients: List<Client> = emptyList()
    private val methods by lazy {
        listOf(
            getString(cit.edu.vestil.worklog.R.string.cash),
            getString(cit.edu.vestil.worklog.R.string.gcash),
            getString(cit.edu.vestil.worklog.R.string.maya),
            getString(cit.edu.vestil.worklog.R.string.bank_transfer),
            getString(cit.edu.vestil.worklog.R.string.paypal)
        )
    }
    private val statuses by lazy {
        listOf(
            getString(cit.edu.vestil.worklog.R.string.pending),
            getString(cit.edu.vestil.worklog.R.string.paid),
            getString(cit.edu.vestil.worklog.R.string.partially_paid)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppNavigator.setup(binding.root, this, AppNavigator.PAYMENTS)
        binding.spMethod.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, methods)
        binding.spStatus.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, statuses)
        binding.btnSavePayment.setOnClickListener { savePayment() }

        loadClients()
        loadPayments()
    }

    private fun loadClients() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getClients()
                if (response.isSuccessful) {
                    clients = response.body().orEmpty()
                    val clientNames = listOf(getString(cit.edu.vestil.worklog.R.string.select_client)) +
                        clients.map { it.name }
                    binding.spClient.adapter =
                        ArrayAdapter(this@PaymentsActivity, android.R.layout.simple_spinner_dropdown_item, clientNames)
                } else {
                    showError("Unable to load clients right now.")
                }
            } catch (e: Exception) {
                showError("Unable to load clients right now.")
            }
        }
    }

    private fun loadPayments() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getPayments()
                if (response.isSuccessful) {
                    renderPayments(response.body().orEmpty())
                } else {
                    showError("Unable to load payments right now.")
                }
            } catch (e: Exception) {
                showError("Unable to load payments right now.")
            }
        }
    }

    private fun savePayment() {
        val clientIndex = binding.spClient.selectedItemPosition - 1
        val amountValue = binding.etAmount.text.toString().trim().toDoubleOrNull()
        val method = binding.spMethod.selectedItem?.toString().orEmpty()
        val status = binding.spStatus.selectedItem?.toString().orEmpty()
        val reference = binding.etReference.text.toString().trim()

        if (clientIndex !in clients.indices) {
            showError("Please select a client.")
            return
        }
        if (amountValue == null || amountValue <= 0.0) {
            showError("Amount must be a positive number.")
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSavePayment.isEnabled = false
        binding.tvError.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.createPayment(
                    Payment(
                        client = Client(id = clients[clientIndex].id),
                        amount = amountValue.toBigDecimal(),
                        method = method,
                        status = status,
                        reference = reference.ifBlank { null }
                    )
                )
                if (response.isSuccessful) {
                    binding.spClient.setSelection(0)
                    binding.etAmount.text?.clear()
                    binding.spMethod.setSelection(0)
                    binding.spStatus.setSelection(0)
                    binding.etReference.text?.clear()
                    showMessage("Payment recorded successfully.")
                    loadPayments()
                } else {
                    showError("Failed to record payment. Please try again.")
                }
            } catch (e: Exception) {
                showError("Failed to record payment. Please try again.")
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnSavePayment.isEnabled = true
            }
        }
    }

    private fun renderPayments(payments: List<Payment>) {
        binding.paymentsListContainer.removeAllViews()
        binding.tvEmptyState.isVisible = payments.isEmpty()

        payments.forEach { payment ->
            val title = "$${payment.amount}"
            val subtitle =
                "${payment.client?.name ?: getString(cit.edu.vestil.worklog.R.string.no_client)} • ${payment.method ?: getString(cit.edu.vestil.worklog.R.string.cash)} • ${payment.status ?: getString(cit.edu.vestil.worklog.R.string.pending)}"
            val meta = payment.reference ?: getString(cit.edu.vestil.worklog.R.string.no_reference)
            RowRenderer.addRow(binding.paymentsListContainer, title, subtitle, meta)
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
