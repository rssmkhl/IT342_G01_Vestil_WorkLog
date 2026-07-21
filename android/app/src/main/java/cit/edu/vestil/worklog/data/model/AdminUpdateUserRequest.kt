package cit.edu.vestil.worklog.data.model

data class AdminUpdateUserRequest(
    val fullName: String? = null,
    val username: String? = null,
    val email: String? = null,
    val role: String? = null
)
