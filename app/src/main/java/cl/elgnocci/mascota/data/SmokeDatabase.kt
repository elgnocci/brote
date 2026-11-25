package cl.elgnocci.mascota.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "smoke")
data class SmokeEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val atEpochMs: Long,
)

@Entity(tableName = "craving")
data class CravingEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val atEpochMs: Long,
    val resisted: Boolean,
)

@Dao
interface SmokeDao {
    @Insert
    suspend fun insertSmoke(event: SmokeEvent)

    @Insert
    suspend fun insertSmokes(events: List<SmokeEvent>)

    @Query("SELECT * FROM smoke ORDER BY atEpochMs DESC, id DESC LIMIT 1")
    suspend fun lastSmoke(): SmokeEvent?

    @Delete
    suspend fun deleteSmoke(event: SmokeEvent)

    @Query("SELECT * FROM smoke")
    fun allSmokes(): Flow<List<SmokeEvent>>

    @Insert
    suspend fun insertCraving(event: CravingEvent)

    @Query("SELECT COUNT(*) FROM craving WHERE resisted = 1")
    fun resistedCount(): Flow<Int>
}

@Database(entities = [SmokeEvent::class, CravingEvent::class], version = 1, exportSchema = false)
abstract class SmokeDatabase : RoomDatabase() {
    abstract fun smokeDao(): SmokeDao

    companion object {
        fun open(context: Context): SmokeDatabase =
            Room.databaseBuilder(context, SmokeDatabase::class.java, "brote.db").build()
    }
}
