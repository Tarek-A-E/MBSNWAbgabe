package com.example.mbsnw_abgabe

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import kotlinx.coroutines.launch
import com.example.mbsnw_abgabe.data.MealDatabase
import java.text.SimpleDateFormat
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.mbsnw_abgabe.data.MealRepository
import kotlinx.coroutines.CoroutineScope
import java.util.*


@Composable
fun WeekBarChart(
    caloriesPerDay: Map<String, Double>,
    scope: CoroutineScope,
    repository: MealRepository
) {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val calendar = Calendar.getInstance()
    calendar.firstDayOfWeek = Calendar.MONDAY
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    val daysOfWeek = (0..6).map {
        val date = format.format(calendar.time)
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        date
    }

    val values = daysOfWeek.map { date -> caloriesPerDay[date] ?: 0.0 }
    val maxCal = 4000.0 // Feste Skalierung für die Hilfslinien

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
        )
        {
            val barColor = MaterialTheme.colorScheme.primary

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .align(Alignment.Center)
            ) {
                val barCount = 8
                val barWidth = size.width / barCount
                val bottom = size.height
                val maxCal = 4000f

                // Y-Achse
                drawLine(
                    color = Color.Gray,
                    start = Offset(barWidth, 0f),
                    end = Offset(barWidth, bottom),
                    strokeWidth = 4f
                )
                // X-Achse
                drawLine(
                    color = Color.Gray,
                    start = Offset(barWidth, bottom - 1),
                    end = Offset(size.width, bottom - 1),
                    strokeWidth = 4f
                )

                // Hilfslinien bei 3000, 2000, 1000
                listOf(4000f, 3000f, 2000f, 1000f).forEach { value ->
                    val y = bottom - (value / maxCal) * size.height
                    drawLine(
                        color = Color.LightGray,
                        start = Offset(barWidth, y),
                        end = Offset(size.width, y),
                        strokeWidth = 3f
                    )
                }

                values.forEachIndexed { index, value ->
                    if (index < 7) {
                        val barHeight =
                            ((value / maxCal) * size.height).toFloat().coerceAtLeast(2.dp.toPx())
                        drawRect(
                            color = barColor,
                            topLeft = Offset(
                                (index + 1) * barWidth + barWidth * 0.15f,
                                bottom - barHeight
                            ),
                            size = androidx.compose.ui.geometry.Size(barWidth * 0.7f, barHeight)
                        )
                    }
                }
            }
        }
    }

    val yAxisWidth = 40.dp
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val barLabels = listOf(" ", "Mo", "Di", "Mi", "Do", "Fr", "Sa", "So")
    val chartWidth = screenWidth - yAxisWidth

    Row(
        Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(yAxisWidth)) // Platz für Y-Achse
        Row(
            modifier = Modifier
                .width(chartWidth)
                .height(24.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            barLabels.forEach {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(it, style = MaterialTheme.typography.labelSmall)
                }
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
                    isInCurrentWeek(meal.date)
                }
                weekCaloriesMap = grouped.mapValues { entry ->
                    entry.value.sumOf { it.cal }
                }.toSortedMap(compareByDescending { it })
            }
        }
    }

    var caloriesPerDay by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }
    val durchschnitt = if (caloriesPerDay.isNotEmpty()) caloriesPerDay.values.sum() / caloriesPerDay.size else 0.0

    LaunchedEffect(Unit) {
        scope.launch {
            repository.getAllMeals().collect { meals ->
                val grouped = meals.filterNotNull().groupBy { meal ->
                    meal.date // oder ggf. meal.date.substring(0, 10) für yyyy-MM-dd
                }
                val calMap = grouped.mapValues { entry ->
                    entry.value.sumOf { it.cal }
                }
                caloriesPerDay = calMap
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
                        text = "Wochenübersicht",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            // Hier den Graphen einbinden
            WeekBarChart(weekCaloriesMap, scope, repository)

            Spacer(modifier = Modifier.height(16.dp))

            Card (
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
                    if (durchschnitt == 0.0 || weekCaloriesMap.isEmpty() || durchschnitt.isNaN()) {
                        Text(
                            text = "Keine Mahlzeiten eingetragen.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        // Durchschnitt pro Tag
                        Text(
                            text = "Kalorienverbrauch pro Woche: %1f kcal".format(durchschnitt),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    scope.launch {
                        repository.deleteAllMeals()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text("Datenbank kys")
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