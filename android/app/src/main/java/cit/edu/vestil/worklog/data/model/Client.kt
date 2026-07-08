package cit.edu.vestil.worklog.data.model

import java.time.LocalDateTime

data class Client(
    val id: Long? = null,
    val name: String,
    val email: String,
    val phone: String,
    val address: String? = null,
    val createdAt: LocalDateTime? = null
)
