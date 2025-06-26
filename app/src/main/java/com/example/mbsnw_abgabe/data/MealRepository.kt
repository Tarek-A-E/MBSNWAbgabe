package com.example.mbsnw_abgabe.data

import kotlinx.coroutines.flow.Flow

class MealRepository(private val dao: MealDao) {
    suspend fun addMeal(meal: Meal) { dao.insertMeal(meal) }
    suspend fun deleteMeal(meal: Meal) { dao.deleteMeal(meal) }
    fun getAllMeals(): Flow<List<Meal?>> { return dao.getAllMeals() }
    suspend fun deleteAllMeals() { dao.deleteAllMeals() }
}