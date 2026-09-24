package ipca.api.relay.presentation.messages.conversations

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ipca.api.relay.di.AppModule
import ipca.api.relay.domain.repository.MessagesRepository
import kotlinx.coroutines.launch

class ConversationsViewModel(
    val repository: MessagesRepository
): ViewModel(){
    var uiState by mutableStateOf(ConversationsUiState())
        private set

    init {
        loadConversations()
    }

    fun loadConversations() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            repository.getConversations()
                .onSuccess { uiState = uiState.copy(conversations = it, isLoading = false) }
                .onFailure { uiState = uiState.copy(error = it.message, isLoading = false) }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ConversationsViewModel(
                    repository = AppModule.messagesRepository
                )
            }
        }
    }
}