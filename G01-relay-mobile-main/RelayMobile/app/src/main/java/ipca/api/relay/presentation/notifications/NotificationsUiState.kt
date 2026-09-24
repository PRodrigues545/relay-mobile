package ipca.api.relay.presentation.notifications

import ipca.api.relay.domain.model.Notification

data class NotificationsUiState(
    val notifications: List<Notification> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val unreadCount: Int = 0
)