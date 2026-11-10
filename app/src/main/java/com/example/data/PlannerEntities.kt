package com.example.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/**
 * Defines how a task behaves in the daily structure:
 * - TIMELESS_STEP: Part of the ordered vertical flow WITHOUT a clock time (solves schedule paralysis).
 * - TIMED_BLOCK: Part of the vertical flow WITH a start clock time and duration.
 * - TOP_BUBBLE: Circular "Anytime" routine bubble at the top of the day sheet (like Structured's "Structure Tomorrow", "Water Plants", "Call Mum").
 */
enum class TaskMode {
    TIMELESS_STEP,
    TIMED_BLOCK,
    TOP_BUBBLE
}

enum class RecurrenceRule(val label: String) {
    NONE("One-Time"),
    DAILY("Every Day"),
    WEEKDAYS("Weekdays"),
    WEEKENDS("Weekends")
}

@Entity(
    tableName = "tasks",
    indices = [Index(value = ["dateEpochDay"]), Index(value = ["sortOrder"])]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String = "",
    /**
     * Null when the task lives in the unscheduled Brain-Dump Inbox.
     * Otherwise stores LocalDate.toEpochDay().
     */
    val dateEpochDay: Long? = null,
    val taskMode: String = TaskMode.TIMELESS_STEP.name,
    /**
     * Optional minutes from midnight (0..1439) when taskMode == TIMED_BLOCK.
     */
    val startTimeMinutes: Int? = null,
    val durationMinutes: Int = 30,
    val sortOrder: Int = 0,
    val colorKey: String = "SKY",
    val iconKey: String = "BOOK",
    val subjectTag: String = "FLOW",
    val recurrence: String = RecurrenceRule.NONE.name,
    val isCompleted: Boolean = false
) {
    val modeEnum: TaskMode
        get() = runCatching { TaskMode.valueOf(taskMode) }.getOrDefault(TaskMode.TIMELESS_STEP)

    val recurrenceEnum: RecurrenceRule
        get() = runCatching { RecurrenceRule.valueOf(recurrence) }.getOrDefault(RecurrenceRule.NONE)
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
 * Reusable Day Blueprint / Folder Template (styled like Screenshot 2's PHY 12 / CHEM 12 folder cards).
 */
@Entity(tableName = "day_templates")
data class DayTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tabCode: String,
    val title: String,
    val subtitle: String,
    val tagLabel: String,
    val colorKey: String,
    /**
     * Pipe-delimited blueprint items:
     * title::mode::startTimeOrMinusOne::duration::colorKey::iconKey::subtask1,subtask2
     */
    val encodedItems: String
)

data class BlueprintTaskSpec(
    val title: String,
    val mode: TaskMode,
    val startTimeMinutes: Int?,
    val durationMinutes: Int,
    val colorKey: String,
    val iconKey: String,
    val subtasks: List<String>
)

fun DayTemplateEntity.decodeSpecs(): List<BlueprintTaskSpec> {
    if (encodedItems.isBlank()) return emptyList()
    return encodedItems.split("||").mapNotNull { row ->
        val parts = row.split("::")
        if (parts.size < 6) return@mapNotNull null
        val mode = runCatching { TaskMode.valueOf(parts[1]) }.getOrDefault(TaskMode.TIMELESS_STEP)
        val startMin = parts[2].toIntOrNull()?.takeIf { it >= 0 }
        val duration = parts[3].toIntOrNull() ?: 30
        val subs = if (parts.size >= 7 && parts[6].isNotBlank()) {
            parts[6].split(";;").filter { it.isNotBlank() }
        } else {
            emptyList()
        }
        BlueprintTaskSpec(
            title = parts[0],
            mode = mode,
            startTimeMinutes = startMin,
            durationMinutes = duration,
            colorKey = parts[4],
            iconKey = parts[5],
            subtasks = subs
        )
    }
}
