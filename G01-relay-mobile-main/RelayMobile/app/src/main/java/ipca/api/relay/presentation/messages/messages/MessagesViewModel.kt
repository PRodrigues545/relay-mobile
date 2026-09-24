package ipca.api.relay.presentation.messages.messages

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ipca.api.relay.data.local.SocketManager
import ipca.api.relay.di.AppModule
import ipca.api.relay.domain.model.Message
import ipca.api.relay.domain.repository.MessagesRepository
import kotlinx.coroutines.launch

class MessagesViewModel(
    private val conversationId: String,
    private val conversationTitle: String,
    private val otherUserAvatar: String?,
    private val otherUserUsername: String?,
    private val otherUserDisplayName: String?,
    val repository: MessagesRepository
): ViewModel() {

    var uiState by mutableStateOf(
        MessagesUiState(
            conversationTitle = conversationTitle,
            otherUserAvatar = otherUserAvatar,
            otherUserUsername = otherUserUsername,
            otherUserDisplayName = otherUserDisplayName
        )
    )
        private set

    val currentUserId: Int = AppModule.getTokenManager()
        .getAccessToken()
        ?.let { decodeUserId(it) } ?: -1

    init {
        loadMessages()
        joinAndListen()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            repository.getMessages(conversationId)
                .onSuccess { uiState = uiState.copy(messages = it, isLoading = false) }
                .onFailure { uiState = uiState.copy(error = it.message, isLoading = false) }
        }
    }

    private fun joinAndListen() {
        SocketManager.joinConversation(conversationId)
        SocketManager.onNewMessage { json ->
            val newMessage = Message(
                id = json.optString("_id"),
                conversationId = conversationId,
                senderId = json.optInt("sender_id"),
                body = json.optString("body"),
                type = json.optString("type", "MESSAGE"),
                offerPrice = if (json.has("offer_price")) json.optDouble("offer_price") else null,
                offerStatus = if (json.has("offer_status")) json.optString("offer_status") else null,
                read = false,
                createdAt = json.optString("created_at")
            )
            // The socket callback fires on whatever thread io.socket dispatches
            // on. Hop back to the viewModelScope so the state mutation is
            // serialized with the rest of the VM's writes.
            viewModelScope.launch {
                uiState = uiState.copy(messages = uiState.messages + newMessage)
            }
        }
    }

    fun onBodyChange(body: String) {
        uiState = uiState.copy(body = body)
    }

    fun sendMessage() {
        val body = uiState.body.trim()
        if (body.isBlank()) return
        SocketManager.sendMessage(conversationId, body)
        uiState = uiState.copy(body = "")
    }

    fun respondToOffer(messageId: String, accept: Boolean) {
        viewModelScope.launch {
            repository.respondToOffer(conversationId, messageId, accept)
                .onSuccess { updatedMessage ->
                    uiState = uiState.copy(
                        messages = uiState.messages.map {
                            if (it.id == messageId) updatedMessage else it
                        }
                    )
                }
                .onFailure {
                    // silently fail — offer stays PENDING, user can retry
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Tell the server we're gone AND detach the new_message listener —
        // leaveConversation does both. Previously only offNewMessage was called,
        // which left the server unaware that we'd left the room (mostly cosmetic
        // but adds up on long-lived sessions).
        SocketManager.leaveConversation(conversationId)
    }

    companion object {
        fun factory(
            conversationId: String,
            conversationTitle: String,
            otherUserAvatar: String? = null,
            otherUserUsername: String? = null,
            otherUserDisplayName: String? = null,
        ) = viewModelFactory {
            initializer {
                MessagesViewModel(
                    conversationId = conversationId,
                    conversationTitle = conversationTitle,
                    otherUserAvatar = otherUserAvatar,
                    otherUserUsername = otherUserUsername,
                    otherUserDisplayName = otherUserDisplayName,
                    repository = AppModule.messagesRepository
                )
            }
        }
    }
}