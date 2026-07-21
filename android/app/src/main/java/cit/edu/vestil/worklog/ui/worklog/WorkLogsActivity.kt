package cit.edu.vestil.worklog.ui.worklog

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import cit.edu.vestil.worklog.data.api.RetrofitClient
import cit.edu.vestil.worklog.data.model.WorkLog
import cit.edu.vestil.worklog.databinding.ActivityWorkLogsBinding
import cit.edu.vestil.worklog.ui.common.ApiErrorParser
import cit.edu.vestil.worklog.ui.common.RowRenderer
import cit.edu.vestil.worklog.ui.common.SessionNavigator
import cit.edu.vestil.worklog.ui.navigation.AppNavigator
import kotlinx.coroutines.launch

class WorkLogsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWorkLogsBinding
    private val dateRegex = Regex("""\d{4}-\d{2}-\d{2}""")
    private var editingWorkLogId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkLogsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppNavigator.setup(binding.root, this, AppNavigator.WORK_LOGS)
        binding.btnSaveWorkLog.setOnClickListener { saveWorkLog() }
        binding.btnCancelEdit.setOnClickListener { resetForm() }

        loadWorkLogs()
    }

    private fun loadWorkLogs() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getWorkLogs()
                if (response.isSuccessful) {
                    renderWorkLogs(response.body().orEmpty())
                } else {
                    if (!SessionNavigator.handleUnauthorized(this@WorkLogsActivity, response.code())) {
                        showError(ApiErrorParser.getErrorMessage(response, getString(cit.edu.vestil.worklog.R.string.unable_to_load_work_logs)))
                    }
                }
            } catch (e: Exception) {
                showError(ApiErrorParser.getThrowableMessage(e, getString(cit.edu.vestil.worklog.R.string.unable_to_load_work_logs)))
            }
        }
    }

    private fun saveWorkLog() {
        val title = binding.etTitle.text.toString().trim()
        val project = binding.etProject.text.toString().trim()
        val date = binding.etDate.text.toString().trim()
        val hoursText = binding.etHours.text.toString().trim()
        val hoursValue = hoursText.toDoubleOrNull()
        val status = binding.etStatus.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        if (title.isEmpty()) {
            showError("Please enter valid work log details.")
            return
        }
        if (date.isNotBlank() && !dateRegex.matches(date)) {
            showError(getString(cit.edu.vestil.worklog.R.string.invalid_date))
            return
        }
        if (hoursText.isNotBlank() && (hoursValue == null || hoursValue < 0.0)) {
            showError(getString(cit.edu.vestil.worklog.R.string.invalid_hours))
            return
        }

        binding.tvError.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSaveWorkLog.isEnabled = false

        lifecycleScope.launch {
            try {
                val payload = WorkLog(
                    title = title,
                    project = project.ifBlank { null },
                    date = date.ifBlank { null },
                    hours = hoursValue ?: 0.0,
                    status = status.ifBlank { null },
                    description = description.ifBlank { null }
                )
                val response = if (editingWorkLogId != null) {
                    RetrofitClient.apiService.updateWorkLog(editingWorkLogId!!, payload)
                } else {
                    RetrofitClient.apiService.createWorkLog(payload)
                }
                if (response.isSuccessful) {
                    showMessage(
                        if (editingWorkLogId != null) {
                            getString(cit.edu.vestil.worklog.R.string.work_log_updated)
                        } else {
                            getString(cit.edu.vestil.worklog.R.string.work_log_saved)
                        }
                    )
                    resetForm()
                    loadWorkLogs()
                } else {
                    if (!SessionNavigator.handleUnauthorized(this@WorkLogsActivity, response.code())) {
                        showError(ApiErrorParser.getErrorMessage(response, "Please enter valid work log details."))
                    }
                }
            } catch (e: Exception) {
                showError(ApiErrorParser.getThrowableMessage(e, "Please enter valid work log details."))
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
            val meta = listOfNotNull(
                workLog.status ?: getString(cit.edu.vestil.worklog.R.string.in_progress),
                workLog.date,
                workLog.description
            ).joinToString(" • ")
            RowRenderer.addRow(
                binding.workLogsListContainer,
                workLog.title,
                subtitle,
                meta,
                actions = listOf(
                    RowRenderer.RowAction(getString(cit.edu.vestil.worklog.R.string.edit)) {
                        populateForm(workLog)
                    },
                    RowRenderer.RowAction(getString(cit.edu.vestil.worklog.R.string.delete)) {
                        deleteWorkLog(workLog)
                    }
                )
            )
        }
    }

    private fun deleteWorkLog(workLog: WorkLog) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.deleteWorkLog(workLog.id ?: return@launch)
                if (response.isSuccessful) {
                    if (editingWorkLogId == workLog.id) {
                        resetForm()
                    }
                    showMessage(getString(cit.edu.vestil.worklog.R.string.work_log_deleted))
                    loadWorkLogs()
                } else if (!SessionNavigator.handleUnauthorized(this@WorkLogsActivity, response.code())) {
                    showError(ApiErrorParser.getErrorMessage(response, "Unable to delete this work log."))
                }
            } catch (e: Exception) {
                showError(ApiErrorParser.getThrowableMessage(e, "Unable to delete this work log."))
            }
        }
    }

    private fun populateForm(workLog: WorkLog) {
        editingWorkLogId = workLog.id
        binding.etTitle.setText(workLog.title)
        binding.etProject.setText(workLog.project.orEmpty())
        binding.etDate.setText(workLog.date.orEmpty())
        binding.etHours.setText(workLog.hours?.toString().orEmpty())
        binding.etStatus.setText(workLog.status.orEmpty())
        binding.etDescription.setText(workLog.description.orEmpty())
        binding.btnSaveWorkLog.text = getString(cit.edu.vestil.worklog.R.string.update_work_log)
        binding.btnCancelEdit.visibility = View.VISIBLE
    }

    private fun resetForm() {
        editingWorkLogId = null
        binding.etTitle.text?.clear()
        binding.etProject.text?.clear()
        binding.etDate.text?.clear()
        binding.etHours.text?.clear()
        binding.etStatus.setText(getString(cit.edu.vestil.worklog.R.string.in_progress))
        binding.etDescription.text?.clear()
        binding.btnSaveWorkLog.text = getString(cit.edu.vestil.worklog.R.string.save_work_log)
        binding.btnCancelEdit.visibility = View.GONE
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
