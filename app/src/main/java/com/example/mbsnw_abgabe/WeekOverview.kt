package com.example.mbsnw_abgabe

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mbsnw_abgabe.data.MealDatabase
import com.example.mbsnw_abgabe.data.MealRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekOverview(mealDB: MealDatabase) {
    val repository = remember { com.example.mbsnw_abgabe.data.MealRepository(mealDB.dao) }
    val scope = rememberCoroutineScope()

    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // State für aktuellen Wochenstart (Montag)
    var weekStart by remember {
        mutableStateOf(Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time)
    }
    val weekEnd = remember(weekStart) {
        Calendar.getInstance().apply {
            time = weekStart
            add(Calendar.DAY_OF_MONTH, 6)
        }.time
    }

    var caloriesPerDay by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }
    val durchschnitt = if (caloriesPerDay.isNotEmpty()) caloriesPerDay.values.sum() / caloriesPerDay.size else 0.0

    // Filtere Mahlzeiten für die aktuelle Woche
    LaunchedEffect(weekStart) {
        scope.launch {
            repository.getAllMeals().collect { meals ->
                val filtered = meals.filterNotNull().filter { meal ->
                    val mealDate = isoFormat.parse(meal.date)
                    mealDate != null && !mealDate.before(weekStart) && !mealDate.after(weekEnd)
                }
                val grouped = filtered.groupBy { it.date }
                caloriesPerDay = grouped.mapValues { it.value.sumOf { meal -> meal.cal } }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wochenübersicht") },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Open drawer */ }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        }
    ) { innerPadding ->

        Box (
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(innerPadding)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Wochen-Navigation
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            weekStart = Calendar.getInstance().apply {
                                time = weekStart
                                add(Calendar.DAY_OF_MONTH, -7)
                            }.time
                        }, modifier = Modifier.weight(1f)
                    ) { Text("<") }
                    Spacer(Modifier.width(16.dp))
                    Text("${dateFormat.format(weekStart)} - ${dateFormat.format(weekEnd)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(16.dp))
                    Button(
                        onClick = {
                            weekStart = Calendar.getInstance().apply {
                                time = weekStart
                                add(Calendar.DAY_OF_MONTH, 7)
                            }.time
                        }, modifier = Modifier.weight(1f)
                    ) { Text(">") }
                }

                Spacer(modifier = Modifier.height(16.dp))
                WeekBarChart(caloriesPerDay, scope, repository)

                Spacer(modifier = Modifier.height(16.dp))

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
                        if (durchschnitt == 0.0 || durchschnitt.isNaN()) {
                            Text(
                                text = "Keine Mahlzeiten eingetragen.",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "Tagesdurschnitt: %d kcal".format(durchschnitt.toInt()),
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