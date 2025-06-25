package com.example.mbsnw_abgabe.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Meal::class],
    version = 1
)
@TypeConverters(MealTimestampConverter::class)
abstract class MealDatabase: RoomDatabase() {
    abstract val dao: MealDao
}