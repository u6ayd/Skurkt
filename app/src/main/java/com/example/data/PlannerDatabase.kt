package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [TaskEntity::class, SubtaskEntity::class, DayTemplateEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PlannerDatabase : RoomDatabase() {
    abstract fun plannerDao(): PlannerDao

    companion object {
        @Volatile
        private var INSTANCE: PlannerDatabase? = null

        fun getInstance(context: Context): PlannerDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PlannerDatabase::class.java,
                    "strukt_planner.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
