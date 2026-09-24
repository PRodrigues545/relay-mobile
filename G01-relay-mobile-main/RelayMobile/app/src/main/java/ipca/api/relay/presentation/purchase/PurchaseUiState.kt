package ipca.api.relay.presentation.purchase

import ipca.api.relay.domain.model.PaymentMethod

data class PurchaseUiState(
    val isLoading: Boolean = true,
    val error: String? = null,

    // Loaded resource summary
    val resourceId: Int = 0,
    val title: String = "",
    val imageUrl: String? = null,
    val conditionLabel: String = "",
    val price: Double = 0.0,

    // User selections
    val selectedMeetingPoint: SelectedMeetingPoint? = null,
    val selectedPaymentMethod: PaymentMethod? = null,

    // Submission
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val createdPurchaseId: Int? = null,
    val clientSecret: String? = null
) {
    val canSubmit: Boolean
        get() = !isSubmitting &&
                selectedMeetingPoint != null &&
                selectedPaymentMethod != null
}

data class SelectedMeetingPoint(
    val id: Int,
    val name: String
)