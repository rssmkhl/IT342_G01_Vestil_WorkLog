package cit.edu.vestil.worklog.data.model

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val username: String,
    val password: String,
    val confirmPassword: String,
    val role: String = "USER"
)
