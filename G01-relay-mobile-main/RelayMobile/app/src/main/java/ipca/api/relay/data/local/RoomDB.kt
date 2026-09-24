package ipca.api.relay.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ipca.api.relay.data.local.dao.ResourceDao
import ipca.api.relay.data.local.dao.UserDao
import ipca.api.relay.data.local.entity.ResourceEntity
import ipca.api.relay.data.local.entity.UserEntity

@Database(
    entities = [
        ResourceEntity::class,
        UserEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun resourceDao(): ResourceDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "relay.db",
                ).build().also { INSTANCE = it }
            }
        }
    }
}
