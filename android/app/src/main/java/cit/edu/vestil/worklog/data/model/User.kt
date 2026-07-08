package cit.edu.vestil.worklog.data.model

data class User(
    val id: Long,
    val fullName: String,
    val email: String,
    val username: String,
    val role: String
)
