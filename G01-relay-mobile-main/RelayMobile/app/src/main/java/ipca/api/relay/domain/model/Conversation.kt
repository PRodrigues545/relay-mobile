package ipca.api.relay.domain.model

data class Conversation(
    val id: String,
    val resourceId: Int,
    val resourceTitle: String,
    val buyerId: Int,
    val sellerId: Int,
    val lastMessageAt: String,
    val otherUserUsername: String? = null,
    val otherUserDisplayName: String? = null,
    val otherUserAvatar: String? = null
)
