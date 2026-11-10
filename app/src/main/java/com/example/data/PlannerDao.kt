package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlannerDao {
    @Transaction
    @Query("SELECT * FROM tasks ORDER BY sortOrder ASC, COALESCE(startTimeMinutes, 9999) ASC, id ASC")
    fun observeAllTasksWithSubtasks(): Flow<List<TaskWithSubtasks>>

    @Query("SELECT * FROM day_templates ORDER BY id ASC")
    fun observeAllTemplates(): Flow<List<DayTemplateEntity>>

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun countTasks(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Long)

    @Query("DELETE FROM subtasks WHERE taskId = :taskId")
    suspend fun deleteSubtasksForTask(taskId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtasks(subtasks: List<SubtaskEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtask(subtask: SubtaskEntity): Long

    @Query("UPDATE subtasks SET isCompleted = :completed WHERE id = :subtaskId")
    suspend fun setSubtaskCompleted(subtaskId: Long, completed: Boolean)

    @Query("DELETE FROM subtasks WHERE id = :subtaskId")
    suspend fun deleteSubtaskById(subtaskId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplates(templates: List<DayTemplateEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: DayTemplateEntity): Long

    @Query("UPDATE tasks SET isCompleted = 0 WHERE dateEpochDay = :epochDay")
    suspend fun resetDayCompletion(epochDay: Long)

    @Query("SELECT MAX(sortOrder) FROM tasks WHERE dateEpochDay = :epochDay")
    suspend fun getMaxSortOrderForDay(epochDay: Long): Int?
}
