package cit.edu.vestil.worklog.data.model

data class AuthResponse(
    val token: String,
    val id: Long,
    val fullName: String,
    val username: String,
    val email: String,
    val role: String
)
