package ipca.api.relay.domain.model

data class ResourceRequest(
    val id: Int,
    val title: String,
    val description: String,
    val requesterId: Int,
    val userName: String,
    val userPicture: String?,
    val createdAt: String
)