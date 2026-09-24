package ipca.api.relay.data.repository

import ipca.api.relay.data.local.TokenManager
import ipca.api.relay.data.remote.api.OperationApi
import ipca.api.relay.data.remote.model.CreateBorrowRequest
import ipca.api.relay.domain.model.Transaction
import ipca.api.relay.domain.repository.OperationRepository
import ipca.api.relay.presentation.messages.messages.decodeUserId
import ipca.api.relay.data.remote.api.CreatePurchaseRequest

class OperationRepositoryImpl(
    private val api: OperationApi,
    private val tokenManager: TokenManager
) : OperationRepository {

    private fun currentUserId(): Int = tokenManager.getAccessToken()
        ?.let { decodeUserId(it) } ?: -1

    override suspend fun getPurchaseHistory(): Result<List<Transaction>> = try {
        val userId = currentUserId()
        Result.success(api.getPurchaseHistory().map { it.toDomain(userId) })
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun getBorrowHistory(): Result<List<Transaction>> = try {
        val userId = currentUserId()
        Result.success(api.getBorrowHistory().map { it.toDomain(userId) })
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun getPurchaseById(id: Int) = try {
        Result.success(api.getPurchaseById(id))
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun getBorrowById(id: Int) = try {
        Result.success(api.getBorrowById(id))
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun createPurchase(resourceId: Int, paymentMethod: String) = try {
        Result.success(api.createPurchase(resourceId, CreatePurchaseRequest(paymentMethod)))
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun createBorrow(
        resourceId: Int,
        startDate: String,
        endDate: String
    ) = try {
        Result.success(api.createBorrow(resourceId, CreateBorrowRequest(startDate, endDate)))
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun approvePurchase(id: Int) = try {
        Result.success(api.approvePurchase(id))
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun rejectPurchase(id: Int) = try {
        Result.success(api.rejectPurchase(id))
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun cancelPurchase(id: Int) = try {
        Result.success(api.cancelPurchase(id))
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun completePurchase(id: Int) = try {
        Result.success(api.completePurchase(id))
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun approveBorrow(id: Int) = try {
        Result.success(api.approveBorrow(id))
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun rejectBorrow(id: Int) = try {
        Result.success(api.rejectBorrow(id))
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun cancelBorrow(id: Int) = try {
        Result.success(api.cancelBorrow(id))
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun returnBorrow(id: Int) = try {
        Result.success(api.returnBorrow(id))
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun completeBorrow(id: Int) = try {
        Result.success(api.completeBorrow(id))
    } catch (e: Exception) { Result.failure(e) }
}