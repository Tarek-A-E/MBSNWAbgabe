package com.example.mbsnw_abgabe.data

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.util.Locale

@Entity
data class Meal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val cal: Double,
    val date: String
)

class MealTimestampConverter {
    private val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Konvertiert Long (Millisekunden) zu Datum (nur das Datum ohne Uhrzeit)
    @TypeConverter
    fun fromTimestamp(value: Long): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = value
            // Setze die Uhrzeit auf Mitternacht (00:00:00)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return format.format(calendar.time)
    }

    // Konvertiert String (Datum im Format yyyy-MM-dd) zu Long (Millisekunden, Mitternacht)
    @TypeConverter
    fun toTimestamp(value: String): Long {
        val parsedDate = format.parse(value)
        val calendar = Calendar.getInstance()
        calendar.time = parsedDate
        // Setze die Uhrzeit auf Mitternacht
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}