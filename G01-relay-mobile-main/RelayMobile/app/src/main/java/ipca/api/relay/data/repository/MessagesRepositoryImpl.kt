package ipca.api.relay.data.repository

import ipca.api.relay.data.remote.api.MessagesApi
import ipca.api.relay.data.remote.model.CreateConversationRequest
import ipca.api.relay.data.remote.model.RespondToOfferRequest
import ipca.api.relay.data.remote.model.SendOfferRequest
import ipca.api.relay.domain.model.Conversation
import ipca.api.relay.domain.model.Message
import ipca.api.relay.domain.repository.MessagesRepository

class MessagesRepositoryImpl(
    private val api: MessagesApi
): MessagesRepository{
    override suspend fun getConversations(): Result<List<Conversation>> {
        return try {
            Result.success(api.getConversations().map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMessages(conversationId: String): Result<List<Message>> {
        return try {
            Result.success(api.getMessages(conversationId).map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createConversation(
        resourceId: Int,
        resourceTitle: String,
        sellerId: Int
    ): Result<Conversation> {
        return try {
            val dto = api.createConversation(
                CreateConversationRequest(resourceId, resourceTitle, sellerId)
            )
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendOffer(
        conversationId: String,
        offerPrice: Double
    ): Result<Unit> {
        return try {
            api.sendOffer(conversationId, SendOfferRequest(offerPrice))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun respondToOffer(
        conversationId: String,
        messageId: String,
        accept: Boolean
    ): Result<Message> {
        return try {
            val dto = api.respondToOffer(
                conversationId,
                messageId,
                RespondToOfferRequest(accept)
            )
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}