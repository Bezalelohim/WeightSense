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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

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

            // Cylinder Weight Settings Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.cylinder_weights),
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    // Gross Cylinder Weight (Full cylinder with gas)
                    OutlinedTextField(
                        value = uiState.fullCylinderWeight.toString(),
                        onValueChange = { 
                            it.toFloatOrNull()?.let { weight ->
                                viewModel.updateFullCylinderWeight(weight)
                            }
                        },
                        label = { Text(stringResource(R.string.gross_cylinder_weight)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    // Pure Gas Weight
                    OutlinedTextField(
                        value = uiState.netWeight.toString(),
                        onValueChange = { 
                            it.toFloatOrNull()?.let { weight ->
                                viewModel.updateNetWeight(weight)
                            }
                        },
                        label = { Text(stringResource(R.string.pure_gas_weight)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    // Display calculated tare weight (empty cylinder weight)
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.tare_weight_label),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = stringResource(
                                    R.string.tare_weight_display,
                                    "%.1f".format(uiState.fullCylinderWeight - uiState.netWeight)
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            // Weight Unit Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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
            }
            
            Button(
                onClick = { viewModel.saveSettings() },
                modifier = Modifier.fillMaxWidth()
            ) {
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
