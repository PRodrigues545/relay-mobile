package ipca.api.relay.presentation.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ipca.api.relay.data.local.TokenManager
import ipca.api.relay.domain.repository.RequestRepository

class CreateRequestViewModelFactory(
    private val repository: RequestRepository,
    private val tokenManager: TokenManager,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateRequestViewModel::class.java)) {
            return CreateRequestViewModel(repository, tokenManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
