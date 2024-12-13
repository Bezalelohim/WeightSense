package com.example.weightsense.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.weightsense.ui.navigation.BottomNavigationBar
import androidx.compose.ui.res.stringResource
import com.example.weightsense.R
import java.util.Locale

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit,
    onNavigateToDevices: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val unitKg = stringResource(R.string.unit_kg)
    val unitLbs = stringResource(R.string.unit_lbs)
    val themeSystem = stringResource(R.string.theme_system)
    val themeLight = stringResource(R.string.theme_light)
    val themeDark = stringResource(R.string.theme_dark)

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(bottom = 80.dp), // Add padding for bottom navigation
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.settings),
                style = MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(
                value = uiState.username,
                onValueChange = { viewModel.updateUsername(it) },
                label = { Text(stringResource(R.string.username)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.currentProfile,
                onValueChange = { viewModel.updateCurrentProfile(it) },
                label = { Text(stringResource(R.string.profile)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.maxWeight.toString(),
                onValueChange = { 
                    it.toFloatOrNull()?.let { weight ->
                        viewModel.updateMaxWeight(weight)
                    }
                },
                label = { Text(stringResource(R.string.max_weight)) },
                modifier = Modifier.fillMaxWidth()
            )

            // Weight Unit Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.weight_unit))
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(
                    selected = uiState.weightUnit == unitKg,
                    onClick = { viewModel.updateWeightUnit(unitKg) }
                )
                Text(unitKg)
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(
                    selected = uiState.weightUnit == unitLbs,
                    onClick = { viewModel.updateWeightUnit(unitLbs) }
                )
                Text(unitLbs)
            }

            // Theme Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.theme))
                Spacer(modifier = Modifier.width(16.dp))
                DropdownMenu(
                    expanded = false,
                    onDismissRequest = { }
                ) {
                    listOf(themeSystem, themeLight, themeDark).forEach { theme ->
                        DropdownMenuItem(
                            text = { Text(theme) },
                            onClick = { viewModel.updateTheme(theme) }
                        )
                    }
                }
            }

            // Notifications Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.notifications))
                Spacer(modifier = Modifier.width(16.dp))
                Switch(
                    checked = uiState.notificationsEnabled,
                    onCheckedChange = { viewModel.toggleNotifications() }
                )
            }
            
            Button(onClick = { viewModel.saveSettings() }) {
                Text(stringResource(R.string.save_settings))
            }
        }

        BottomNavigationBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            onNavigateToHome = onNavigateToHome,
            onNavigateToDevices = onNavigateToDevices,
            onNavigateToSettings = {},
            currentRoute = "settings"
        )
    }
}
