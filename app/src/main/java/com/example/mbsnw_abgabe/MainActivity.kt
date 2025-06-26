package com.example.mbsnw_abgabe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import com.example.mbsnw_abgabe.data.Meal
import com.example.mbsnw_abgabe.data.MealDatabase
import com.example.mbsnw_abgabe.data.MealRepository
import com.example.mbsnw_abgabe.ui.theme.MBSNWAbgabeTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.example.mbsnw_abgabe.data.MealDao


class MainActivity : ComponentActivity() {
    private lateinit var database: MealDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        database = Room.databaseBuilder(
            applicationContext,
            MealDatabase::class.java,
            "meals.db"
        ).build()

        setContent {
            MBSNWAbgabeTheme {
                AppNavigation(database)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagesuebersichtScreen(
    onScanClick: () -> Unit,
    onBluetoothClick: () -> Unit,
    onWeeklyClick: () -> Unit,
    mealDB: MealDatabase
) {
    var goalCalories by remember { mutableStateOf(2000) }
    var usedCalories by remember { mutableStateOf(0) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var newGoalInput by remember { mutableStateOf("") }

    // Get today's date in the correct format
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today = dateFormat.format(Date())

    // Create repository and state for meals
    val repository = remember { MealRepository(mealDB.dao) }
    var todaysMeals by remember { mutableStateOf<List<Meal>>(emptyList()) }

    // Collect meals from database
    LaunchedEffect(Unit) {
        repository.getAllMeals().collect { meals ->
            todaysMeals = meals
                .filterNotNull()
                .filter { it.date == today }
                .take(10)
        }
    }

    // Calculate used calories whenever meals change
    LaunchedEffect(Unit) {
        repository.getAllMeals().collect { meals ->
            todaysMeals = meals
                .filterNotNull()
                .filter { it.date == today }
            usedCalories = todaysMeals.sumOf { it.cal.toInt() }
        }
    }

    val remainingCalories = goalCalories - usedCalories
    val progress = (usedCalories / goalCalories.toFloat()).coerceIn(0f, 1f)

    // Goal setting dialog
    if (showGoalDialog) {
        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            title = { Text("Tägliches Kalorienziel setzen") },
            text = {
                OutlinedTextField(
                    value = newGoalInput,
                    onValueChange = { newGoalInput = it.filter { char -> char.isDigit() } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Tägliche Kalorien") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    newGoalInput.toIntOrNull()?.let {
                        if (it > 0) goalCalories = it
                    }
                    showGoalDialog = false
                    newGoalInput = ""
                }) {
                    Text("Ziel setzen")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showGoalDialog = false
                    newGoalInput = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tagesübersicht") },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Open drawer */ }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { showGoalDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Ziel setzen")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Calorie card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Tägliches Ziel: $goalCalories kcal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$usedCalories kcal verbraucht • $remainingCalories kcal übrig",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = progress.coerceIn(0f, 1f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.background
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Today's Meals Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Heutige Mahlzeiten",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (todaysMeals.isEmpty()) {
                        Text(
                            text = "Keine Mahlzeiten für heute",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        todaysMeals.forEach { meal ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = meal.name,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "${meal.cal.toInt()} kcal",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ActionButton(text = "📷  Scan", onClick = { onScanClick() })
                ActionButton(text = "🔵  Bluetooth", onClick = { onBluetoothClick() })
                ActionButton(text = "📅  Wochenübersicht", onClick = { onWeeklyClick() })
            }
        }
    }
}

@Composable
fun ActionButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Text(text = text, fontSize = 16.sp)
    }
}