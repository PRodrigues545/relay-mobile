package ipca.api.relay.data.remote.model

import com.google.gson.annotations.SerializedName
import ipca.api.relay.domain.model.Resource

data class ResourceDTO(
    val id: Int,
    val title: String,
    val description: String?,
    val price: Double?,
    val type: String?,
    val condition: String?,

    @SerializedName("borrow_duration")
    val borrowDuration: String?,

    val zones: String?,

    @SerializedName("image_url")
    val imageUrl: String?,

    @SerializedName("owner_id")
    val ownerId: Int,

    @SerializedName("owner_name")
    val ownerName: String?,

    @SerializedName("owner_username")
    val ownerUsername: String?,

    @SerializedName("owner_reputation")
    val ownerReputation: Int?,

    @SerializedName("owner_profile_picture")
    val ownerPicture: String?,

    @SerializedName("category_id")
    val categoryId: Int?,

    @SerializedName("status_id")
    val statusId: Int?,

    @SerializedName("category_name")
    val categoryName: String?,

    @SerializedName("status_name")
    val statusName: String?
)

fun ResourceDTO.toDomain(): Resource {
    return Resource(
        id = this.id,
        title = this.title,
        description = this.description ?: "",
        price = this.price,
        type = this.type ?: "SELL",
        condition = this.condition,
        borrowDuration = this.borrowDuration,
        zones = this.zones,
        category = this.categoryName ?: "General",
        categoryId = this.categoryId ?: 0,
        imageUrl = this.imageUrl,
        isAvailable = this.statusId == 1 || this.statusName == "AVAILABLE",
        ownerId = this.ownerId,
        ownerName = this.ownerName.orEmpty(),
        ownerUsername = this.ownerUsername.orEmpty(),
        ownerReputation = this.ownerReputation ?: 75,
        ownerPicture = this.ownerPicture
    )
}