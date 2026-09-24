package ipca.api.relay.data.remote.model

import ipca.api.relay.domain.model.User
import com.google.gson.annotations.SerializedName

data class AvatarResponse(
    val avatarUrl: String
)

data class UserDto(
    val id: String,
    val name: String,
    val email: String
) {
    fun toUser(): User {
        return User(id = id, name = name, email = email)
    }
}
data class RoleDto(
    val id: Int,
    val name: String
)
data class CourseDto(
    val id: Int,
    val name: String
)

data class UniversityDto(
    val id: Int,
    val name: String
)

data class UserProfileDto(
    val id: Int,
    val name: String?,
    val username: String?,
    val email: String?,
    val reputation: Int?,
    val address: String?,
    val profilePicture: String?,
    val course: CourseDto?,
    val university: UniversityDto?,
    val role: RoleDto?,
    val createdAt: String?
)

data class UpdateUserProfileRequest(
    val name: String?,
    val address: String?,
    val course_id: Int?,
    val university_id: Int?
)