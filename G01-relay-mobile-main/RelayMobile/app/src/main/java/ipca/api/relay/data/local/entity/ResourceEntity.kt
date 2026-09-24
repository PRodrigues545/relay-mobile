package ipca.api.relay.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "resources")
data class ResourceEntity(
    @PrimaryKey
    val id: Int,
    val title: String,
    val description: String,
    val price: Double?,
    val category: String,
    val categoryId: Int,
    val imageUrl: String?,
    val isAvailable: Boolean,
    val type: String,
    val condition: String?,
    val borrowDuration: String?,
    val zones: String?,
    val ownerId: Int,
    val ownerName: String,
    val ownerUsername: String,
    val ownerReputation: Int,
    val ownerPicture: String?,
    val cachedAt: Long,
)
