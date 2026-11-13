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
    MONTHLY("Monthly"),
    CUSTOM_DAYS("Every N Days")
}

fun formatOrdinalDayOfMonth(day: Int): String {
    val d = day.coerceIn(1, 31)
    val suffix = when {
        d in 11..13 -> "th"
        d % 10 == 1 -> "st"
        d % 10 == 2 -> "nd"
        d % 10 == 3 -> "rd"
        else -> "th"
    }
    return "${d}$suffix of month"
}

fun normalizeTaskSortOrder(rawOrder: Int): Int {
    val normalized = if (rawOrder >= 10 && rawOrder % 10 == 0) rawOrder / 10 else rawOrder
    return normalized.coerceIn(1, 99)
}

fun nextDaySortOrder(currentMaxOrder: Int?): Int {
    val maxOrder = currentMaxOrder ?: 0
    if (maxOrder <= 0) return 1
    return (normalizeTaskSortOrder(maxOrder) + 1).coerceIn(1, 99)
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
    val durationMinutes: Int = 0,
    val sortOrder: Int = 0,
    val colorKey: String = "CORAL",
    val iconKey: String = "BOOK",
    val folderName: String = "General",
    val sectionName: String = "Core",
    val recurrence: String = RecurrenceRule.NONE.name,
    val recurrenceDayOfMonth: Int = 1,
    val recurrenceIntervalDays: Int = 2,
    val excludedEpochDaysCsv: String = "",
    val completedEpochDaysCsv: String = "",
    val allowSimultaneous: Boolean = false,
    val isCompleted: Boolean = false,
    val sessionSegments: String = "",
    val sessionSegmentsByDayCsv: String = "",
    val activeSessionStartMin: Int? = null,
    val activeSessionEpochDay: Long? = null
) {
    @Ignore
    val modeEnum: TaskMode =
        runCatching { TaskMode.valueOf(taskMode) }.getOrDefault(TaskMode.TIMELESS_STEP)

    @Ignore
    val recurrenceEnum: RecurrenceRule =
        runCatching { RecurrenceRule.valueOf(recurrence) }.getOrDefault(RecurrenceRule.NONE)

    @Ignore
    val effectiveStepNumber: Int = normalizeTaskSortOrder(sortOrder)

    @Ignore
    val excludedEpochDays: Set<Long> =
        if (excludedEpochDaysCsv.isBlank()) {
            emptySet()
        } else {
            excludedEpochDaysCsv.split(",").mapNotNull { it.trim().toLongOrNull() }.toSet()
        }

    @Ignore
    val completedEpochDays: Set<Long> =
        if (completedEpochDaysCsv.isBlank()) {
            emptySet()
        } else {
            completedEpochDaysCsv.split(",").mapNotNull { it.trim().toLongOrNull() }.toSet()
        }

    @Ignore
    val sessionSegmentsByDay: Map<Long, String> =
        if (sessionSegmentsByDayCsv.isBlank()) {
            emptyMap()
        } else {
            buildMap {
                for (entry in sessionSegmentsByDayCsv.split(";;")) {
                    val colonIdx = entry.indexOf(':')
                    if (colonIdx > 0 && colonIdx < entry.length - 1) {
                        val day = entry.substring(0, colonIdx).trim().toLongOrNull()
                        val segs = entry.substring(colonIdx + 1).trim()
                        if (day != null && segs.isNotEmpty()) {
                            put(day, segs)
                        }
                    }
                }
            }
        }

    @Ignore
    val parsedSessionSegments: List<Pair<Int, Int>> = parseSegmentsString(sessionSegments)

    @Ignore
    val hasBreakLoop: Boolean =
        parsedSessionSegments.size > 1 || (parsedSessionSegments.size == 1 && activeSessionStartMin != null)

    @Ignore
    val totalWorkedMinutes: Int =
        parsedSessionSegments.sumOf { (s, e) -> (e - s).coerceAtLeast(1) }

    @Ignore
    val totalBreakMinutes: Int =
        if (parsedSessionSegments.size < 2) {
            0
        } else {
            parsedSessionSegments.zipWithNext().sumOf { (prev, next) ->
                (next.first - prev.second).coerceAtLeast(1)
            }
        }

    fun isCompletedOnDay(epochDay: Long): Boolean {
        return if (recurrenceEnum == RecurrenceRule.NONE) {
            isCompleted
        } else {
            epochDay in completedEpochDays
        }
    }

    fun sessionSegmentsForDay(epochDay: Long): String {
        return if (recurrenceEnum == RecurrenceRule.NONE) {
            sessionSegments
        } else {
            sessionSegmentsByDay[epochDay]
                ?: if (dateEpochDay == epochDay && sessionSegmentsByDay.isEmpty()) sessionSegments else ""
        }
    }

    fun parsedSessionSegmentsForDay(epochDay: Long): List<Pair<Int, Int>> =
        parseSegmentsString(sessionSegmentsForDay(epochDay))

    fun activeSessionStartMinForDay(epochDay: Long): Int? {
        return if (recurrenceEnum == RecurrenceRule.NONE) {
            activeSessionStartMin
        } else {
            if (activeSessionEpochDay == null || activeSessionEpochDay == epochDay) {
                activeSessionStartMin
            } else {
                null
            }
        }
    }

    fun hasBreakLoopOnDay(epochDay: Long): Boolean {
        val segs = parsedSessionSegmentsForDay(epochDay)
        val active = activeSessionStartMinForDay(epochDay)
        return segs.size > 1 || (segs.size == 1 && active != null)
    }

    fun withUpdatedDayCompletion(epochDay: Long, completed: Boolean): TaskEntity {
        return if (recurrenceEnum == RecurrenceRule.NONE) {
            copy(isCompleted = completed)
        } else {
            val updatedSet = if (completed) {
                completedEpochDays + epochDay
            } else {
                completedEpochDays - epochDay
            }
            val updatedCsv = updatedSet.sorted().joinToString(",")
            copy(
                completedEpochDaysCsv = updatedCsv,
                isCompleted = completed
            )
        }
    }

    fun withUpdatedDaySession(
        epochDay: Long,
        newSegments: String,
        newActiveStartMin: Int?
    ): TaskEntity {
        val nextActiveDay = if (newActiveStartMin != null) epochDay else null
        return if (recurrenceEnum == RecurrenceRule.NONE) {
            copy(
                sessionSegments = newSegments,
                activeSessionStartMin = newActiveStartMin,
                activeSessionEpochDay = nextActiveDay
            )
        } else {
            val updatedMap = sessionSegmentsByDay.toMutableMap()
            if (newSegments.isBlank()) {
                updatedMap.remove(epochDay)
            } else {
                updatedMap[epochDay] = newSegments
            }
            val updatedByDayCsv = updatedMap.entries
                .sortedBy { it.key }
                .joinToString(";;") { "${it.key}:${it.value}" }
            copy(
                sessionSegments = newSegments,
                sessionSegmentsByDayCsv = updatedByDayCsv,
                activeSessionStartMin = newActiveStartMin,
                activeSessionEpochDay = nextActiveDay
            )
        }
    }

    fun forEpochDay(epochDay: Long): TaskEntity {
        if (recurrenceEnum == RecurrenceRule.NONE) {
            return if (activeSessionStartMin != null && activeSessionEpochDay != null && activeSessionEpochDay != epochDay) {
                copy(activeSessionStartMin = null)
            } else {
                this
            }
        }
        val dayDone = isCompletedOnDay(epochDay)
        val daySegs = sessionSegmentsForDay(epochDay)
        val dayActiveStart = activeSessionStartMinForDay(epochDay)
        if (isCompleted == dayDone && sessionSegments == daySegs && activeSessionStartMin == dayActiveStart) {
            return this
        }
        return copy(
            isCompleted = dayDone,
            sessionSegments = daySegs,
            activeSessionStartMin = dayActiveStart
        )
    }

    fun recurrenceSummaryLabel(): String = when (recurrenceEnum) {
        RecurrenceRule.NONE -> RecurrenceRule.NONE.label
        RecurrenceRule.DAILY -> RecurrenceRule.DAILY.label
        RecurrenceRule.WEEKDAYS -> RecurrenceRule.WEEKDAYS.label
        RecurrenceRule.WEEKLY -> RecurrenceRule.WEEKLY.label
        RecurrenceRule.MONTHLY -> "Monthly (${formatOrdinalDayOfMonth(recurrenceDayOfMonth)})"
        RecurrenceRule.CUSTOM_DAYS -> "Every ${recurrenceIntervalDays.coerceAtLeast(1)} days"
    }

    companion object {
        fun parseSegmentsString(rawSegments: String): List<Pair<Int, Int>> {
            if (rawSegments.isBlank()) return emptyList()
            return rawSegments.split("||").mapNotNull { seg ->
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

    fun forEpochDay(epochDay: Long): TaskWithSubtasks {
        val projected = task.forEpochDay(epochDay)
        return if (projected === task) this else copy(task = projected)
    }
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
