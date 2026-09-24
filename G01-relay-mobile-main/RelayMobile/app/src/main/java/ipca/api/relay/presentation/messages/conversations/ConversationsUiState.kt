package ipca.api.relay.presentation.messages.conversations

import ipca.api.relay.domain.model.Conversation

data class ConversationsUiState(
    val conversations: List<Conversation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)