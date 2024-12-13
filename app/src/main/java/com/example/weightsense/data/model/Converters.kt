package com.example.weightsense.data.model

import androidx.room.TypeConverter
import java.time.Instant
import java.util.UUID

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Instant?): Long? {
        return date?.toEpochMilli()
    }

    @TypeConverter
    fun fromUUID(uuid: UUID?): String? {
        return uuid?.toString()
    }

    @TypeConverter
    fun toUUID(uuid: String?): UUID? {
        return uuid?.let { UUID.fromString(it) }
    }

    @TypeConverter
    fun fromBleDeviceStatus(status: BleDeviceStatus): String {
        return status.name
    }

    @TypeConverter
    fun toBleDeviceStatus(status: String): BleDeviceStatus {
        return BleDeviceStatus.valueOf(status)
    }
}
