package com.example.weightsense.ui.device

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.weightsense.data.ble.BleManager
import com.example.weightsense.data.model.BleDevice
import com.example.weightsense.data.model.BleDeviceStatus
import com.example.weightsense.data.repository.BleDeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.UUID
import java.time.Instant
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.delay

@HiltViewModel
class DevicesViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bleManager: BleManager,
    private val bleDeviceRepository: BleDeviceRepository
) : AndroidViewModel(context as Application) {

    private val _devices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val devices: StateFlow<List<BluetoothDevice>> = _devices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _weightData = MutableStateFlow<Float?>(null)
    val weightData: StateFlow<Float?> = _weightData.asStateFlow()

    private val _connectedDevices = MutableStateFlow<List<BleDevice>>(emptyList())
    val connectedDevices: StateFlow<List<BleDevice>> = _connectedDevices.asStateFlow()

    private var currentConnectedDevice: BluetoothDevice? = null

    private val _connectedDeviceAddress = MutableStateFlow<String?>(null)
    val connectedDeviceAddress: StateFlow<String?> = _connectedDeviceAddress.asStateFlow()

    init {
        viewModelScope.launch {
            bleManager.scannedDevices
                .collect { scannedDevices ->
                    Log.d("DevicesViewModel", "Received ${scannedDevices.size} devices from BleManager")
                    scannedDevices.forEach { device ->
                        Log.d("DevicesViewModel", "Device: ${device.address}, name: ${device.name}")
                    }
                    val devicesList = scannedDevices.toMutableList()
                    currentConnectedDevice?.let { connectedDevice ->
                        if (!devicesList.any { it.address == connectedDevice.address }) {
                            devicesList.add(connectedDevice)
                        }
                    }
                    _devices.value = devicesList
                }
        }
        viewModelScope.launch {
            bleManager.weightData.collect { weight ->
                _weightData.value = weight
            }
        }
        viewModelScope.launch {
            bleDeviceRepository.getConnectedDevices().collect { devices ->
                _connectedDevices.value = devices
            }
        }
        viewModelScope.launch {
            bleManager.isConnected.collect { isConnected ->
                currentConnectedDevice?.let { device ->
                    updateConnectionState(device.address, isConnected)
                }
            }
        }
    }

    fun connectToDevice(device: BluetoothDevice) {
        viewModelScope.launch {
            try {
                bleManager.connect(device)
                currentConnectedDevice = device
                updateConnectionState(device.address, true)
                bleDeviceRepository.updateDeviceStatus(device.address, BleDeviceStatus.CONNECTED)
            } catch (e: Exception) {
                Log.e("DevicesViewModel", "Connection error: ${e.message}")
                updateConnectionState(device.address, false)
                handleConnectionError(device, e)
            }
        }
    }

    fun disconnectDevice(device: BluetoothDevice) {
        viewModelScope.launch {
            try {
                bleManager.disconnect()
                updateConnectionState(device.address, false)
                bleDeviceRepository.updateDeviceStatus(device.address, BleDeviceStatus.DISCONNECTED)
                currentConnectedDevice = null
            } catch (e: Exception) {
                Log.e("DevicesViewModel", "Disconnection error: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun handleConnectionError(device: BluetoothDevice, error: Exception) {
        viewModelScope.launch {
            bleDeviceRepository.updateDeviceStatus(device.address, BleDeviceStatus.DISCONNECTED)
            // You might want to show an error message to the user here
            error.printStackTrace()
        }
    }

    private fun checkBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun clearDisconnectedDevices() {
        viewModelScope.launch {
            bleDeviceRepository.clearDisconnectedDevices()
        }
    }

    fun readWeightData() {
        if (checkBluetoothPermissions()) {
            bleManager.readWeightData()
        }
    }

    fun startScan() {
        if (!checkBluetoothPermissions()) {
            Log.e("DevicesViewModel", "Missing permissions for scanning")
            return
        }
        
        viewModelScope.launch {
            try {
                if (!bleManager.checkBluetoothEnabled()) {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(enableBtIntent)
                    return@launch
                }
                
                _isScanning.value = true
                Log.d("DevicesViewModel", "Starting scan...")
                bleManager.startScan()
                
                // Wait for scan results
                delay(10000)
                
                // Stop scan
                stopScan()
                Log.d("DevicesViewModel", "Scan completed. Found devices: ${_devices.value.map { it.address }}")
            } catch (e: Exception) {
                Log.e("DevicesViewModel", "Scan error: ${e.message}")
                e.printStackTrace()
            } finally {
                _isScanning.value = false
            }
        }
    }

    private fun stopScan() {
        if (!checkBluetoothPermissions()) return
        bleManager.stopScan()
        _isScanning.value = false
    }

    override fun onCleared() {
        super.onCleared()
        stopScan()
        currentConnectedDevice?.let { device ->
            disconnectDevice(device)
        }
    }

    // Update this when connection state changes
    private fun updateConnectionState(deviceAddress: String, isConnected: Boolean) {
        _connectedDeviceAddress.value = if (isConnected) deviceAddress else null
    }
}
