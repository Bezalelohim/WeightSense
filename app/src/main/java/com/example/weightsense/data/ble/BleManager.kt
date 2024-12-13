package com.example.weightsense.data.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton
import android.Manifest
import java.nio.ByteBuffer
import java.util.UUID
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.util.Log
import kotlinx.coroutines.flow.asStateFlow
import android.bluetooth.le.ScanSettings
import android.bluetooth.le.ScanFilter

@Singleton
class BleManager @Inject constructor(
    private val context: Context
) {
    private val bluetoothManager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
    private var gatt: BluetoothGatt? = null

    // State flows
    private val _scannedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val scannedDevices: StateFlow<List<BluetoothDevice>> = _scannedDevices.asStateFlow()

    private val _weightData = MutableStateFlow<Float?>(null)
    val weightData: StateFlow<Float?> = _weightData

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private var weightCharacteristic: BluetoothGattCharacteristic? = null

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private val scanFilters = listOf(
        ScanFilter.Builder()
            .build()
    )

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            Log.d("BleManager", "Scan result received: ${result.device.address}")
            val device = result.device
            val currentList = _scannedDevices.value.toMutableList()
            if (!currentList.any { it.address == device.address }) {
                currentList.add(device)
                Log.d("BleManager", "Adding new device: ${device.address}, RSSI: ${result.rssi}")
                _scannedDevices.value = currentList
            }
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            Log.d("BleManager", "Batch scan results received: ${results.size} devices")
            val currentList = _scannedDevices.value.toMutableList()
            results.forEach { result ->
                if (!currentList.any { it.address == result.device.address }) {
                    currentList.add(result.device)
                }
            }
            _scannedDevices.value = currentList
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("BleManager", "Scan failed with error code: $errorCode")
            when (errorCode) {
                SCAN_FAILED_ALREADY_STARTED -> Log.e("BleManager", "Scan already started")
                SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> Log.e("BleManager", "App registration failed")
                SCAN_FAILED_FEATURE_UNSUPPORTED -> Log.e("BleManager", "BLE not supported")
                SCAN_FAILED_INTERNAL_ERROR -> Log.e("BleManager", "Internal error")
                SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES -> {
                    Log.e("BleManager", "No hardware resources available for scanning")
                    // Stop current scan and retry after a delay
                    stopScan()
                    // Could implement a retry mechanism here
                }
                SCAN_FAILED_SCANNING_TOO_FREQUENTLY -> {
                    Log.e("BleManager", "Scanning too frequently")
                    // Implement exponential backoff
                    // Could add a delay before next scan attempt
                }
            }
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (newState) {
                    BluetoothGatt.STATE_CONNECTED -> {
                        _isConnected.value = true
                        if (ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            return
                        }
                        gatt.discoverServices()
                        Log.d("BleManager", "Connected to device: ${gatt.device.address}")
                    }
                    BluetoothGatt.STATE_DISCONNECTED -> {
                        _isConnected.value = false
                        weightCharacteristic = null
                        Log.d("BleManager", "Disconnected from device: ${gatt.device.address}")
                    }
                }
            } else {
                Log.e("BleManager", "Connection state change failed with status: $status")
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt.getService(UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b"))
                weightCharacteristic = service?.getCharacteristic(UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8"))
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS && 
                characteristic.uuid == UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")) {
                val value = characteristic.value
                if (value != null) {
                    val weightValue = ByteBuffer.wrap(value).float
                    _weightData.value = weightValue
                    Log.d("BleManager", "Weight data read: $weightValue")
                }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            if (characteristic.uuid == UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")) {
                val value = characteristic.value
                if (value != null) {
                    val weightValue = ByteBuffer.wrap(value).float
                    _weightData.value = weightValue
                    Log.d("BleManager", "Weight data changed: $weightValue")
                }
            }
        }
    }

    // Scanning operations
    fun startScan() {
        if (!hasRequiredPermissions()) {
            Log.e("BleManager", "Missing required permissions for scanning")
            return
        }
        
        // Check permissions based on API level
        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN)== PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED
        }

        if (hasPermission) {
            _scannedDevices.value = emptyList() // Clear previous results
            Log.d("BleManager", "Starting BLE scan with settings...")
            bluetoothLeScanner?.startScan(scanFilters, scanSettings, scanCallback)
            Log.d("BleManager", "Scan started successfully")
        } else {
            Log.e("BleManager", if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) 
                "Missing BLUETOOTH_SCAN permission" 
                else "Missing ACCESS_FINE_LOCATION permission")
        }
    }

    fun stopScan() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) 
                    == PackageManager.PERMISSION_GRANTED) {
                    bluetoothLeScanner?.stopScan(scanCallback)
                }
            } else {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
                    == PackageManager.PERMISSION_GRANTED) {
                    bluetoothLeScanner?.stopScan(scanCallback)
                }
            }
        } catch (e: Exception) {
            Log.e("BleManager", "Error stopping scan: ${e.message}")
        }
    }

    // Connection operations
    fun connect(device: BluetoothDevice) {
        if (!checkBluetoothPermission()) return
        disconnect()
        gatt = device.connectGatt(context, false, gattCallback)
    }

    fun disconnect() {
        if (!checkBluetoothPermission()) return
        gatt?.disconnect()
        gatt?.close()
        gatt = null
        _isConnected.value = false
    }

    // Data operations
    fun readWeightData() {
        if (!checkBluetoothPermission()) return
        weightCharacteristic?.let { characteristic ->
            gatt?.readCharacteristic(characteristic)
        }
    }

    // Permission checks
    fun checkBluetoothEnabled() = bluetoothAdapter?.isEnabled == true

    private fun hasRequiredPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun checkBluetoothPermission() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        checkPermission(Manifest.permission.BLUETOOTH_CONNECT)
    } else true

    private fun checkPermission(permission: String) = 
        ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}

