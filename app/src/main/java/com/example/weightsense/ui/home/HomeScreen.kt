package com.example.weightsense.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.weightsense.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.weightsense.ui.navigation.BottomNavigationBar
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToDevices: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val weightState by viewModel.weightState.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()
    val isConnected by viewModel.isDeviceConnected.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ConnectionStatus(isConnected)
            
            Text(
                text = stringResource(R.string.current_weight),
                style = MaterialTheme.typography.titleLarge
            )
            
            val weightLevel = (weightState.currentWeight / weightState.maxWeight)
                .coerceIn(0f, 1f)
            
            WeightIndicator(
                level = weightLevel,
                currentWeight = weightState.currentWeight,
                maxWeight = weightState.maxWeight,
                modifier = Modifier.size(200.dp)
            )
            
            WeightDisplay(
                currentWeight = weightState.currentWeight,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            )
            
            WeightInfoCard(
                maxWeight = weightState.maxWeight,
                lastSyncTime = lastSyncTime,
                modifier = Modifier.fillMaxWidth()
            )
            
            Button(
                onClick = { viewModel.readWeightData() },
                enabled = isConnected,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(stringResource(R.string.refresh_weight))
            }
        }

        BottomNavigationBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            onNavigateToHome = {},
            onNavigateToDevices = onNavigateToDevices,
            onNavigateToSettings = onNavigateToSettings,
            currentRoute = "home"
        )
    }
}

@Composable
private fun ConnectionStatus(isConnected: Boolean) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.errorContainer
        ),
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Text(
            text = if (isConnected) 
                stringResource(R.string.device_connected) 
            else 
                stringResource(R.string.device_disconnected),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (isConnected) 
                MaterialTheme.colorScheme.onPrimaryContainer 
            else 
                MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

@Composable
private fun WeightDisplay(currentWeight: Float, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "%.1f".format(currentWeight),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = stringResource(R.string.unit_kg),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun WeightInfoCard(maxWeight: Float, lastSyncTime: Long, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${stringResource(R.string.max_weight)}: $maxWeight${stringResource(R.string.unit_kg)}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.last_synced, formatLastSyncTime(lastSyncTime)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WeightIndicator(
    level: Float,
    currentWeight: Float,
    maxWeight: Float,
    modifier: Modifier = Modifier
) {
    // Get colors outside of Canvas
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    Box(
        modifier = modifier.padding(16.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw background container
            drawRect(
                color = surfaceVariantColor,
                topLeft = Offset(size.width * 0.2f, 0f),
                size = Size(size.width * 0.6f, size.height)
            )
            
            // Draw weight level with inverted gradient color based on level
            val levelColor = when {
                level < 0.3f -> Color(0xFFF44336) // Red when low
                level < 0.7f -> Color(0xFFFFC107) // Yellow when medium
                else -> Color(0xFF4CAF50) // Green when high/full
            }
            
            drawRect(
                color = levelColor,
                topLeft = Offset(size.width * 0.2f, size.height * (1f - level)),
                size = Size(size.width * 0.6f, size.height * level)
            )
            
            // Draw level markers
            for (i in 0..10) {
                val y = size.height * (i / 10f)
                drawLine(
                    color = onSurfaceVariantColor,
                    start = Offset(size.width * 0.15f, y),
                    end = Offset(size.width * 0.85f, y),
                    strokeWidth = 1f
                )
            }
        }
    }
}

private fun formatLastSyncTime(timestamp: Long): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
}
