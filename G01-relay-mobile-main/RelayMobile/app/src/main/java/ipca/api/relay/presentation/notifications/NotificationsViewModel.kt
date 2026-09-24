package ipca.api.relay.presentation.notifications

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ipca.api.relay.di.AppModule
import ipca.api.relay.domain.repository.NotificationRepository
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val repository: NotificationRepository,
) : ViewModel() {

    var uiState by mutableStateOf(NotificationsUiState())
        private set


    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            repository.getMyNotifications()
                .onSuccess { notifications ->
                    uiState = uiState.copy(
                        notifications = notifications,
                        unreadCount = notifications.count { !it.isRead },
                        isLoading = false
                    )
                }
                .onFailure {
                    uiState = uiState.copy(error = it.message, isLoading = false)
                }
        }
    }

    fun markAsRead(id: Int) {
        viewModelScope.launch {
            repository.markAsRead(id)
                .onSuccess {
                    uiState = uiState.copy(
                        notifications = uiState.notifications.map {
                            if (it.id == id) it.copy(isRead = true) else it
                        },
                        unreadCount = uiState.unreadCount - 1
                    )
                }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            repository.markAllAsRead()
                .onSuccess {
                    uiState = uiState.copy(
                        notifications = uiState.notifications.map { it.copy(isRead = true) },
                        unreadCount = 0
                    )
                }
        }
    }


    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                NotificationsViewModel(repository = AppModule.notificationRepository)
            }
        }
    }
}