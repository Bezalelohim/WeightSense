package com.example.weightsense.ui.device

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalContext
import com.example.weightsense.ui.common.BlePermissionHandler
import com.example.weightsense.ui.navigation.BottomNavigationBar
import androidx.compose.ui.res.stringResource
import com.example.weightsense.R
import android.util.Log
import android.os.Build


@Composable
fun DevicesScreen(
    modifier: Modifier = Modifier,
    viewModel: DevicesViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    BlePermissionHandler {
        DevicesContent(
            modifier = modifier,
            viewModel = viewModel,
            onNavigateToHome = onNavigateToHome,
            onNavigateToSettings = onNavigateToSettings
        )
    }
}

@Composable
fun DevicesContent(
    modifier: Modifier = Modifier,
    viewModel: DevicesViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val devices by viewModel.devices.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()

    LaunchedEffect(devices) {
        Log.d("DevicesScreen", "UI updated with ${devices.size} devices")
        devices.forEach { device ->
            Log.d("DevicesScreen", "Device: ${device.address}")
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.scan_for_devices),
            style = MaterialTheme.typography.headlineSmall,
            
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(16.dp)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(devices) { device ->
                DeviceItem(
                    device = device,
                    onConnectClick = { viewModel.connectToDevice(device) }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { viewModel.startScan() },
                enabled = !isScanning
            ) {
                Text(
                    if (isScanning) 
                        stringResource(R.string.scanning) 
                    else 
                        stringResource(R.string.scan_for_devices)
                )
            }
        }

        BottomNavigationBar(
            onNavigateToHome = onNavigateToHome,
            onNavigateToDevices = {},
            onNavigateToSettings = onNavigateToSettings,
            currentRoute = "devices"
        )
    }
}

@Composable
fun DeviceItem(
    device: BluetoothDevice,
    onConnectClick: () -> Unit,
    viewModel: DevicesViewModel = hiltViewModel()
) {
    val connectedAddress by viewModel.connectedDeviceAddress.collectAsState()
    val isConnected = device.address == connectedAddress
    val context = LocalContext.current
    val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
    } else {
        ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                if (hasPermission) {
                    Text(
                        text = device.name ?: stringResource(R.string.unknown_device),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = device.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = stringResource(R.string.bluetooth_permission_required),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            if (hasPermission) {
                Button(
                    onClick = { 
                        if (isConnected) {
                            viewModel.disconnectDevice(device)
                        } else {
                            onConnectClick()
                        }
                    },
                    modifier = Modifier.padding(start = 8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isConnected) Icons.Default.Close else Icons.Default.CheckCircle,
                            contentDescription = if (isConnected) "Disconnect" else "Connect",
                            modifier = Modifier.size(16.dp),
                            tint = LocalContentColor.current
                        )
                        Text(
                            text = if (isConnected) "Disconnect" else "Connect"
                        )
                    }
                }
            }
        }
    }
}
