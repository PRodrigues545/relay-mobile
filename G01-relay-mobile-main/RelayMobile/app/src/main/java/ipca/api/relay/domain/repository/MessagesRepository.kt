package ipca.api.relay.domain.repository

import ipca.api.relay.domain.model.Conversation
import ipca.api.relay.domain.model.Message

interface MessagesRepository {
    suspend fun getConversations(): Result<List<Conversation>>
    suspend fun getMessages(conversationId: String): Result<List<Message>>
    suspend fun createConversation(
        resourceId: Int,
        resourceTitle: String,
        sellerId: Int
    ): Result<Conversation>
    suspend fun sendOffer(
        conversationId: String,
        offerPrice: Double
    ): Result<Unit>
    suspend fun respondToOffer(
        conversationId: String,
        messageId: String,
        accept: Boolean
    ): Result<Message>
}