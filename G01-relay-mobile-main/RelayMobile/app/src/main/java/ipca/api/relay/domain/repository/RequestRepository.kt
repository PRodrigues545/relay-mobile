package ipca.api.relay.domain.repository

import ipca.api.relay.domain.model.ResourceRequest

interface RequestRepository {
    suspend fun getRequests(): List<ResourceRequest>
    suspend fun createRequest(title: String, description: String): ResourceRequest
    suspend fun deleteRequest(id: Int): Result<Unit>
}
