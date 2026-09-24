package ipca.api.relay.data.remote.model

import com.google.gson.annotations.SerializedName
import ipca.api.relay.domain.model.Transaction
import ipca.api.relay.domain.model.TransactionType

data class PurchaseDto(
    val id: Int,
    @SerializedName("buyer_id") val buyerId: Int,
    @SerializedName("seller_id") val sellerId: Int?,
    @SerializedName("resource_id") val resourceId: Int,
    @SerializedName("agreed_price") val agreedPrice: Double?,
    @SerializedName("status_name") val statusName: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String?,
    @SerializedName("clientSecret") val clientSecret: String? = null // 👈 novo
) {
    fun toDomain(currentUserId: Int) = Transaction(
        id = id,
        type = TransactionType.PURCHASE,
        resourceId = resourceId,
        counterpartyId = if (currentUserId == buyerId) sellerId ?: 0 else buyerId,
        isBuyer = currentUserId == buyerId,
        agreedPrice = agreedPrice,
        statusName = statusName,
        createdAt = createdAt
    )
}