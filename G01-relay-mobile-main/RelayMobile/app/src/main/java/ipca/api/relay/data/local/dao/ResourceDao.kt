package ipca.api.relay.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import ipca.api.relay.data.local.entity.ResourceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ResourceDao {

    @Query("SELECT * FROM resources ORDER BY cachedAt DESC")
    fun observeAll(): Flow<List<ResourceEntity>>

    @Query("SELECT * FROM resources ORDER BY cachedAt DESC")
    suspend fun getAll(): List<ResourceEntity>

    @Query("SELECT * FROM resources WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): ResourceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ResourceEntity>)

    @Query("DELETE FROM resources")
    suspend fun clear()

    @Query("DELETE FROM resources WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Transaction
    suspend fun replaceAll(items: List<ResourceEntity>) {
        clear()
        upsertAll(items)
    }
}
