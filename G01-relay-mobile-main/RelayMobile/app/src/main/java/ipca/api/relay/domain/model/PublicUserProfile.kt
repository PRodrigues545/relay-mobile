package ipca.api.relay.domain.model

data class PublicUserProfile(
    val id: Int,
    val username: String,
    val displayName: String,
    val reputation: Int,
    val profilePicture: String?,
    val universityName: String?,
    val courseName: String?,
    val createdAt: String?,
)
