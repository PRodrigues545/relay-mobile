package ipca.api.relay.presentation.create

import ipca.api.relay.domain.model.ResourceRequest

data class CreateRequestUiState(
    val title: String = "",
    val description: String = "",
    val requests: List<ResourceRequest> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val currentUserId: Int = -1,
    val pendingDeleteId: Int? = null,
    val deleteInProgress: Boolean = false
)