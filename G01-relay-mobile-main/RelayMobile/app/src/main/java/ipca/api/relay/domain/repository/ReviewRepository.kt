package ipca.api.relay.domain.repository

import ipca.api.relay.domain.model.PendingReview
import ipca.api.relay.domain.model.Review

interface ReviewRepository {
    suspend fun getUserReviews(userId: Int, reviewedAs: String? = null): Result<List<Review>>
    suspend fun getPendingReviews(): Result<List<PendingReview>>
    suspend fun submitReview(transactionId: Int, transactionType: String, score: Int, comment: String?): Result<Review>
}