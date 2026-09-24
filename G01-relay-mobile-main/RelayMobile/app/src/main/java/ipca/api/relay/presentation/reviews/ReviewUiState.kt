package ipca.api.relay.presentation.reviews

import ipca.api.relay.domain.model.Review
import ipca.api.relay.presentation.profile.ReviewTab

data class ReviewUiState(
    val sellerReviews: List<Review> = emptyList(),
    val buyerReviews: List<Review> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedTab: ReviewTab = ReviewTab.SELLER
) {
    val reviews: List<Review> get() = when (selectedTab) {
        ReviewTab.SELLER -> sellerReviews
        ReviewTab.CLIENT -> buyerReviews
    }
    val averageRating: Double get() = if (reviews.isEmpty()) 0.0
    else reviews.map { it.score }.average()
}