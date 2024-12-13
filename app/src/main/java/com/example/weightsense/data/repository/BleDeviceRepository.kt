package com.example.weightsense.data.repository

import com.example.weightsense.data.dao.BleDeviceDao
import com.example.weightsense.data.model.BleDevice
import com.example.weightsense.data.model.BleDeviceStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton
import java.time.Instant

@Singleton
class BleDeviceRepository @Inject constructor(
    private val bleDeviceDao: BleDeviceDao
) {
    fun getAllDevices(): Flow<List<BleDevice>> = bleDeviceDao.getAllDevices()

    suspend fun insertDevice(device: BleDevice) = bleDeviceDao.insertDevice(device)

    suspend fun deleteDevice(device: BleDevice) = bleDeviceDao.deleteDevice(device.address)

    suspend fun getDeviceByAddress(address: String): BleDevice? = bleDeviceDao.getDeviceByAddress(address)

    suspend fun updateDeviceStatus(address: String, status: BleDeviceStatus) {
        bleDeviceDao.getDeviceByAddress(address)?.let { device ->
            bleDeviceDao.insertDevice(device.copy(status = status))
        }
    }

    suspend fun updateLastConnected(address: String) {
        bleDeviceDao.getDeviceByAddress(address)?.let { device ->
            bleDeviceDao.insertDevice(device.copy(lastConnected = Instant.now()))
        }
    }

    fun getConnectedDevices(): Flow<List<BleDevice>> = bleDeviceDao.getDevicesByStatus(
        BleDeviceStatus.CONNECTED)

    suspend fun deleteAllDevices() = bleDeviceDao.deleteAllDevices()

    suspend fun clearDisconnectedDevices() = bleDeviceDao.deleteDevicesByStatus(BleDeviceStatus.DISCONNECTED)
}
