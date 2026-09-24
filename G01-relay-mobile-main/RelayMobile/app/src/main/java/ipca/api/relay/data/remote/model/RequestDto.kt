package ipca.api.relay.data.remote.model

import com.google.gson.annotations.SerializedName
import ipca.api.relay.domain.model.ResourceRequest

data class ResourceRequestDTO(
    val id: Int,
    val title: String,
    val description: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("requester_id") val requesterId: Int?,
    @SerializedName("user_name") val userName: String?,
    @SerializedName("user_picture") val userPicture: String?
)

// Mapper para converter DTO em objeto de Domínio
fun ResourceRequestDTO.toDomain() = ResourceRequest(
    id = id,
    title = title,
    description = description ?: "",
    requesterId = requesterId ?: 0,
    userName = userName ?: "Utilizador",
    userPicture = userPicture,
    createdAt = createdAt
)