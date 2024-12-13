package com.example.weightsense.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun BottomNavigationBar(
    modifier: Modifier = Modifier,
    onNavigateToHome: () -> Unit,
    onNavigateToDevices: () -> Unit,
    onNavigateToSettings: () -> Unit,
    currentRoute: String
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NavigationIcon(
                icon = Icons.AutoMirrored.Filled.List,
                contentDescription = "Devices",
                onClick = onNavigateToDevices,
                isSelected = currentRoute == "devices"
            )
            NavigationIcon(
                icon = Icons.Default.Home,
                contentDescription = "Home",
                onClick = onNavigateToHome,
                isSelected = currentRoute == "home"
            )
            NavigationIcon(
                icon = Icons.Default.Settings,
                contentDescription = "Settings",
                onClick = onNavigateToSettings,
                isSelected = currentRoute == "settings"
            )
        }
    }
}

@Composable
private fun NavigationIcon(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    isSelected: Boolean
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
