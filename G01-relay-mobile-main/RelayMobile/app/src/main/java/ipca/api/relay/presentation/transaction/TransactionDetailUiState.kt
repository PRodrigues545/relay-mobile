package ipca.api.relay.presentation.transaction

import ipca.api.relay.domain.model.TransactionType

data class TransactionDetailUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val actionLoading: Boolean = false,
    val actionError: String? = null,
    val actionSuccess: String? = null,

    // Purchase fields
    val purchaseId: Int = 0,
    val buyerId: Int = 0,
    val sellerId: Int = 0,
    val resourceId: Int = 0,
    val agreedPrice: Double? = null,
    val statusName: String = "",
    val createdAt: String = "",
    val updatedAt: String? = null,

    // Borrow-only fields
    val borrowerId: Int = 0,
    val ownerId: Int = 0,
    val startDate: String? = null,
    val endDate: String? = null,

    // Current user role
    val currentUserId: Int = 0,
    val type: TransactionType = TransactionType.PURCHASE
) {
    val isBuyer get() = when (type) {
        TransactionType.PURCHASE -> currentUserId == buyerId
        TransactionType.BORROW -> currentUserId == borrowerId
    }

    val isSeller get() = !isBuyer

    // Which actions are available based on status and role
    val availableActions: List<TransactionAction> get() = when (type) {
        TransactionType.PURCHASE -> purchaseActions()
        TransactionType.BORROW -> borrowActions()
    }

    private fun purchaseActions(): List<TransactionAction> = buildList {
        when (statusName) {
            "AWAITING_SELLER" -> {
                if (isSeller) {
                    add(TransactionAction.APPROVE)
                    add(TransactionAction.REJECT)
                }
                if (isBuyer) add(TransactionAction.CANCEL)
            }
            "REQUESTED" -> {
                if (isBuyer) add(TransactionAction.CANCEL)
            }
            "APPROVED" -> {
                if (isBuyer) add(TransactionAction.COMPLETE)
            }
        }
    }

    private fun borrowActions(): List<TransactionAction> = buildList {
        when (statusName) {
            "AWAITING_OWNER" -> {
                if (isSeller) {
                    add(TransactionAction.APPROVE)
                    add(TransactionAction.REJECT)
                }
                if (isBuyer) add(TransactionAction.CANCEL)
            }
            "REQUESTED" -> {
                if (isBuyer) add(TransactionAction.CANCEL)
            }
            "ACTIVE" -> {
                if (isBuyer) add(TransactionAction.RETURN)
            }
            "RETURNED" -> {
                if (isSeller) add(TransactionAction.COMPLETE)
            }
        }
    }
}

enum class TransactionAction {
    APPROVE, REJECT, CANCEL, COMPLETE, RETURN
}