package ipca.api.relay.data.remote.model

import com.google.gson.annotations.SerializedName
import ipca.api.relay.domain.model.Message

data class MessageDto(
    @SerializedName("_id") val id: String,
    @SerializedName("conversation_id") val conversationId: String,
    @SerializedName("sender_id") val senderId: Int,
    val body: String,
    val type: String = "MESSAGE",
    @SerializedName("offer_price") val offerPrice: Double? = null,
    @SerializedName("offer_status") val offerStatus: String? = null,
    val read: Boolean = false,
    @SerializedName("created_at") val createdAt: String = ""
) {
    fun toDomain() = Message(
        id = id,
        conversationId = conversationId,
        senderId = senderId,
        body = body,
        type = type,
        offerPrice = offerPrice,
        offerStatus = offerStatus,
        read = read,
        createdAt = createdAt
    )
}

data class CreateConversationRequest(
    @SerializedName("resource_id") val resourceId: Int,
    @SerializedName("resource_title") val resourceTitle: String,
    @SerializedName("seller_id") val sellerId: Int
)

data class SendMessageRequest(val body: String)

data class SendOfferRequest(
    @SerializedName("offerPrice") val offerPrice: Double
)

data class RespondToOfferRequest(val accept: Boolean)