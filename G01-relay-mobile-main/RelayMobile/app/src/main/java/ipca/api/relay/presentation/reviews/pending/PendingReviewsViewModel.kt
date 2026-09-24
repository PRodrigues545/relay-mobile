package ipca.api.relay.presentation.reviews.pending

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ipca.api.relay.di.AppModule
import ipca.api.relay.domain.repository.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PendingReviewsViewModel(
    private val reviews: ReviewRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PendingReviewsUiState())
    val uiState = _uiState.asStateFlow()

    init { reload() }

    /**
     * Reloads from the server. Called on init and from the screen's
     * lifecycle observer when the user returns from WriteReviewScreen, so
     * a freshly submitted review disappears from the list without manual
     * fiddling.
     */
    fun reload() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            reviews.getPendingReviews()
                .onSuccess { items ->
                    _uiState.update { it.copy(isLoading = false, items = items) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Could not load pending reviews"
                        )
                    }
                }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PendingReviewsViewModel(AppModule.reviewRepository)
            }
        }
    }
}
