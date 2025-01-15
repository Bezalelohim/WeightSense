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
import androidx.core.app.ActivityCompat
import android.util.Log
import kotlinx.coroutines.flow.asStateFlow
import android.bluetooth.le.ScanSettings
import android.bluetooth.le.ScanFilter
import android.bluetooth.BluetoothGattDescriptor
import java.nio.ByteOrder

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
        }
    }

    // Add connected device address state
    private val _connectedDeviceAddress = MutableStateFlow<String?>(null)
    val connectedDeviceAddress: StateFlow<String?> = _connectedDeviceAddress.asStateFlow()

    // Data operations
    fun readWeightData() {
        if (!checkBluetoothPermission()) return
        weightCharacteristic?.let { characteristic ->
            gatt?.readCharacteristic(characteristic)
        }
    }

    // Call this method when you receive weight data from the BLE device
    fun updateWeightData(weight: Float) {
        _weightData.value = "%.2f".format(weight).toFloat()  // Format to 2 decimal places
        Log.d("BleManager", "Weight data updated: %.2f".format(weight))
    }

    // Add this method to set the weight characteristic
    fun setWeightCharacteristic(characteristic: BluetoothGattCharacteristic) {
        weightCharacteristic = characteristic
    }

    // Add this method to set the BluetoothGatt
    fun setGatt(bluetoothGatt: BluetoothGatt) {
        gatt = bluetoothGatt
    }

    // Connect to a BLE device
    fun connect(device: BluetoothDevice) {
        if (!checkBluetoothPermission()) {
            Log.e("BleManager", "Missing permissions for connection")
            return
        }
        
        // Disconnect existing connection if any
        disconnect()
        
        // Connect with auto connect parameter set to true for better reliability
        gatt = device.connectGatt(context, true, gattCallback, BluetoothDevice.TRANSPORT_LE)
        _connectedDeviceAddress.value = device.address
        Log.d("BleManager", "Attempting to connect to device: ${device.address}")
    }

    // Disconnect from the BLE device
    fun disconnect() {
        try {
            gatt?.let { gatt ->
                gatt.disconnect()
                gatt.close()
            }
            gatt = null
            weightCharacteristic = null
            _isConnected.value = false
            _connectedDeviceAddress.value = null
            Log.d("BleManager", "Disconnected from device")
        } catch (e: Exception) {
            Log.e("BleManager", "Error during disconnect: ${e.message}")
        }
    }

    // Start scanning for BLE devices
    fun startScan() {
        bluetoothLeScanner?.startScan(scanFilters, scanSettings, scanCallback)
        Log.d("BleManager", "Started scanning for BLE devices")
    }

    // Stop scanning for BLE devices
    fun stopScan() {
        bluetoothLeScanner?.stopScan(scanCallback)
        Log.d("BleManager", "Stopped scanning for BLE devices")
    }

    // Override the onCharacteristicChanged method to log received weight data
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    when (newState) {
                        BluetoothGatt.STATE_CONNECTED -> {
                            Log.d("BleManager", "Connected to GATT server")
                            _isConnected.value = true
                            _connectedDeviceAddress.value = gatt.device.address
                            // Add delay before discovering services
                            Thread.sleep(600)
                            gatt.discoverServices()
                        }
                        BluetoothGatt.STATE_DISCONNECTED -> {
                            Log.d("BleManager", "Disconnected from GATT server")
                            _isConnected.value = false
                            _connectedDeviceAddress.value = null
                            disconnect()
                        }
                    }
                }
                else -> {
                    Log.e("BleManager", "Error $status encountered for connection state change")
                    _isConnected.value = false
                    // Retry connection on error
                    if (status == 133 || status == 22) {
                        Log.d("BleManager", "Retrying connection...")
                        disconnect()
                        // Retry connection after a short delay
                        Thread.sleep(1000)
                        gatt.device?.let { device ->
                            connect(device)
                        }
                    } else {
                        disconnect()
                    }
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BleManager", "Services discovered")
                // Find the weight service and characteristic
                val service = gatt.getService(UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b"))
                if (service == null) {
                    Log.e("BleManager", "Weight service not found")
                    return
                }
                
                val characteristic = service.getCharacteristic(
                    UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")
                )
                
                if (characteristic != null) {
                    weightCharacteristic = characteristic
                    // Enable notifications
                    if (gatt.setCharacteristicNotification(characteristic, true)) {
                        Log.d("BleManager", "Notifications enabled for weight characteristic")
                        
                        // Get the Client Characteristic Configuration Descriptor
                        val descriptor = characteristic.getDescriptor(
                            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
                        )
                        
                        if (descriptor != null) {
                            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            gatt.writeDescriptor(descriptor)
                            Log.d("BleManager", "Notification descriptor written")
                        }
                    } else {
                        Log.e("BleManager", "Failed to enable notifications")
                    }
                } else {
                    Log.e("BleManager", "Weight characteristic not found")
                }
            } else {
                Log.e("BleManager", "Service discovery failed with status: $status")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            if (characteristic.uuid == UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")) {
                val value = characteristic.value
                if (value != null && value.size == 4) {
                    val weightValue = bytesToFloat(value)
                    Log.d("BleManager", "Received weight value: %.2f".format(weightValue))
                    updateWeightData(weightValue)
                }
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (characteristic.uuid == UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")) {
                    val value = characteristic.value
                    if (value != null && value.size == 4) {
                        val weightValue = bytesToFloat(value)
                        Log.d("BleManager", "Read weight value: %.2f".format(weightValue))
                        updateWeightData(weightValue)
                    }
                }
            }
        }
    }

    // Permission checks
    fun checkBluetoothEnabled() = bluetoothAdapter?.isEnabled == true

    private fun checkBluetoothPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Helper function to convert bytes to float (Little Endian)
    private fun bytesToFloat(bytes: ByteArray): Float {
        return ByteBuffer.wrap(bytes)
            .order(ByteOrder.LITTLE_ENDIAN)  // Specify little endian byte order
            .float
    }
}

