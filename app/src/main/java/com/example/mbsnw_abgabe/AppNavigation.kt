package com.example.mbsnw_abgabe

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            TagesuebersichtScreen(
                onScanClick = { navController.navigate("camera") },
                onBluetoothClick = { navController.navigate("bluetooth") },
                onWeeklyClick = { navController.navigate("weekoverview") }
            )
        }
        composable("camera") { CameraPage() }

        composable("bluetooth") { BluetoothPage() }

        composable("weekoverview") { WeekOverview() }
    }
}