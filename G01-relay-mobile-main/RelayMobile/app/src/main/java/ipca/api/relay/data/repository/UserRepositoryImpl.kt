package ipca.api.relay.data.repository

import android.content.Context
import android.net.Uri
import ipca.api.relay.data.local.dao.UserDao
import ipca.api.relay.data.local.entity.UserEntity
import ipca.api.relay.data.remote.api.UserApi
import ipca.api.relay.data.remote.model.UserProfileDto
import ipca.api.relay.domain.model.PublicUserProfile
import ipca.api.relay.domain.repository.UserRepository
import retrofit2.HttpException
import java.io.IOException
import ipca.api.relay.data.remote.model.UpdateUserProfileRequest
import ipca.api.relay.domain.model.University
import ipca.api.relay.domain.model.Course

class UserRepositoryImpl(
    private val userApi: UserApi,
    private val context: Context,
    private val userCache: UserDao,
) : UserRepository {
    override suspend fun getUniversities(): Result<List<University>> {
        return try {
            Result.success(userApi.getUniversities().map { University(it.id, it.name) })
        } catch (e: HttpException) {
            Result.failure(AuthException.ServerError(e.code()))
        } catch (e: IOException) {
            Result.failure(AuthException.NetworkError(e))
        }
    }

    override suspend fun getCoursesByUniversity(universityId: Int): Result<List<Course>> {
        return try {
            Result.success(userApi.getCoursesByUniversity(universityId).map { Course(it.id, it.name) })
        } catch (e: HttpException) {
            Result.failure(AuthException.ServerError(e.code()))
        } catch (e: IOException) {
            Result.failure(AuthException.NetworkError(e))
        }
    }

    override suspend fun getMe(): Result<UserProfileDto> {
        return try {
            val dto = userApi.getMe()
            userCache.upsert(dto.toCurrentUserEntity())
            Result.success(dto)
        } catch (e: HttpException) {
            Result.failure(AuthException.ServerError(e.code()))
        } catch (e: IOException) {
            Result.failure(AuthException.NetworkError(e))
        }
    }

    override suspend fun getUser(id: Int): Result<PublicUserProfile> {
        return try {
            val dto = userApi.getUser(id)
            val rawName = dto.name.orEmpty()
            val safeUsername = dto.username.orEmpty()
            Result.success(
                PublicUserProfile(
                    id = dto.id,
                    username = safeUsername,
                    // Same fallback rule as Resource.ownerDisplayName: prefer
                    // the chosen name, fall back to username. Applied here so
                    // the screen doesn't have to know about absence.
                    displayName = rawName.takeIf { it.isNotBlank() } ?: safeUsername,
                    reputation = dto.reputation ?: 0,
                    profilePicture = dto.profilePicture,
                    universityName = dto.university?.name,
                    courseName = dto.course?.name,
                    createdAt = dto.createdAt
                )
            )
        } catch (e: HttpException) {
            Result.failure(AuthException.ServerError(e.code()))
        } catch (e: IOException) {
            Result.failure(AuthException.NetworkError(e))
        }
    }

    override suspend fun uploadAvatar(uri: Uri): Result<String> {
        return try {
            val part = uriToMultipart(context, uri) ?: return Result.failure(
                IllegalArgumentException("Could not read image file")
            )
            // Backend returns the full updated profile; extract the picture URL from it.
            val updated = userApi.uploadAvatar(part)
            val url = updated.profilePicture
                ?: return Result.failure(IllegalStateException("Server did not return a picture URL"))
            userCache.upsert(updated.toCurrentUserEntity())
            Result.success(url)
        } catch (e: HttpException) {
            Result.failure(AuthException.ServerError(e.code()))
        } catch (e: IOException) {
            Result.failure(AuthException.NetworkError(e))
        }
    }

    override suspend fun updateMe(
        name: String?,
        address: String?,
        courseId: Int?,
        universityId: Int?
    ): Result<UserProfileDto> {
        return try {
            val response = userApi.updateMe(
                UpdateUserProfileRequest(
                    name = name,
                    address = address,
                    course_id = courseId,
                    university_id = universityId
                )
            )
            // Write-through after a successful edit; next launch reads the
            // cache and shows the edited values without a round-trip.
            userCache.upsert(response.toCurrentUserEntity())
            Result.success(response)
        } catch (e: HttpException) {
            Result.failure(AuthException.ServerError(e.code()))
        } catch (e: IOException) {
            Result.failure(AuthException.NetworkError(e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

private fun UserProfileDto.toCurrentUserEntity() = UserEntity(
    id = 0,
    userId = id,
    username = username.orEmpty(),
    name = name,
    email = email.orEmpty(),
    profilePicture = profilePicture,
    reputation = reputation ?: 0,
    universityName = university?.name,
    courseName = course?.name,
)
