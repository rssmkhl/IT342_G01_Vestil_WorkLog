package cit.edu.vestil.worklog.data.model

data class AdminWorkLogRow(
    val id: Long,
    val title: String,
    val project: String? = null,
    val date: String? = null,
    val hours: Double? = null,
    val status: String? = null,
    val userName: String? = null,
    val clientName: String? = null
)
