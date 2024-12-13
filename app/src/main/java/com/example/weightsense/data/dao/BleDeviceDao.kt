package com.example.weightsense.data.dao

import androidx.room.*
import com.example.weightsense.data.model.BleDevice
import com.example.weightsense.data.model.BleDeviceStatus 

import kotlinx.coroutines.flow.Flow

@Dao
interface BleDeviceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: BleDevice)

    @Query("SELECT * FROM ble_devices")
    fun getAllDevices(): Flow<List<BleDevice>>

    @Query("DELETE FROM ble_devices WHERE address = :deviceAddress")
    suspend fun deleteDevice(deviceAddress: String)

    @Query("SELECT * FROM ble_devices WHERE address = :address")
    suspend fun getDeviceByAddress(address: String): BleDevice?

    @Query("SELECT * FROM ble_devices WHERE status = :status")
    fun getDevicesByStatus(status: BleDeviceStatus): Flow<List<BleDevice>>

    @Query("DELETE FROM ble_devices")
    suspend fun deleteAllDevices()

    @Query("DELETE FROM ble_devices WHERE status = :status")
    suspend fun deleteDevicesByStatus(status: BleDeviceStatus)
}
