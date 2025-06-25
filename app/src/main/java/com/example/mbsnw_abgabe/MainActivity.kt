package com.example.mbsnw_abgabe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import com.example.mbsnw_abgabe.data.MealDatabase
import com.example.mbsnw_abgabe.ui.theme.MBSNWAbgabeTheme
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

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
        database.close()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagesuebersichtScreen(
    onScanClick: () -> Unit,
    onBluetoothClick: () -> Unit,
    onWeeklyClick: () -> Unit
) {
    var usedCalories by remember { mutableStateOf(800) }
    val goalCalories = 2000
    val remainingCalories = goalCalories - usedCalories
    val progress = usedCalories / goalCalories.toFloat()

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
                    IconButton(onClick = { /* TODO: Open settings */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
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
                        text = "Kalorienverbrauch",
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

@Preview(showBackground = true)
@Composable
fun TagesuebersichtPreview() {
    MBSNWAbgabeTheme {
        TagesuebersichtScreen( onScanClick = {}, onBluetoothClick = {}, onWeeklyClick = {} )
    }
}