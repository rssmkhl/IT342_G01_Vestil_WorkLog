package cit.edu.vestil.worklog.data.model

import java.math.BigDecimal

data class DashboardSummary(
    val totalClients: Long = 0,
    val totalWorkLogs: Long = 0,
    val totalPayments: BigDecimal = BigDecimal.ZERO
)
