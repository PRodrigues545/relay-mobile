package ipca.api.relay.data.remote.model

import com.google.gson.annotations.SerializedName
import ipca.api.relay.domain.model.Conversation

data class OtherUserDto(
    val id: Int,
    val username: String?,
    val name: String?,
    @SerializedName("profile_picture") val profilePicture: String?
)

data class ConversationDto(
    @SerializedName("_id") val id: String,
    @SerializedName("resource_id") val resourceId: Int,
    @SerializedName("resource_title") val resourceTitle: String,
    @SerializedName("buyer_id") val buyerId: Int,
    @SerializedName("seller_id") val sellerId: Int,
    @SerializedName("last_message_at") val lastMessageAt: String,
    @SerializedName("other_user") val otherUser: OtherUserDto?
) {
    fun toDomain(): Conversation {
        val rawName = otherUser?.name.orEmpty()
        val safeUsername = otherUser?.username.orEmpty()
        val displayName = rawName.takeIf { it.isNotBlank() }
            ?: safeUsername.takeIf { it.isNotBlank() }
        return Conversation(
            id = id,
            resourceId = resourceId,
            resourceTitle = resourceTitle,
            buyerId = buyerId,
            sellerId = sellerId,
            lastMessageAt = lastMessageAt,
            otherUserUsername = otherUser?.username,
            otherUserDisplayName = displayName,
            otherUserAvatar = otherUser?.profilePicture
        )
    }
}
