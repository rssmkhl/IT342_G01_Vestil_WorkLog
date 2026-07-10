package cit.edu.vestil.worklog.data.model

data class WorkLog(
    val id: Long? = null,
    val title: String = "",
    val description: String? = null,
    val date: String? = null,
    val hours: Double? = null,
    val status: String? = null,
    val project: String? = null,
    val client: Client? = null,
    val createdAt: String? = null
)
