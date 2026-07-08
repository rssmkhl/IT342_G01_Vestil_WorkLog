package cit.edu.vestil.worklog.data.model

data class Client(
    val id: Long? = null,
    val name: String,
    val email: String,
    val phone: String? = null,
    val company: String? = null,
    val notes: String? = null,
    val createdAt: String? = null
)
