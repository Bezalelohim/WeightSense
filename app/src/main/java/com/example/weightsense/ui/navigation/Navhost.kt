package com.example.weightsense.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.weightsense.ui.device.DevicesScreen
import com.example.weightsense.ui.home.HomeScreen
import com.example.weightsense.ui.settings.SettingsScreen

@Composable
fun WeightSenseNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onNavigateToDevices = { navController.navigate("devices") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("devices") {
            DevicesScreen(
                onNavigateToHome = { navController.navigate("home") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("settings") {
            SettingsScreen(
                onNavigateToHome = { navController.navigate("home") },
                onNavigateToDevices = { navController.navigate("devices") }
            )
        }
    }
}
