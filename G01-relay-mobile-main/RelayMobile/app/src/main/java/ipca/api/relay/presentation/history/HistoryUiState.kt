package ipca.api.relay.presentation.history

import ipca.api.relay.domain.model.Transaction
import ipca.api.relay.domain.model.TransactionType

enum class HistoryFilter { ALL, PURCHASES, BORROWINGS }

data class HistoryUiState(
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val filter: HistoryFilter = HistoryFilter.ALL
) {
    val filtered: List<Transaction> get() = when (filter) {
        HistoryFilter.ALL -> transactions
        HistoryFilter.PURCHASES -> transactions.filter { it.type == TransactionType.PURCHASE }
        HistoryFilter.BORROWINGS -> transactions.filter { it.type == TransactionType.BORROW }
    }
}