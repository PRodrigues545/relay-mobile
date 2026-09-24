package ipca.api.relay.presentation.reviews.write

data class WriteReviewUiState(
    val transactionId: Int,
    val transactionType: String,
    val reviewedName: String,
    val reviewedPicture: String?,
    val score: Int = 0,                // 0 means "not yet picked"
    val comment: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
) {
    val canSubmit: Boolean
        get() = !isSubmitting && score in 1..5
}
