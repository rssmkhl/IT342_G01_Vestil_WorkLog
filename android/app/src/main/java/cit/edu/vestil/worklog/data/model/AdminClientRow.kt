package cit.edu.vestil.worklog.data.model

data class AdminClientRow(
    val id: Long,
    val name: String,
    val email: String,
    val phone: String? = null,
    val company: String? = null,
    val notes: String? = null,
    val userName: String? = null,
    val status: String? = null
)
