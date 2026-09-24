package ipca.api.relay.data.repository

import ipca.api.relay.data.remote.api.ReviewApi
import ipca.api.relay.data.remote.model.SubmitReviewRequest
import ipca.api.relay.domain.model.PendingReview
import ipca.api.relay.domain.model.Review
import ipca.api.relay.domain.repository.ReviewRepository
import kotlin.collections.map

class ReviewRepositoryImpl(private val api: ReviewApi) : ReviewRepository {
    override suspend fun getUserReviews(userId: Int, reviewedAs: String?): Result<List<Review>> = try {
        Result.success(api.getUserReviews(userId, reviewedAs).map {
            Review(it.id, it.score, it.comment, it.reviewerUsername, it.reviewerName, it.reviewerPicture, it.reviewedAs, it.transactionType)
        })
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun getPendingReviews(): Result<List<PendingReview>> = try {
        Result.success(api.getPendingReviews().map {
            PendingReview(it.id, it.transactionId, it.transactionType, it.reviewedAs, it.reviewedUsername, it.reviewedPicture)
        })
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun submitReview(transactionId: Int, transactionType: String, score: Int, comment: String?): Result<Review> = try {
        val dto = api.submitReview(
            SubmitReviewRequest(
                transactionId,
                transactionType,
                score,
                comment
            )
        )
        Result.success(Review(dto.id, dto.score, dto.comment, dto.reviewerUsername, dto.reviewerName, dto.reviewerPicture, dto.reviewedAs, dto.transactionType))
    } catch (e: Exception) { Result.failure(e) }
}