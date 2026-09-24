package ipca.api.relay.presentation.reviews

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ipca.api.relay.di.AppModule
import ipca.api.relay.domain.model.Review
import ipca.api.relay.domain.repository.ReviewRepository
import ipca.api.relay.presentation.profile.ReviewTab
import kotlinx.coroutines.launch

class ReviewsViewModel(
private val repository: ReviewRepository,
private val userId: Int,
initialTab: ReviewTab = ReviewTab.SELLER
) : ViewModel() {

    var uiState by mutableStateOf(ReviewUiState(selectedTab = initialTab))
    private set

            init { loadReviews() }

    fun onTabChange(tab: ReviewTab) {
        uiState = uiState.copy(selectedTab = tab)
    }

    private fun loadReviews() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            val sellerResult = repository.getUserReviews(userId, "SELLER")
            val buyerResult = repository.getUserReviews(userId, "BUYER")
            uiState = uiState.copy(
                sellerReviews = sellerResult.getOrElse { emptyList() },
                buyerReviews = buyerResult.getOrElse { emptyList() },
                isLoading = false,
                error = sellerResult.exceptionOrNull()?.message
                    ?: buyerResult.exceptionOrNull()?.message
            )
        }
    }

    companion object {
        fun factory(userId: Int, initialTab: ReviewTab = ReviewTab.SELLER) = viewModelFactory {
            initializer {
                ReviewsViewModel(
                    repository = AppModule.reviewRepository,
                    userId = userId,
                    initialTab = initialTab
                )
            }
        }
    }
}