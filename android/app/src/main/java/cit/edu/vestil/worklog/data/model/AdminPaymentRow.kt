package cit.edu.vestil.worklog.data.model

import java.math.BigDecimal

data class AdminPaymentRow(
    val id: Long,
    val amount: BigDecimal = BigDecimal.ZERO,
    val paymentDate: String? = null,
    val method: String? = null,
    val status: String? = null,
    val reference: String? = null,
    val userName: String? = null,
    val clientName: String? = null
)
