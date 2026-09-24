package ipca.api.relay.presentation.transaction

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ipca.api.relay.data.local.TokenManager
import ipca.api.relay.di.AppModule
import ipca.api.relay.domain.model.TransactionType
import ipca.api.relay.domain.repository.OperationRepository
import ipca.api.relay.presentation.messages.messages.decodeUserId
import kotlinx.coroutines.launch

class TransactionDetailViewModel(
    private val id: Int,
    private val type: TransactionType,
    private val repository: OperationRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    var uiState by mutableStateOf(TransactionDetailUiState(type = type))
        private set

    init {
        uiState = uiState.copy(currentUserId = decodeUserId(tokenManager.getAccessToken() ?: ""))
        loadDetail()
    }

    fun loadDetail() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            when (type) {
                TransactionType.PURCHASE -> repository.getPurchaseById(id)
                    .onSuccess { dto ->
                        uiState = uiState.copy(
                            isLoading = false,
                            purchaseId = dto.id,
                            buyerId = dto.buyerId,
                            sellerId = dto.sellerId ?: 0,
                            resourceId = dto.resourceId,
                            agreedPrice = dto.agreedPrice,
                            statusName = dto.statusName,
                            createdAt = dto.createdAt,
                            updatedAt = dto.updatedAt
                        )
                    }
                    .onFailure { uiState = uiState.copy(isLoading = false, error = it.message) }

                TransactionType.BORROW -> repository.getBorrowById(id)
                    .onSuccess { dto ->
                        uiState = uiState.copy(
                            isLoading = false,
                            borrowerId = dto.borrowerId,
                            ownerId = dto.ownerId ?: 0,
                            resourceId = dto.resourceId,
                            statusName = dto.statusName,
                            createdAt = dto.createdAt,
                            updatedAt = dto.updatedAt,
                            startDate = dto.startDate,
                            endDate = dto.endDate
                        )
                    }
                    .onFailure { uiState = uiState.copy(isLoading = false, error = it.message) }
            }
        }
    }

    fun onAction(action: TransactionAction) {
        viewModelScope.launch {
            uiState = uiState.copy(actionLoading = true, actionError = null, actionSuccess = null)

            val result = when (action) {
                TransactionAction.APPROVE -> when (type) {
                    TransactionType.PURCHASE -> repository.approvePurchase(id)
                        .map { it.statusName }
                    TransactionType.BORROW -> repository.approveBorrow(id)
                        .map { it.statusName }
                }
                TransactionAction.REJECT -> when (type) {
                    TransactionType.PURCHASE -> repository.rejectPurchase(id)
                        .map { it.statusName }
                    TransactionType.BORROW -> repository.rejectBorrow(id)
                        .map { it.statusName }
                }
                TransactionAction.CANCEL -> when (type) {
                    TransactionType.PURCHASE -> repository.cancelPurchase(id)
                        .map { it.statusName }
                    TransactionType.BORROW -> repository.cancelBorrow(id)
                        .map { it.statusName }
                }
                TransactionAction.COMPLETE -> when (type) {
                    TransactionType.PURCHASE -> repository.completePurchase(id)
                        .map { it.statusName }
                    TransactionType.BORROW -> repository.completeBorrow(id)
                        .map { it.statusName }
                }
                TransactionAction.RETURN -> repository.returnBorrow(id)
                    .map { it.statusName }
            }

            result
                .onSuccess { newStatus ->
                    uiState = uiState.copy(
                        actionLoading = false,
                        statusName = newStatus,
                        actionSuccess = actionSuccessMessage(action)
                    )
                }
                .onFailure {
                    uiState = uiState.copy(
                        actionLoading = false,
                        actionError = it.message ?: "Action failed"
                    )
                }
        }
    }

    private fun actionSuccessMessage(action: TransactionAction) = when (action) {
        TransactionAction.APPROVE -> "Approved successfully"
        TransactionAction.REJECT  -> "Rejected"
        TransactionAction.CANCEL  -> "Cancelled"
        TransactionAction.COMPLETE -> "Completed!"
        TransactionAction.RETURN -> "Return registered"
    }

    companion object {
        fun factory(id: Int, type: TransactionType) = viewModelFactory {
            initializer {
                TransactionDetailViewModel(
                    id = id,
                    type = type,
                    repository = AppModule.operationRepository,
                    tokenManager = AppModule.getTokenManager()
                )
            }
        }
    }
}