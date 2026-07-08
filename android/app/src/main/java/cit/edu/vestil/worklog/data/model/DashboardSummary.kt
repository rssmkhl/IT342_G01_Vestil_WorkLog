package cit.edu.vestil.worklog.data.model

import java.math.BigDecimal

data class DashboardSummary(
    val totalClients: Long,
    val totalWorkLogs: Long,
    val totalPayments: BigDecimal,
    val totalHours: Double,
    val recentWorkLogs: List<WorkLog>,
    val recentPayments: List<Payment>
)
