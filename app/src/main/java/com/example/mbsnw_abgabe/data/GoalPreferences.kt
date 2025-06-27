package com.example.mbsnw_abgabe.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore("settings")

object GoalPreferences {
    private val GOAL_KEY = intPreferencesKey("goal_calories")

    fun getGoal(context: Context): Flow<Int> =
        context.dataStore.data.map { it[GOAL_KEY] ?: 2000 }

    suspend fun setGoal(context: Context, value: Int) {
        context.dataStore.edit { it[GOAL_KEY] = value }
    }
}