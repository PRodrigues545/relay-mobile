package ipca.api.relay.presentation.borrow

import ipca.api.relay.presentation.purchase.SelectedMeetingPoint

data class BorrowUiState(
    val isLoading: Boolean = true,
    val error: String? = null,

    val resourceId: Int = 0,
    val title: String = "",
    val imageUrl: String? = null,
    val descriptionShort: String = "",
    val borrowDuration: String = "",   // e.g. "1 semestre"; display-only
    val zones: String = "",            // owner's pickup zones, display-only

    // ISO yyyy-MM-dd. Pickers populate these; both must be set before submit.
    val startDate: String = "",
    val endDate: String = "",

    val selectedMeetingPoint: SelectedMeetingPoint? = null,

    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val createdBorrowId: Int? = null,
) {
    val canSubmit: Boolean
        get() = !isSubmitting &&
                startDate.isNotBlank() &&
                endDate.isNotBlank() &&
                endDate > startDate &&            // lexicographic works for ISO dates
                selectedMeetingPoint != null
}