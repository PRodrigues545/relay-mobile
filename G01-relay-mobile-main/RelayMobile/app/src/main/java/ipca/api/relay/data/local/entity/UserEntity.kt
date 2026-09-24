package ipca.api.relay.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "current_user")
data class UserEntity(
    @PrimaryKey
    val id: Int = 0,
    val userId: Int,
    val username: String,
    val name: String?,
    val email: String,
    val profilePicture: String?,
    val reputation: Int,
    val universityName: String?,
    val courseName: String?,
)
