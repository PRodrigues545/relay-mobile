package ipca.api.relay.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ipca.api.relay.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM current_user WHERE id = 0 LIMIT 1")
    fun observeCurrent(): Flow<UserEntity?>

    @Query("SELECT * FROM current_user WHERE id = 0 LIMIT 1")
    suspend fun getCurrent(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: UserEntity)

    @Query("DELETE FROM current_user")
    suspend fun clear()
}
