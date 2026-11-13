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

    @Query("SELECT * FROM folders ORDER BY id ASC")
    fun observeAllFolders(): Flow<List<FolderEntity>>

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun countTasks(): Int

    @Query("SELECT COUNT(*) FROM folders")
    suspend fun countFolders(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Long)

    @Query("DELETE FROM tasks WHERE id IN (:taskIds)")
    suspend fun deleteTasksByIds(taskIds: List<Long>)

    @Query("DELETE FROM subtasks WHERE taskId = :taskId")
    suspend fun deleteSubtasksForTask(taskId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtasks(subtasks: List<SubtaskEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtask(subtask: SubtaskEntity): Long

    @Query("UPDATE subtasks SET isCompleted = :completed WHERE id = :subtaskId")
    suspend fun setSubtaskCompleted(subtaskId: Long, completed: Boolean)

    @Query("SELECT * FROM subtasks WHERE taskId = :taskId ORDER BY sortOrder ASC, id ASC")
    suspend fun getSubtasksForTask(taskId: Long): List<SubtaskEntity>

    @Query("SELECT MAX(sortOrder) FROM subtasks WHERE taskId = :taskId")
    suspend fun getMaxSubtaskSortOrder(taskId: Long): Int?

    @Query("DELETE FROM subtasks WHERE id = :subtaskId")
    suspend fun deleteSubtaskById(subtaskId: Long)

    @Query("DELETE FROM subtasks WHERE id IN (:subtaskIds)")
    suspend fun deleteSubtasksByIds(subtaskIds: List<Long>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolders(folders: List<FolderEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity): Long

    @Update
    suspend fun updateFolder(folder: FolderEntity)

    @Query("SELECT MAX(sortOrder) FROM tasks WHERE dateEpochDay = :epochDay")
    suspend fun getMaxSortOrderForDay(epochDay: Long): Int?
}
