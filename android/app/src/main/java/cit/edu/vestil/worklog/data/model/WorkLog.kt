package cit.edu.vestil.worklog.data.model

import java.time.LocalDate
import java.time.LocalDateTime

data class WorkLog(
    val id: Long? = null,
    val client: Client? = null,
    val date: LocalDate,
    val description: String,
    val hours: Double,
    val createdAt: LocalDateTime? = null
)
