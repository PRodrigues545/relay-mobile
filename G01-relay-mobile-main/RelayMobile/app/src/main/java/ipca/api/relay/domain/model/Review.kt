package ipca.api.relay.domain.model

data class Review(
    val id: Int,
    val score: Int,
    val comment: String?,
    val reviewerUsername: String?,
    val reviewerName: String?,
    val reviewerPicture: String?,
    val reviewedAs: String,
    val transactionType: String
)

data class PendingReview(
    val id: Int,
    val transactionId: Int,
    val transactionType: String,
    val reviewedAs: String,
    val reviewedUsername: String?,
    val reviewedPicture: String?
)