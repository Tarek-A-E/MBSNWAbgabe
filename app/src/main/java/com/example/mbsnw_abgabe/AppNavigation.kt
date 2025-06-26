package com.example.mbsnw_abgabe

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mbsnw_abgabe.data.MealDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(mealDB: MealDatabase) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            TagesuebersichtScreen(
                mealDB = mealDB,
                onScanClick = { navController.navigate("camera") },
                onBluetoothClick = { navController.navigate("bluetooth") },
                onWeeklyClick = { navController.navigate("weekoverview") },
            )
        }
        composable("camera") {
            CameraPage(mealDB,navController, onMealScanned = { meal ->
                CoroutineScope(Dispatchers.IO).launch {
                    mealDB.dao.insertMeal(meal)
                }
            })
        }
        composable("bluetooth") { BluetoothPage() }

        composable("weekoverview") { WeekOverview(mealDB = mealDB) }
    }
}