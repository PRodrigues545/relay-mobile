package ipca.api.relay.data.remote.model

import com.google.gson.annotations.SerializedName
import ipca.api.relay.domain.model.Transaction
import ipca.api.relay.domain.model.TransactionType

data class BorrowDto(
    val id: Int,
    @SerializedName("borrower_id") val borrowerId: Int,
    @SerializedName("owner_id") val ownerId: Int?,
    @SerializedName("resource_id") val resourceId: Int,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String,
    @SerializedName("status_name") val statusName: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String?
) {
    fun toDomain(currentUserId: Int) = Transaction(
        id = id,
        type = TransactionType.BORROW,
        resourceId = resourceId,
        counterpartyId = if (currentUserId == borrowerId) ownerId ?: 0 else borrowerId,
        isBuyer = currentUserId == borrowerId,
        agreedPrice = null,
        statusName = statusName,
        createdAt = createdAt,
        startDate = startDate,
        endDate = endDate
    )
}