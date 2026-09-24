package ipca.api.relay.presentation.messages.messages

import ipca.api.relay.domain.model.Message

data class MessagesUiState(
    val messages: List<Message> = emptyList(),
    val body: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val conversationTitle: String = "",
    val otherUserAvatar: String? = null,
    val otherUserUsername: String? = null,
    val otherUserDisplayName: String? = null
)
