package com.example.mbsnw_abgabe.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Insert
    suspend fun insertMeal(meal: Meal)

    @Delete
    suspend fun deleteMeal(meal: Meal)

    @Query("DELETE FROM Meal")
    suspend fun deleteAllMeals()

    @Query("SELECT * FROM Meal")
    fun getAllMeals(): Flow<List<Meal?>>
}