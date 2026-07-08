package cit.edu.vestil.worklog.ui.worklog

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import cit.edu.vestil.worklog.data.api.RetrofitClient
import cit.edu.vestil.worklog.data.model.WorkLog
import cit.edu.vestil.worklog.databinding.ActivityWorkLogsBinding
import cit.edu.vestil.worklog.ui.common.RowRenderer
import cit.edu.vestil.worklog.ui.navigation.AppNavigator
import kotlinx.coroutines.launch

class WorkLogsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWorkLogsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkLogsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppNavigator.setup(binding.root, this, AppNavigator.WORK_LOGS)
        binding.btnSaveWorkLog.setOnClickListener { saveWorkLog() }

        loadWorkLogs()
    }

    private fun loadWorkLogs() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getWorkLogs()
                if (response.isSuccessful) {
                    renderWorkLogs(response.body().orEmpty())
                } else {
                    showMessage("Unable to load work logs right now.")
                }
            } catch (e: Exception) {
                showMessage("Unable to load work logs right now.")
            }
        }
    }

    private fun saveWorkLog() {
        val title = binding.etTitle.text.toString().trim()
        val project = binding.etProject.text.toString().trim()
        val date = binding.etDate.text.toString().trim()
        val hoursText = binding.etHours.text.toString().trim()
        val status = binding.etStatus.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        if (title.isEmpty()) {
            showMessage("Please enter valid work log details.")
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSaveWorkLog.isEnabled = false

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.createWorkLog(
                    WorkLog(
                        title = title,
                        project = project.ifBlank { null },
                        date = date.ifBlank { null },
                        hours = hoursText.toDoubleOrNull() ?: 0.0,
                        status = status.ifBlank { null },
                        description = description.ifBlank { null }
                    )
                )
                if (response.isSuccessful) {
                    binding.etTitle.text?.clear()
                    binding.etProject.text?.clear()
                    binding.etDate.text?.clear()
                    binding.etHours.text?.clear()
                    binding.etStatus.setText(getString(cit.edu.vestil.worklog.R.string.in_progress))
                    binding.etDescription.text?.clear()
                    showMessage("Work log added successfully.")
                    loadWorkLogs()
                } else {
                    showMessage("Please enter valid work log details.")
                }
            } catch (e: Exception) {
                showMessage("Please enter valid work log details.")
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnSaveWorkLog.isEnabled = true
            }
        }
    }

    private fun renderWorkLogs(workLogs: List<WorkLog>) {
        binding.workLogsListContainer.removeAllViews()
        binding.tvEmptyState.isVisible = workLogs.isEmpty()

        workLogs.forEach { workLog ->
            val subtitle = "${workLog.project ?: getString(cit.edu.vestil.worklog.R.string.general_project)} • ${workLog.hours ?: 0.0}h"
            val meta = workLog.status ?: getString(cit.edu.vestil.worklog.R.string.in_progress)
            RowRenderer.addRow(binding.workLogsListContainer, workLog.title, subtitle, meta)
        }
    }

    private fun showMessage(message: String) {
        binding.tvMessage.text = message
        binding.tvMessage.visibility = View.VISIBLE
    }
}
