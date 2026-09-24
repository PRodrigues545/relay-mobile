package ipca.api.relay.domain.model

data class Message(
    val id: String,
    val conversationId: String,
    val senderId: Int,
    val body: String,
    val type: String,        // "MESSAGE" or "COUNTER_OFFER"
    val offerPrice: Double?,
    val offerStatus: String?,
    val read: Boolean,
    val createdAt: String
)