package cit.edu.vestil.worklog.data.model

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

data class Payment(
    val id: Long? = null,
    val client: Client? = null,
    val amount: BigDecimal,
    val paymentDate: LocalDate,
    val status: String = "PENDING",
    val createdAt: LocalDateTime? = null
)
