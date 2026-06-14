package com.example.repository

import com.example.data.local.AppDatabase
import com.example.data.local.PlannerTask
import kotlinx.coroutines.flow.Flow

class PlannerRepository(private val db: AppDatabase) {

    val allTasks: Flow<List<PlannerTask>> = db.plannerTaskDao().getAllTasks()

    fun getTasksByType(type: String): Flow<List<PlannerTask>> =
        db.plannerTaskDao().getTasksByType(type)

    suspend fun insertTask(task: PlannerTask): Long {
        return db.plannerTaskDao().insertTask(task)
    }

    suspend fun updateTask(task: PlannerTask) {
        db.plannerTaskDao().updateTask(task)
    }

    suspend fun deleteTask(task: PlannerTask) {
        db.plannerTaskDao().deleteTask(task)
    }
}
