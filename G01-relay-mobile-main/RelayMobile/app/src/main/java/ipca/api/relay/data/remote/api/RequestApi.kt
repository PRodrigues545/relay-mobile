package ipca.api.relay.data.remote.api

import ipca.api.relay.data.remote.model.ResourceRequestDTO
import ipca.api.relay.data.remote.model.CreateRequestRequest
import retrofit2.Response  // Import que faltava
import retrofit2.http.*

interface RequestApi {
    // Adiciona a barra final para bater exatamente com o que o router espera
    @GET("requests/")
    suspend fun getAllRequests(): List<ResourceRequestDTO>

    @POST("requests/")
    suspend fun createRequest(@Body request: CreateRequestRequest): Response<Unit>

    @DELETE("requests/{id}")
    suspend fun deleteRequest(@Path("id") id: Int): Response<Unit>
}