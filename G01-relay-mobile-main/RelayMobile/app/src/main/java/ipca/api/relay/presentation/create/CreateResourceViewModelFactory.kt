package ipca.api.relay.presentation.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ipca.api.relay.domain.repository.ResourceRepository

class CreateResourceViewModelFactory(
    private val repository: ResourceRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateResourceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateResourceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}