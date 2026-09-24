package ipca.api.relay.domain.repository

import android.net.Uri
import ipca.api.relay.data.remote.model.UserProfileDto
import ipca.api.relay.domain.model.Course
import ipca.api.relay.domain.model.PublicUserProfile
import ipca.api.relay.domain.model.University

interface UserRepository {
    suspend fun getUniversities(): Result<List<University>>
    suspend fun getCoursesByUniversity(universityId: Int): Result<List<Course>>
    suspend fun uploadAvatar(uri: Uri): Result<String>

    suspend fun getMe(): Result<UserProfileDto>

    suspend fun getUser(id: Int): Result<PublicUserProfile>

    suspend fun updateMe(
        name: String?,
        address: String?,
        courseId: Int?,
        universityId: Int?
    ): Result<UserProfileDto>
}
