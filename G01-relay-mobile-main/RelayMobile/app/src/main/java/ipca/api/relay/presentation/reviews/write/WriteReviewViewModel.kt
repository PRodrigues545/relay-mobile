package ipca.api.relay.presentation.reviews.write

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

class WriteReviewViewModel(
    transactionId: Int,
    transactionType: String,
    reviewedName: String,
    reviewedPicture: String?,
    private val reviews: ReviewRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        WriteReviewUiState(
            transactionId = transactionId,
            transactionType = transactionType,
            reviewedName = reviewedName,
            reviewedPicture = reviewedPicture,
        )
    )
    val uiState = _uiState.asStateFlow()

    fun onScoreChange(score: Int) {
        _uiState.update { it.copy(score = score, error = null) }
    }

    fun onCommentChange(comment: String) {
        _uiState.update { it.copy(comment = comment) }
    }

    fun onSubmit() {
        val s = _uiState.value
        if (!s.canSubmit) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            reviews.submitReview(
                transactionId = s.transactionId,
                transactionType = s.transactionType,
                score = s.score,
                comment = s.comment.trim().takeIf { it.isNotBlank() }
            )
                .onSuccess {
                    _uiState.update { it.copy(isSubmitting = false, isSuccess = true) }
                }
                .onFailure { e ->
                    // The server returns 4xx with "already reviewed" when the
                    // user tried to submit twice. Surface the server message
                    // directly — it's already user-facing Portuguese.
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            error = e.message ?: "Could not submit review"
                        )
                    }
                }
        }
    }

    companion object {
        fun factory(
            transactionId: Int,
            transactionType: String,
            reviewedName: String,
            reviewedPicture: String?,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                WriteReviewViewModel(
                    transactionId = transactionId,
                    transactionType = transactionType,
                    reviewedName = reviewedName,
                    reviewedPicture = reviewedPicture,
                    reviews = AppModule.reviewRepository,
                )
            }
        }
    }
}