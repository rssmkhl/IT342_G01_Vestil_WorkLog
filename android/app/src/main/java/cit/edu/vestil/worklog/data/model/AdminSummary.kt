package cit.edu.vestil.worklog.data.model

data class AdminSummary(
    val totalUsers: Long = 0,
    val totalClients: Long = 0,
    val totalWorkLogs: Long = 0,
    val pendingPayments: Long = 0
)
