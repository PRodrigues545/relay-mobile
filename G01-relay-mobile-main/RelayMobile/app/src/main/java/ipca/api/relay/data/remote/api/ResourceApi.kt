package ipca.api.relay.data.remote.api

import ipca.api.relay.data.remote.model.CategoryDto
import ipca.api.relay.data.remote.model.CreateResourceRequest
import ipca.api.relay.data.remote.model.ResourceDTO
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ResourceApi {
    @GET("resources/all")
    suspend fun getResources(
        @Query("type") type: String? = null,
        @Query("category") category: String? = null
    ): List<ResourceDTO>

    @GET("resources/{id}")
    suspend fun getResourceById(@Path("id") id: Int): ResourceDTO

    @POST("resources/create")
    suspend fun createResource(@Body request: CreateResourceRequest): ResourceDTO

    @Multipart
    @POST("resources/{id}/image")
    suspend fun uploadImage(@Path("id") id: Int, @Part file: MultipartBody.Part): ResourceDTO

    @PUT("resources/edit/{id}")
    suspend fun updateResource(@Path("id") id: Int, @Body request: CreateResourceRequest): ResourceDTO

    @DELETE("resources/delete/{id}")
    suspend fun deleteResource(@Path("id") id: Int): Response<Unit>

    @GET("resources/filter")
    suspend fun filterResources(
        @Query("ownerId") ownerId: Int? = null,
        @Query("type") type: String? = null,
        @Query("status") status: Int? = null
    ): List<ResourceDTO>

    @GET("resources/categories")
    suspend fun getCategories(): List<CategoryDto>
}