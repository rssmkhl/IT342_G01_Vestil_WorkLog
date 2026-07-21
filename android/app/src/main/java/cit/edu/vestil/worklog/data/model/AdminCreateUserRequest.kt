package cit.edu.vestil.worklog.data.model

data class AdminCreateUserRequest(
    val fullName: String,
    val username: String,
    val email: String,
    val role: String,
    val password: String? = null
)
