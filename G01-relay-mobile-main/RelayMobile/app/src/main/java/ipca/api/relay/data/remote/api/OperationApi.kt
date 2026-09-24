package ipca.api.relay.data.remote.api

import ipca.api.relay.data.remote.model.BorrowDto
import ipca.api.relay.data.remote.model.CreateBorrowRequest
import ipca.api.relay.data.remote.model.PurchaseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import com.google.gson.annotations.SerializedName

data class CreatePurchaseRequest(
    @SerializedName("paymentMethod") val paymentMethod: String
)

interface OperationApi {
    @GET("operation/purchase/history")
    suspend fun getPurchaseHistory(): List<PurchaseDto>

    @GET("operation/borrow/history")
    suspend fun getBorrowHistory(): List<BorrowDto>

    @GET("operation/purchase/{id}")
    suspend fun getPurchaseById(@Path("id") id: Int): PurchaseDto

    @GET("operation/borrow/{id}")
    suspend fun getBorrowById(@Path("id") id: Int): BorrowDto

    // Creates a purchase request. Server uses req.user.id as buyer; no body
    // needed. Returns the freshly-created row with status_name joined in.
    @POST("operation/purchase/{resourceId}")
    suspend fun createPurchase(@Path("resourceId") resourceId: Int): PurchaseDto

    // Creates a borrow request. Body carries the date range chosen by the
    // borrower. Server validates start < end.
    @POST("operation/borrow/{resourceId}")
    suspend fun createBorrow(
        @Path("resourceId") resourceId: Int,
        @Body request: CreateBorrowRequest
    ): BorrowDto

    @PATCH("operation/purchase/{id}/approve")
    suspend fun approvePurchase(@Path("id") id: Int): PurchaseDto

    @PATCH("operation/purchase/{id}/reject")
    suspend fun rejectPurchase(@Path("id") id: Int): PurchaseDto

    @PATCH("operation/purchase/{id}/cancel")
    suspend fun cancelPurchase(@Path("id") id: Int): PurchaseDto

    @PATCH("operation/purchase/{id}/complete")
    suspend fun completePurchase(@Path("id") id: Int): PurchaseDto

    @POST("operation/purchase/{resourceId}")
    suspend fun createPurchase(
        @Path("resourceId") resourceId: Int,
        @Body request: CreatePurchaseRequest
    ): PurchaseDto

    @PATCH("operation/borrow/{id}/approve")
    suspend fun approveBorrow(@Path("id") id: Int): BorrowDto

    @PATCH("operation/borrow/{id}/reject")
    suspend fun rejectBorrow(@Path("id") id: Int): BorrowDto

    @PATCH("operation/borrow/{id}/cancel")
    suspend fun cancelBorrow(@Path("id") id: Int): BorrowDto

    @PATCH("operation/borrow/{id}/return")
    suspend fun returnBorrow(@Path("id") id: Int): BorrowDto

    @PATCH("operation/borrow/{id}/complete")
    suspend fun completeBorrow(@Path("id") id: Int): BorrowDto
}