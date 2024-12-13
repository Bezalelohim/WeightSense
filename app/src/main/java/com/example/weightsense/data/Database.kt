package com.example.weightsense.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.weightsense.data.dao.BleDeviceDao
import com.example.weightsense.data.model.BleDevice
import com.example.weightsense.data.model.Converters
import android.content.Context

@Database(entities = [BleDevice::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bleDeviceDao(): BleDeviceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "weightsense_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
