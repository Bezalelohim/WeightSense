package com.example.weightsense.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.time.Instant
import java.util.UUID

@Entity(tableName = "ble_devices")
@TypeConverters(Converters::class)
data class BleDevice(
    @PrimaryKey val address: String,
    val name: String,
    val serviceUuid: UUID,
    val characteristicUuid: UUID,
    val lastConnected: Instant,
    val status: BleDeviceStatus
)

enum class BleDeviceStatus {
    CONNECTED,
    DISCONNECTED,
    // Add other statuses as needed
}
