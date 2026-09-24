package ipca.api.relay.data.remote.api

import ipca.api.relay.data.remote.model.PendingReviewDto
import ipca.api.relay.data.remote.model.ReviewDto
import ipca.api.relay.data.remote.model.SubmitReviewRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ReviewApi {
    @GET("reviews/user/{userId}")
    suspend fun getUserReviews(
        @Path("userId") userId: Int,
        @Query("reviewedAs") reviewedAs: String? = null
    ): List<ReviewDto>

    @GET("reviews/pending")
    suspend fun getPendingReviews(): List<PendingReviewDto>

    @POST("reviews")
    suspend fun submitReview(@Body body: SubmitReviewRequest): ReviewDto
}
