package ipca.api.relay.data.repository

import ipca.api.relay.data.remote.api.RequestApi
import ipca.api.relay.data.remote.model.CreateRequestRequest
import ipca.api.relay.data.remote.model.toDomain // Garante que este import aponta para o mapper do ResourceRequestDTO
import ipca.api.relay.domain.model.ResourceRequest
import ipca.api.relay.domain.repository.RequestRepository
import ipca.api.relay.data.remote.model.ResourceRequestDTO

class RequestRepositoryImpl(
    private val api: RequestApi
) : RequestRepository {

    override suspend fun getRequests(): List<ResourceRequest> {
        // Forçamos o tipo para o Kotlin não usar o mapper do ResourceDTO por engano
        val dtos: List<ResourceRequestDTO> = api.getAllRequests()
        return dtos.map { it.toDomain() }
    }

    override suspend fun createRequest(title: String, description: String): ResourceRequest {
        val requestBody = CreateRequestRequest(title, description)
        val response = api.createRequest(requestBody)

        if (response.isSuccessful) {
            return ResourceRequest(
                id = 0,
                title = title,
                description = description,
                requesterId = 0,
                userName = "Eu", // Ou buscar do session manager
                createdAt = "Agora",
                userPicture = null
            )
        } else {
            throw Exception("Erro ao criar pedido: ${response.code()}")
        }
    }

    override suspend fun deleteRequest(id: Int): Result<Unit> = runCatching {
        val response = api.deleteRequest(id)
        if (!response.isSuccessful) {
            val message = when (response.code()) {
                403 -> "You can only delete your own requests"
                404 -> "This request no longer exists"
                else -> "Could not delete the request (code ${response.code()})"
            }
            throw Exception(message)
        }
    }
}
