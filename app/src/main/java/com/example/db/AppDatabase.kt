package com.example.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import com.example.model.TransferHistoryEntity
import com.example.model.TrustedDeviceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransferHistoryDao {
    @Query("SELECT * FROM transfer_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<TransferHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: TransferHistoryEntity)

    @Query("DELETE FROM transfer_history WHERE id = :id")
    suspend fun deleteHistory(id: String)

    @Query("DELETE FROM transfer_history")
    suspend fun clearAll()
}

@Dao
interface TrustedDeviceDao {
    @Query("SELECT * FROM trusted_devices ORDER BY lastConnected DESC")
    fun getAllTrustedDevices(): Flow<List<TrustedDeviceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: TrustedDeviceEntity)

    @Query("DELETE FROM trusted_devices WHERE id = :id")
    suspend fun removeDevice(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM trusted_devices WHERE id = :id OR name = :name)")
    suspend fun isTrusted(id: String, name: String): Boolean
}

@Database(entities = [TransferHistoryEntity::class, TrustedDeviceEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): TransferHistoryDao
    abstract fun trustedDeviceDao(): TrustedDeviceDao
}
