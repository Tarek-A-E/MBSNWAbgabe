package com.example.mbsnw_abgabe

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.mbsnw_abgabe.data.Meal
import com.example.mbsnw_abgabe.data.MealDatabase
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WeekOverview(mealDB: MealDatabase) {
    val repository = remember { com.example.mbsnw_abgabe.data.MealRepository(mealDB.dao) }
    val scope = rememberCoroutineScope()
    var weekCaloriesMap by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }


    LaunchedEffect(Unit) {
        scope.launch {
            repository.getAllMeals().collect { meals ->
                val grouped = meals.filterNotNull().groupBy { meal ->
                    getYearWeekString(meal.date)
                }
                weekCaloriesMap = grouped.mapValues { entry ->
                    entry.value.sumOf { it.cal }
                }.toSortedMap(compareByDescending { it })
            }
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                        text = "Kalorienverbrauch pro Woche",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


fun isInCurrentWeek(dateString: String): Boolean {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val mealDate = format.parse(dateString) ?: return false

    val calendar = Calendar.getInstance()
    val weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR)
    val year = calendar.get(Calendar.YEAR)

    val mealCal = Calendar.getInstance().apply { time = mealDate }
    val mealWeek = mealCal.get(Calendar.WEEK_OF_YEAR)
    val mealYear = mealCal.get(Calendar.YEAR)

    return weekOfYear == mealWeek && year == mealYear
}

fun getYearWeekString(dateString: String): String {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val date = format.parse(dateString) ?: return "Unbekannt"
    val cal = Calendar.getInstance().apply { time = date }
    val year = cal.get(Calendar.YEAR)
    val week = cal.get(Calendar.WEEK_OF_YEAR)
    return "$year-W$week"
}