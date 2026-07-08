package cit.edu.vestil.worklog.data.model

import java.math.BigDecimal

data class Payment(
    val id: Long? = null,
    val client: Client? = null,
    val amount: BigDecimal = BigDecimal.ZERO,
    val paymentDate: String? = null,
    val method: String? = null,
    val status: String? = null,
    val reference: String? = null,
    val createdAt: String? = null
)
