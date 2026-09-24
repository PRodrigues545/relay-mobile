package ipca.api.relay.domain.model

data class Notification (
    val id: Int,
    val userId: Int,
    val type: String,
    val title: String,
    val body: String,
    val isRead: Boolean,
    val createdAt: String = ""
)