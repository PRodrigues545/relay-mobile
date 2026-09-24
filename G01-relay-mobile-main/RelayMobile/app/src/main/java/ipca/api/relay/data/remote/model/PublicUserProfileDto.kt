package ipca.api.relay.data.remote.model

import com.google.gson.annotations.SerializedName

data class PublicUserProfileDto(
    val id: Int,
    val name: String?,
    val username: String?,
    val reputation: Int?,
    @SerializedName("profile_picture") val profilePicture: String?,
    val course: CourseDto?,
    val university: UniversityDto?,
    @SerializedName("created_at") val createdAt: String?,
)
