package cit.edu.vestil.worklog.data.model

import com.google.gson.annotations.SerializedName

data class AdminSummary(
    @SerializedName("totalFreelancers")
    val totalFreelancers: Long = 0,
    @SerializedName("activeClients")
    val activeClients: Long = 0,
    @SerializedName("workLogsThisWeek")
    val workLogsThisWeek: Long = 0,
    val pendingPayments: Long = 0,
    val totalRevenue: Double = 0.0,
    val activeProjects: Long = 0
)
