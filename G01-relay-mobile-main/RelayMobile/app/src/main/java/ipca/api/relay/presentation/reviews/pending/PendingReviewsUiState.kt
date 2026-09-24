package ipca.api.relay.presentation.reviews.pending

import ipca.api.relay.domain.model.PendingReview

data class PendingReviewsUiState(
    val isLoading: Boolean = true,
    val items: List<PendingReview> = emptyList(),
    val error: String? = null,
)

