package cit.edu.vestil.worklog.ui.worklog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cit.edu.vestil.worklog.data.model.WorkLog
import cit.edu.vestil.worklog.databinding.ItemWorkLogBinding

class WorkLogAdapter(private val workLogs: List<WorkLog>) :
    RecyclerView.Adapter<WorkLogAdapter.WorkLogViewHolder>() {

    inner class WorkLogViewHolder(private val binding: ItemWorkLogBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(workLog: WorkLog) {
            binding.tvClientName.text = workLog.client?.name ?: "Unknown"
            binding.tvDate.text = workLog.date.toString()
            binding.tvDescription.text = workLog.description
            binding.tvHours.text = "${workLog.hours} hours"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkLogViewHolder {
        val binding = ItemWorkLogBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WorkLogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WorkLogViewHolder, position: Int) {
        holder.bind(workLogs[position])
    }

    override fun getItemCount(): Int = workLogs.size
}
