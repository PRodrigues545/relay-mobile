package ipca.api.relay.data.remote.api

import ipca.api.relay.data.remote.model.ConversationDto
import ipca.api.relay.data.remote.model.CreateConversationRequest
import ipca.api.relay.data.remote.model.MessageDto
import ipca.api.relay.data.remote.model.RespondToOfferRequest
import ipca.api.relay.data.remote.model.SendOfferRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface MessagesApi {
    @GET("messaging/conversations")
    suspend fun getConversations(): List<ConversationDto>

    @GET("messaging/conversations/{conversationId}/messages")
    suspend fun getMessages(
        @Path("conversationId") conversationId: String
    ): List<MessageDto>

    @POST("messaging/conversations")
    suspend fun createConversation(
        @Body request: CreateConversationRequest
    ): ConversationDto

    @POST("messaging/conversations/{conversationId}/offer")
    suspend fun sendOffer(
        @Path("conversationId") conversationId: String,
        @Body body: SendOfferRequest
    ): Response<Unit>
    @PATCH("messaging/conversations/{conversationId}/offer/{messageId}")
    suspend fun respondToOffer(
        @Path("conversationId") conversationId: String,
        @Path("messageId") messageId: String,
        @Body body: RespondToOfferRequest
    ): MessageDto
}