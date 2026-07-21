package cit.edu.vestil.worklog.data.model

data class AuditLogEntry(
    val id: Long,
    val timestamp: String? = null,
    val adminName: String? = null,
    val action: String? = null,
    val targetRecord: String? = null
)
