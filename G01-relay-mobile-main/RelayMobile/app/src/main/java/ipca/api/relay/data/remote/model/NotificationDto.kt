package ipca.api.relay.data.remote.model

import com.google.gson.annotations.SerializedName
import ipca.api.relay.domain.model.Notification

data class NotificationDto (
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    val type: String,
    val title: String,
    val body: String,
    @SerializedName("is_read") val isRead: Boolean,
    @SerializedName("created_at") val createdAt: String = ""
){
    fun toDomain() = Notification(
        id = id,
        userId = userId,
        type = type,
        title = title,
        body = body,
        isRead = isRead,
        createdAt = createdAt
    )
}