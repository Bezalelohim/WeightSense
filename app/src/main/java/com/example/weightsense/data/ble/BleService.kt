package com.example.weightsense.data.ble

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val bluetoothManager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private var bluetoothLeScanner: BluetoothLeScanner? = null

    private val _scanResults = MutableStateFlow<List<ScanResult>>(emptyList())
    val scanResults: StateFlow<List<ScanResult>> = _scanResults

    private val _weightData = MutableStateFlow(0f)
    val weightData: StateFlow<Float> = _weightData

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val currentList = _scanResults.value.toMutableList()
            val existingDeviceIndex = currentList.indexOfFirst { it.device.address == result.device.address }
            if (existingDeviceIndex >= 0) {
                currentList[existingDeviceIndex] = result
            } else {
                currentList.add(result)
            }
            _scanResults.value = currentList
        }
    }

    private var gatt: BluetoothGatt? = null
    private var weightCharacteristic: BluetoothGattCharacteristic? = null

    fun startScan() {
        if (bluetoothAdapter?.isEnabled == true) {
            bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Handle permission not granted
                return
            }
            bluetoothLeScanner?.startScan(scanCallback)
        }
    }

    fun stopScan() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Handle permission not granted
            return
        }
        bluetoothLeScanner?.stopScan(scanCallback)
    }

    fun readWeightData() {
        weightCharacteristic?.let { characteristic ->
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Handle permission not granted
                return@let
            }
            gatt?.readCharacteristic(characteristic)
        }
    }

    // Add this method to set the weight characteristic
    fun setWeightCharacteristic(characteristic: BluetoothGattCharacteristic) {
        weightCharacteristic = characteristic
    }

    // Add this method to set the BluetoothGatt
    fun setGatt(bluetoothGatt: BluetoothGatt) {
        gatt = bluetoothGatt
    }

    // Call this method when you receive weight data from the BLE device
    fun updateWeightData(weight: Float) {
        _weightData.value = weight
    }
}
