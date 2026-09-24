package ipca.api.relay.domain.repository

import ipca.api.relay.data.remote.model.BorrowDto
import ipca.api.relay.data.remote.model.PurchaseDto
import ipca.api.relay.domain.model.Transaction

interface OperationRepository {
    suspend fun getPurchaseHistory(): Result<List<Transaction>>
    suspend fun getBorrowHistory(): Result<List<Transaction>>
    suspend fun getPurchaseById(id: Int): Result<PurchaseDto>
    suspend fun getBorrowById(id: Int): Result<BorrowDto>

    suspend fun createPurchase(resourceId: Int, paymentMethod: String): Result<PurchaseDto>
    suspend fun createBorrow(
        resourceId: Int,
        startDate: String,
        endDate: String
    ): Result<BorrowDto>

    suspend fun approvePurchase(id: Int): Result<PurchaseDto>
    suspend fun rejectPurchase(id: Int): Result<PurchaseDto>
    suspend fun cancelPurchase(id: Int): Result<PurchaseDto>
    suspend fun completePurchase(id: Int): Result<PurchaseDto>

    suspend fun approveBorrow(id: Int): Result<BorrowDto>
    suspend fun rejectBorrow(id: Int): Result<BorrowDto>
    suspend fun cancelBorrow(id: Int): Result<BorrowDto>
    suspend fun returnBorrow(id: Int): Result<BorrowDto>
    suspend fun completeBorrow(id: Int): Result<BorrowDto>
}