package ipca.api.relay.presentation.history

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ipca.api.relay.di.AppModule
import ipca.api.relay.domain.repository.OperationRepository
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val repository: OperationRepository
) : ViewModel() {

    var uiState by mutableStateOf(HistoryUiState())
        private set

    init { loadHistory() }

    fun loadHistory() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            val purchases = repository.getPurchaseHistory()
                .getOrElse { emptyList() }
            val borrowings = repository.getBorrowHistory()
                .getOrElse { emptyList() }

            val all = (purchases + borrowings)
                .sortedByDescending { it.createdAt }

            uiState = uiState.copy(transactions = all, isLoading = false)
        }
    }

    fun onFilterChange(filter: HistoryFilter) {
        uiState = uiState.copy(filter = filter)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                HistoryViewModel(repository = AppModule.operationRepository)
            }
        }
    }
}