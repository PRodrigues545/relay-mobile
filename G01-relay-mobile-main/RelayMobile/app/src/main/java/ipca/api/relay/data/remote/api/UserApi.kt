package ipca.api.relay.data.remote.api

import ipca.api.relay.data.remote.model.CourseDto
import ipca.api.relay.data.remote.model.PublicUserProfileDto
import ipca.api.relay.data.remote.model.UniversityDto
import ipca.api.relay.data.remote.model.UserProfileDto
import okhttp3.MultipartBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PUT
import retrofit2.http.Body
import ipca.api.relay.data.remote.model.UpdateUserProfileRequest
import ipca.api.relay.domain.repository.DeviceTokenRequest
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Path

interface UserApi {
    @GET("users/universities")
    suspend fun getUniversities(): List<UniversityDto>

    @GET("users/universities/{universityId}/courses")
    suspend fun getCoursesByUniversity(
        @Path("universityId") universityId: Int
    ): List<CourseDto>

    @GET("users/me")
    suspend fun getMe(): UserProfileDto

    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: Int): PublicUserProfileDto

    @Multipart
    @POST("users/me/avatar")
    suspend fun uploadAvatar(
        @Part file: MultipartBody.Part
    ): UserProfileDto

    @PUT("users/me")
    suspend fun updateMe(
        @Body request: UpdateUserProfileRequest
    ): UserProfileDto

    @POST("users/device-token")
    suspend fun updateDeviceToken(
        @Header("Authorization") authorization: String,
        @Body body: DeviceTokenRequest
    ): Response<Unit>
}
