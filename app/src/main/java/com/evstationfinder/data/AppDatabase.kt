package com.evstationfinder.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface StationDao {
    @Query("SELECT * FROM stations ORDER BY distanceKm ASC")
    fun observeAll(): Flow<List<StationEntity>>

    @Query("SELECT * FROM stations WHERE id = :id")
    suspend fun get(id: String): StationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stations: List<StationEntity>)

    @Query("UPDATE stations SET availableSlots = :availableSlots WHERE id = :id")
    suspend fun updateAvailability(id: String, availableSlots: Int)

    @Query("SELECT COUNT(*) FROM stations")
    suspend fun count(): Int
}

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles ORDER BY name ASC")
    fun observeAll(): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles WHERE id = :id")
    suspend fun get(id: String): VehicleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vehicles: List<VehicleEntity>)
}

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<BookingEntity>>

    @Query("SELECT COUNT(*) FROM bookings WHERE stationId = :stationId AND date = :date AND slotId = :slotId AND status = 'upcoming'")
    suspend fun activeSlotCount(stationId: String, date: String, slotId: String): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(booking: BookingEntity)

    @Query("UPDATE bookings SET status = 'cancelled' WHERE id = :id AND status = 'upcoming'")
    suspend fun cancel(id: String)

    @Query("SELECT * FROM bookings WHERE id = :id")
    suspend fun get(id: String): BookingEntity?
}

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profile ORDER BY id ASC")
    fun observeAll(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profile WHERE isActive = 1 LIMIT 1")
    fun observeActive(): Flow<ProfileEntity?>

    @Query("SELECT COUNT(*) FROM profile")
    suspend fun count(): Int

    @Query("UPDATE profile SET isActive = 0")
    suspend fun clearActive()

    @Query("UPDATE profile SET isActive = 1 WHERE id = :id")
    suspend fun setActive(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(profile: ProfileEntity)
}

@Database(
    entities = [StationEntity::class, VehicleEntity::class, ProfileEntity::class, BookingEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stationDao(): StationDao
    abstract fun vehicleDao(): VehicleDao
    abstract fun bookingDao(): BookingDao
    abstract fun profileDao(): ProfileDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "ev_station_finder.db"
            )
            .fallbackToDestructiveMigration()
            .build().also { instance = it }
        }
    }
}
