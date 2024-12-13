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
import androidx.compose.ui.unit.sp
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

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.current_weight),
                style = MaterialTheme.typography.titleLarge
            )
            
            // Calculate the weight level (0.0f to 1.0f)
            val weightLevel = (weightState.currentWeight / weightState.maxWeight)
                .coerceIn(0f, 1f)
            
            WeightIndicator(
                level = weightLevel,
                modifier = Modifier.size(200.dp)
            )
            
            Text(
                text = stringResource(R.string.weight_format).format(weightState.currentWeight),
                style = MaterialTheme.typography.titleMedium
            )
            
            Text(
                text = stringResource(R.string.max_weight) + ": ${weightState.maxWeight}${stringResource(R.string.unit_kg)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = stringResource(R.string.last_synced, formatLastSyncTime(lastSyncTime)),
                style = MaterialTheme.typography.bodyMedium
            )
            
            Button(
                onClick = { viewModel.readWeightData() },
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

fun formatLastSyncTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Composable
fun WeightIndicator(level: Float, modifier: Modifier = Modifier) {
    // Get colors and typography outside of the Canvas
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val labelStyle = MaterialTheme.typography.labelMedium

    Box(
        modifier = modifier.padding(16.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw background container
            drawRect(
                color = surfaceVariant,
                topLeft = Offset(size.width * 0.2f, 0f),
                size = Size(size.width * 0.6f, size.height)
            )
            
            // Draw weight level with gradient color based on level
            val levelColor = when {
                level < 0.3f -> Color.Green
                level < 0.7f -> Color.Yellow
                else -> Color.Red
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
                    color = onSurfaceVariant,
                    start = Offset(size.width * 0.15f, y),
                    end = Offset(size.width * 0.85f, y),
                    strokeWidth = 1f
                )
            }
        }
        
        // Add percentage text
        Text(
            text = "${(level * 100).toInt()}%",
            style = labelStyle,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(bottom = 8.dp)
        )
    }
}
