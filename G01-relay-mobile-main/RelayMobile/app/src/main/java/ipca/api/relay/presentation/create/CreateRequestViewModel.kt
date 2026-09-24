package ipca.api.relay.presentation.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ipca.api.relay.data.local.TokenManager
import ipca.api.relay.domain.repository.RequestRepository
import ipca.api.relay.presentation.messages.messages.decodeUserId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateRequestViewModel(
    private val repository: RequestRepository,
    private val tokenManager: TokenManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateRequestUiState())
    val uiState = _uiState.asStateFlow()

    init {
        val userId = tokenManager.getAccessToken()?.let { decodeUserId(it) } ?: -1
        _uiState.update { it.copy(currentUserId = userId) }
        fetchRequests()
    }

    fun onTitleChange(newTitle: String) = _uiState.update { it.copy(title = newTitle) }

    fun fetchRequests() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val list = repository.getRequests()
                _uiState.update { it.copy(requests = list, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun publishRequest() {
        val title = _uiState.value.title
        if (title.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Enviamos o título (e uma descrição vazia por agora)
                repository.createRequest(title, "")
                _uiState.update { it.copy(title = "", isSuccess = true) }
                fetchRequests() // Recarrega a lista para mostrar o novo pedido
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    // ── Delete flow ─────────────────────────────────────────────────────────
    fun requestDelete(requestId: Int) {
        _uiState.update { it.copy(pendingDeleteId = requestId, error = null) }
    }

    fun cancelDelete() {
        if (_uiState.value.deleteInProgress) return
        _uiState.update { it.copy(pendingDeleteId = null) }
    }

    fun confirmDelete() {
        val id = _uiState.value.pendingDeleteId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(deleteInProgress = true, error = null) }
            repository.deleteRequest(id)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            deleteInProgress = false,
                            pendingDeleteId = null,
                            requests = it.requests.filterNot { r -> r.id == id }
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            deleteInProgress = false,
                            pendingDeleteId = null,
                            error = e.message ?: "Could not delete the request"
                        )
                    }
                }
        }
    }
}
