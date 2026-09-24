package ipca.api.relay.domain.model

enum class TransactionType { PURCHASE, BORROW }

data class Transaction(
    val id: Int,
    val type: TransactionType,
    val resourceId: Int,
    val counterpartyId: Int,
    val isBuyer: Boolean,   // true = buyer/borrower, false = seller/owner
    val agreedPrice: Double?,
    val statusName: String,
    val createdAt: String,
    val startDate: String? = null,
    val endDate: String? = null
)