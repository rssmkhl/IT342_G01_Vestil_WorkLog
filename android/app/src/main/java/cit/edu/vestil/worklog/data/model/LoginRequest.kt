package cit.edu.vestil.worklog.data.model

data class LoginRequest(
    val usernameOrEmail: String,
    val password: String
)
