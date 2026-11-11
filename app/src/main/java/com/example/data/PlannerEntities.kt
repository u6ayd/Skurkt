package com.example.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

enum class TaskMode {
    TIMELESS_STEP,
    TIMED_BLOCK,
    TOP_BUBBLE
}

/**
 * Supports One-Time, Daily (e.g. Pray, Read), Weekdays, Weekly, and Monthly (e.g. 1st of month File Taxes).
 */
enum class RecurrenceRule(val label: String) {
    NONE("One-Time"),
    DAILY("Every Day"),
    WEEKDAYS("Weekdays"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly")
}

@Entity(
    tableName = "tasks",
    indices = [Index(value = ["dateEpochDay"]), Index(value = ["sortOrder"])]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String = "",
    val dateEpochDay: Long? = null,
    val taskMode: String = TaskMode.TIMELESS_STEP.name,
    val startTimeMinutes: Int? = null,
    val durationMinutes: Int = 30,
    val sortOrder: Int = 0,
    val colorKey: String = "CORAL",
    val iconKey: String = "BOOK",
    val folderName: String = "General",
    val sectionName: String = "Core",
    val recurrence: String = RecurrenceRule.NONE.name,
    /**
     * Day of month (1..31) when recurrence == MONTHLY (e.g. 1 for "1st of every month: File Taxes").
     */
    val recurrenceDayOfMonth: Int = 1,
    val isCompleted: Boolean = false,
    /**
     * Stores recorded Focus timer segments ("14:00-14:37||14:47-15:15") so pausing & resuming
     * shows a dotted "Continued" line on the timeline without creating a separate Break task.
     */
    val sessionSegments: String = "",
    /**
     * Non-null when a live Focus timer segment is currently running (stores start minutes from midnight).
     */
    val activeSessionStartMin: Int? = null
) {
    val modeEnum: TaskMode
        get() = runCatching { TaskMode.valueOf(taskMode) }.getOrDefault(TaskMode.TIMELESS_STEP)

    val recurrenceEnum: RecurrenceRule
        get() = runCatching { RecurrenceRule.valueOf(recurrence) }.getOrDefault(RecurrenceRule.NONE)

    val parsedSessionSegments: List<Pair<Int, Int>>
        get() {
            if (sessionSegments.isBlank()) return emptyList()
            return sessionSegments.split("||").mapNotNull { seg ->
                val parts = seg.split("-")
                if (parts.size == 2) {
                    val s = parts[0].toIntOrNull()
                    val e = parts[1].toIntOrNull()
                    if (s != null && e != null) s to e else null
                } else null
            }
        }
}

@Entity(
    tableName = "subtasks",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["taskId"])]
)
data class SubtaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val title: String,
    val stageTag: String = "STEP",
    val isCompleted: Boolean = false,
    val sortOrder: Int = 0
)

data class TaskWithSubtasks(
    @Embedded val task: TaskEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "taskId"
    )
    val subtasks: List<SubtaskEntity>
) {
    val sortedSubtasks: List<SubtaskEntity>
        get() = subtasks.sortedBy { it.sortOrder }
}

/**
 * Folder Entity with customizable Sections inside each Folder (replaces static Templates per user feedback).
 * Examples: Physics (Sections: Electrostatics, Optics, Modern Physics), Reading, Extracurriculars & Sport.
 */
@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val codeTag: String,
    val name: String,
    val colorKey: String,
    val iconKey: String,
    /**
     * Semicolon-separated list of sections inside this folder (e.g. "Electrostatics;;Optics;;Practice").
     */
    val sectionsCsv: String
) {
    val sections: List<String>
        get() = sectionsCsv.split(";;").map { it.trim() }.filter { it.isNotEmpty() }
}
