package com.example.data

import androidx.compose.runtime.Immutable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

enum class TaskMode {
    TIMELESS_STEP,
    TIMED_BLOCK,
    TOP_BUBBLE
}

enum class RecurrenceRule(val label: String) {
    NONE("One-Time"),
    DAILY("Every Day"),
    WEEKDAYS("Weekdays"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly")
}

@Immutable
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
    val recurrenceDayOfMonth: Int = 1,
    val isCompleted: Boolean = false,
    val sessionSegments: String = "",
    val activeSessionStartMin: Int? = null
) {
    @Ignore
    val modeEnum: TaskMode =
        runCatching { TaskMode.valueOf(taskMode) }.getOrDefault(TaskMode.TIMELESS_STEP)

    @Ignore
    val recurrenceEnum: RecurrenceRule =
        runCatching { RecurrenceRule.valueOf(recurrence) }.getOrDefault(RecurrenceRule.NONE)

    @Ignore
    val parsedSessionSegments: List<Pair<Int, Int>> =
        if (sessionSegments.isBlank()) {
            emptyList()
        } else {
            sessionSegments.split("||").mapNotNull { seg ->
                val dashIdx = seg.indexOf('-')
                if (dashIdx > 0 && dashIdx < seg.length - 1) {
                    val s = seg.substring(0, dashIdx).toIntOrNull()
                    val e = seg.substring(dashIdx + 1).toIntOrNull()
                    if (s != null && e != null) s to e else null
                } else {
                    null
                }
            }
        }
}

@Immutable
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

@Immutable
data class TaskWithSubtasks(
    @Embedded val task: TaskEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "taskId"
    )
    val subtasks: List<SubtaskEntity>
) {
    @Ignore
    val sortedSubtasks: List<SubtaskEntity> =
        if (subtasks.size <= 1) subtasks else subtasks.sortedBy { it.sortOrder }

    @Ignore
    val completedSubtasksCount: Int = subtasks.count { it.isCompleted }
}

@Immutable
@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val codeTag: String,
    val name: String,
    val colorKey: String,
    val iconKey: String,
    val sectionsCsv: String
) {
    @Ignore
    val sections: List<String> =
        if (sectionsCsv.isBlank()) {
            emptyList()
        } else {
            sectionsCsv.split(";;").mapNotNull { raw ->
                val trimmed = raw.trim()
                trimmed.ifEmpty { null }
            }
        }
}
