package com.example.mbsnw_abgabe

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.mbsnw_abgabe.data.MealDatabase
import java.text.SimpleDateFormat
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import java.util.*

@Composable
fun WeekBarChart(caloriesPerDay: Map<String, Double>) {
    // Wochentage bestimmen
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val calendar = Calendar.getInstance()
    calendar.firstDayOfWeek = Calendar.MONDAY
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    val daysOfWeek = (0..6).map {
        val date = format.format(calendar.time)
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        date
    }

    // Kalorien pro Tag berechnen
    val values = daysOfWeek.map { date -> caloriesPerDay[date] ?: 0.0 }
    val maxCal = (values.maxOrNull() ?: 1.0).coerceAtLeast(1.0)

    Row(
        Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        // Y-Achsen-Beschriftung
        Column(
            modifier = Modifier
                .width(40.dp)
                .fillMaxHeight()
                .padding(top = 8.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            listOf(4000, 3000, 2000, 1000, 0).forEach { value ->
                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }

        Box(
            modifier = Modifier
                .height(180.dp)
                .fillMaxWidth()
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .align(Alignment.Center)
            ) {
                val barWidth = size.width / 9
                val right = size.width - barWidth
                val bottom = size.height
                val top = 0f

                // Y-Achse
                drawLine(
                    color = Color.Gray,
                    start = Offset(barWidth, top),
                    end = Offset(barWidth, bottom),
                    strokeWidth = 2f
                )
                // X-Achse
                drawLine(
                    color = Color.Gray,
                    start = Offset(barWidth, bottom),
                    end = Offset(right, bottom),
                    strokeWidth = 2f
                )
                // Hilfslinien und Werte
                for (i in 1..4) {
                    val y = bottom - (i * (size.height / 4))
                    drawLine(
                        color = Color.LightGray,
                        start = Offset(barWidth, y),
                        end = Offset(right, y),
                        strokeWidth = 1f
                    )
                }
                // Balken zeichnen
                values.forEachIndexed { idx, cal ->
                    val barHeight =
                        ((cal / maxCal) * size.height).toFloat().coerceAtMost(size.height)
                    drawRect(
                        color = Color(0xFF90CAF9),
                        topLeft = Offset(
                            x = barWidth * (idx + 1),
                            y = size.height - barHeight
                        ),
                        size = androidx.compose.ui.geometry.Size(
                            width = barWidth,
                            height = barHeight
                        )
                    )
                }
            }
        }
    }

    // Optional: Wochentagsnamen darunter anzeigen
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(40.dp)) // Platz für Y-Achse
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So").forEach {
                Text(it, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

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
                val currentWeekMeals = meals.filterNotNull().groupBy { meal ->
                    isInCurrentWeak(meal.date)
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
            Spacer(modifier = Modifier.height(16.dp))
            // Hier den Graphen einbinden
            WeekBarChart(weekCaloriesMap)
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