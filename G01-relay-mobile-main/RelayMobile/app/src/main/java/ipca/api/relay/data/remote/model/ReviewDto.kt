package ipca.api.relay.data.remote.model

import com.google.gson.annotations.SerializedName

data class ReviewDto(
    val id: Int,
    val score: Int,
    val comment: String?,
    @SerializedName("transaction_type") val transactionType: String,
    @SerializedName("reviewed_as") val reviewedAs: String,
    @SerializedName("reviewer_username") val reviewerUsername: String?,
    @SerializedName("reviewer_name") val reviewerName: String?,
    @SerializedName("reviewer_picture") val reviewerPicture: String?,
    @SerializedName("created_at") val createdAt: String
)

data class PendingReviewDto(
    val id: Int,
    @SerializedName("transaction_id") val transactionId: Int,
    @SerializedName("transaction_type") val transactionType: String,
    @SerializedName("reviewed_as") val reviewedAs: String,
    @SerializedName("reviewed_username") val reviewedUsername: String?,
    @SerializedName("reviewed_picture") val reviewedPicture: String?
)

data class SubmitReviewRequest(
    @SerializedName("transactionId") val transactionId: Int,
    @SerializedName("transactionType") val transactionType: String,
    val score: Int,
    val comment: String?
)
