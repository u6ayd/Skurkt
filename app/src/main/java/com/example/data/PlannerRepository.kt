package com.example.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime

class PlannerRepository(private val dao: PlannerDao) {

    val allTasksFlow: Flow<List<TaskWithSubtasks>> = dao.observeAllTasksWithSubtasks()
    val allFoldersFlow: Flow<List<FolderEntity>> = dao.observeAllFolders()

    suspend fun ensureSeedData(todayEpochDay: Long = LocalDate.now().toEpochDay()) {
        if (dao.countFolders() == 0) {
            dao.insertFolders(
                listOf(
                    FolderEntity(
                        codeTag = "PHY 12",
                        name = "Physics",
                        colorKey = "SKY",
                        iconKey = "SCIENCE",
                        sectionsCsv = "Electrostatics;;Current Electricity;;Optics;;Mock Papers"
                    ),
                    FolderEntity(
                        codeTag = "CHEM 12",
                        name = "Chemistry",
                        colorKey = "ORANGE",
                        iconKey = "SCIENCE",
                        sectionsCsv = "Physical Chemistry;;Organic;;Inorganic"
                    ),
                    FolderEntity(
                        codeTag = "MATH 12",
                        name = "Mathematics",
                        colorKey = "YELLOW",
                        iconKey = "CODE",
                        sectionsCsv = "Calculus;;Vectors & 3D;;Algebra"
                    ),
                    FolderEntity(
                        codeTag = "READ",
                        name = "Reading & Daily",
                        colorKey = "SAGE",
                        iconKey = "BOOK",
                        sectionsCsv = "Books;;Daily Habits;;Reflection"
                    ),
                    FolderEntity(
                        codeTag = "SPORT",
                        name = "Sports & Life",
                        colorKey = "MINT",
                        iconKey = "FITNESS",
                        sectionsCsv = "Workouts;;Extracurriculars;;Admin & Taxes"
                    )
                )
            )
        }

        if (dao.countTasks() > 0) return

        // 1. Top Routine Bubbles (Matches Structured Screenshots 5 & 6: "Call Mum", "Structure Tomorrow", "Water Plants")
        dao.insertTask(
            TaskEntity(
                title = "Structure Tomorrow",
                dateEpochDay = todayEpochDay,
                taskMode = TaskMode.TOP_BUBBLE.name,
                durationMinutes = 10,
                sortOrder = 1,
                colorKey = "CORAL",
                iconKey = "CHECKLIST",
                folderName = "Reading & Daily",
                sectionName = "Daily Habits",
                recurrence = RecurrenceRule.DAILY.name
            )
        )
        dao.insertTask(
            TaskEntity(
                title = "Water Plants",
                dateEpochDay = todayEpochDay,
                taskMode = TaskMode.TOP_BUBBLE.name,
                durationMinutes = 10,
                sortOrder = 2,
                colorKey = "SAGE",
                iconKey = "PLANT",
                folderName = "Sports & Life",
                sectionName = "Admin & Taxes",
                recurrence = RecurrenceRule.DAILY.name
            )
        )
        dao.insertTask(
            TaskEntity(
                title = "Call Mum",
                dateEpochDay = todayEpochDay,
                taskMode = TaskMode.TOP_BUBBLE.name,
                durationMinutes = 15,
                sortOrder = 3,
                colorKey = "SKY",
                iconKey = "CALL",
                folderName = "Sports & Life",
                sectionName = "Extracurriculars",
                recurrence = RecurrenceRule.NONE.name,
                isCompleted = true
            )
        )

        // 2. Daily Timeline Spine (Mixing Timed & Timeless + Pause/Resume Continued Example)
        dao.insertTask(
            TaskEntity(
                title = "Rise and Shine",
                dateEpochDay = todayEpochDay,
                taskMode = TaskMode.TIMED_BLOCK.name,
                startTimeMinutes = 8 * 60,
                durationMinutes = 15,
                sortOrder = 1,
                colorKey = "CORAL",
                iconKey = "ALARM",
                folderName = "Reading & Daily",
                sectionName = "Daily Habits",
                recurrence = RecurrenceRule.DAILY.name,
                isCompleted = true
            )
        )

        val yogaId = dao.insertTask(
            TaskEntity(
                title = "Yoga Workout",
                dateEpochDay = todayEpochDay,
                taskMode = TaskMode.TIMED_BLOCK.name,
                startTimeMinutes = 8 * 60 + 15,
                durationMinutes = 30,
                sortOrder = 2,
                colorKey = "SKY",
                iconKey = "WORKOUT",
                folderName = "Sports & Life",
                sectionName = "Workouts",
                recurrence = RecurrenceRule.DAILY.name,
                isCompleted = true
            )
        )
        dao.insertSubtasks(
            listOf(
                SubtaskEntity(taskId = yogaId, title = "Warm-up stretch", stageTag = "STEP 1", isCompleted = true, sortOrder = 1),
                SubtaskEntity(taskId = yogaId, title = "Core mobility", stageTag = "STEP 2", isCompleted = true, sortOrder = 2)
            )
        )

        // Timeless task slotted into the unscheduled gap between 08:45 and 14:00
        dao.insertTask(
            TaskEntity(
                title = "Read a Book",
                notes = "20 pages daily reading",
                dateEpochDay = todayEpochDay,
                taskMode = TaskMode.TIMELESS_STEP.name,
                startTimeMinutes = null,
                durationMinutes = 45,
                sortOrder = 3,
                colorKey = "SAGE",
                iconKey = "BOOK",
                folderName = "Reading & Daily",
                sectionName = "Books",
                recurrence = RecurrenceRule.DAILY.name,
                isCompleted = false
            )
        )

        // Physics task with a recorded pause/resume session ("14:00-14:37" -> Continued "14:47-15:15")
        val physicsId = dao.insertTask(
            TaskEntity(
                title = "Physics: Electric Charges & Fields",
                notes = "Gauss Law derivations and practice problems",
                dateEpochDay = todayEpochDay,
                taskMode = TaskMode.TIMED_BLOCK.name,
                startTimeMinutes = 14 * 60,
                durationMinutes = 60,
                sortOrder = 4,
                colorKey = "SKY",
                iconKey = "SCIENCE",
                folderName = "Physics",
                sectionName = "Electrostatics",
                recurrence = RecurrenceRule.NONE.name,
                isCompleted = false,
                sessionSegments = "${14 * 60}-${14 * 60 + 37}||${14 * 60 + 47}-${15 * 60 + 15}"
            )
        )
        dao.insertSubtasks(
            listOf(
                SubtaskEntity(taskId = physicsId, title = "Lecture Watched", stageTag = "LECTURE", isCompleted = true, sortOrder = 1),
                SubtaskEntity(taskId = physicsId, title = "Anki Flashcards", stageTag = "ANKI", isCompleted = false, sortOrder = 2),
                SubtaskEntity(taskId = physicsId, title = "25 Chapter MCQs", stageTag = "MCQS", isCompleted = false, sortOrder = 3)
            )
        )

        // Monthly recurring task on the 1st of every month ("File Taxes")
        val firstOfCurrentMonth = LocalDate.ofEpochDay(todayEpochDay).withDayOfMonth(1).toEpochDay()
        dao.insertTask(
            TaskEntity(
                title = "File Taxes & Monthly Review",
                notes = "Automatically repeats on the 1st of every month",
                dateEpochDay = firstOfCurrentMonth,
                taskMode = TaskMode.TIMELESS_STEP.name,
                startTimeMinutes = null,
                durationMinutes = 45,
                sortOrder = 5,
                colorKey = "YELLOW",
                iconKey = "TAX",
                folderName = "Sports & Life",
                sectionName = "Admin & Taxes",
                recurrence = RecurrenceRule.MONTHLY.name,
                recurrenceDayOfMonth = 1,
                isCompleted = false
            )
        )

        // 3. Brain-Dump Inbox Tasks (Matches Screenshot 5 left sidebar)
        dao.insertTask(
            TaskEntity(
                title = "Do Laundry",
                dateEpochDay = null,
                taskMode = TaskMode.TIMELESS_STEP.name,
                durationMinutes = 60,
                sortOrder = 1,
                colorKey = "SKY",
                iconKey = "LAUNDRY",
                folderName = "Sports & Life",
                sectionName = "Admin & Taxes"
            )
        )
        dao.insertTask(
            TaskEntity(
                title = "Chemistry: Electrochemistry",
                dateEpochDay = null,
                taskMode = TaskMode.TIMELESS_STEP.name,
                durationMinutes = 60,
                sortOrder = 2,
                colorKey = "ORANGE",
                iconKey = "SCIENCE",
                folderName = "Chemistry",
                sectionName = "Physical Chemistry"
            )
        )
        dao.insertTask(
            TaskEntity(
                title = "Call Grandparents",
                dateEpochDay = null,
                taskMode = TaskMode.TIMELESS_STEP.name,
                durationMinutes = 30,
                sortOrder = 3,
                colorKey = "SKY",
                iconKey = "CALL",
                folderName = "Sports & Life",
                sectionName = "Extracurriculars"
            )
        )
        dao.insertTask(
            TaskEntity(
                title = "Go for a Run",
                dateEpochDay = null,
                taskMode = TaskMode.TIMELESS_STEP.name,
                durationMinutes = 60,
                sortOrder = 4,
                colorKey = "CORAL",
                iconKey = "FITNESS",
                folderName = "Sports & Life",
                sectionName = "Workouts"
            )
        )
    }

    suspend fun saveTaskWithSubtasks(
        task: TaskEntity,
        subtaskTitles: List<Pair<String, String>>
    ): Long {
        val nextOrder = if (task.id == 0L && task.dateEpochDay != null && task.sortOrder <= 0) {
            val currentMax = dao.getMaxSortOrderForDay(task.dateEpochDay) ?: 0
            val normalizedMax = if (currentMax >= 10 && currentMax % 10 == 0) currentMax / 10 else currentMax
            (normalizedMax + 1).coerceIn(1, 99)
        } else {
            val raw = task.sortOrder
            val normalized = if (raw >= 20 && raw % 10 == 0) raw / 10 else raw
            normalized.coerceIn(1, 99)
        }
        val taskId = dao.insertTask(task.copy(sortOrder = nextOrder))
        dao.deleteSubtasksForTask(taskId)
        val entities = subtaskTitles
            .filter { it.first.isNotBlank() }
            .mapIndexed { idx, (title, stage) ->
                SubtaskEntity(
                    taskId = taskId,
                    title = title.trim(),
                    stageTag = stage.ifBlank { "STEP ${idx + 1}" },
                    isCompleted = false,
                    sortOrder = idx + 1
                )
            }
        if (entities.isNotEmpty()) {
            dao.insertSubtasks(entities)
        }
        return taskId
    }

    suspend fun updateTaskStepOrder(task: TaskEntity, newStepOrder: Int) {
        dao.updateTask(task.copy(sortOrder = newStepOrder.coerceIn(1, 99)))
    }

    suspend fun reorderDaySpineTask(
        daySpineTasks: List<TaskEntity>,
        targetTask: TaskEntity,
        newStepNumber: Int
    ) {
        if (daySpineTasks.isEmpty()) {
            updateTaskStepOrder(targetTask, newStepNumber)
            return
        }
        val mutable = daySpineTasks.toMutableList()
        val currentIdx = mutable.indexOfFirst { it.id == targetTask.id }
        val targetIdx = (newStepNumber - 1).coerceIn(0, mutable.lastIndex.coerceAtLeast(0))
        if (currentIdx >= 0) {
            val removed = mutable.removeAt(currentIdx)
            mutable.add(targetIdx, removed)
        } else {
            mutable.add(targetIdx.coerceAtMost(mutable.size), targetTask)
        }
        mutable.forEachIndexed { index, item ->
            val desiredOrder = index + 1
            if (item.id == targetTask.id || item.sortOrder != desiredOrder) {
                dao.updateTask(item.copy(sortOrder = desiredOrder))
            }
        }
    }

    suspend fun markTaskSimultaneous(task: TaskEntity) {
        if (!task.allowSimultaneous) {
            dao.updateTask(task.copy(allowSimultaneous = true))
        }
    }

    suspend fun excludeRecurringTaskOnDay(task: TaskEntity, epochDay: Long) {
        val updatedSet = (task.excludedEpochDays + epochDay).sorted()
        val updatedCsv = updatedSet.joinToString(",")
        dao.updateTask(task.copy(excludedEpochDaysCsv = updatedCsv))
    }

    suspend fun toggleTaskCompleted(task: TaskEntity) {
        dao.updateTask(task.copy(isCompleted = !task.isCompleted))
    }

    suspend fun toggleSubtaskCompleted(subtask: SubtaskEntity) {
        dao.setSubtaskCompleted(subtask.id, !subtask.isCompleted)
    }

    suspend fun addQuickSubtask(taskId: Long, title: String, stageTag: String = "STEP") {
        if (title.isBlank()) return
        dao.insertSubtask(
            SubtaskEntity(
                taskId = taskId,
                title = title.trim(),
                stageTag = stageTag,
                isCompleted = false,
                sortOrder = 99
            )
        )
    }

    suspend fun scheduleInboxTaskToDay(
        task: TaskEntity,
        targetEpochDay: Long,
        asTimed: Boolean = false,
        startTimeMinutes: Int? = null,
        durationMinutes: Int = task.durationMinutes,
        allowSimultaneous: Boolean = false
    ) {
        val currentMax = dao.getMaxSortOrderForDay(targetEpochDay) ?: 0
        val normalizedMax = if (currentMax >= 10 && currentMax % 10 == 0) currentMax / 10 else currentMax
        val nextOrder = (normalizedMax + 1).coerceIn(1, 99)
        dao.updateTask(
            task.copy(
                dateEpochDay = targetEpochDay,
                taskMode = if (asTimed && startTimeMinutes != null) {
                    TaskMode.TIMED_BLOCK.name
                } else {
                    TaskMode.TIMELESS_STEP.name
                },
                startTimeMinutes = if (asTimed) startTimeMinutes else null,
                durationMinutes = if (asTimed) durationMinutes.coerceIn(5, 720) else durationMinutes.coerceIn(0, 720),
                sortOrder = nextOrder,
                allowSimultaneous = allowSimultaneous
            )
        )
    }

    suspend fun moveTaskToInbox(task: TaskEntity) {
        dao.updateTask(
            task.copy(
                dateEpochDay = null,
                taskMode = TaskMode.TIMELESS_STEP.name,
                startTimeMinutes = null,
                isCompleted = false,
                allowSimultaneous = false
            )
        )
    }

    suspend fun deleteTask(taskId: Long) {
        dao.deleteTaskById(taskId)
    }

    /**
     * Live Focus Session Logging:
     * - When Play is tapped, notes current device clock time and feeds it to the timeline.
     * - For Timeless tasks, preserves startTimeMinutes = null so fake clock timestamps are never injected.
     * - When Pause is tapped, saves the segment ("start-pause") so a break is implicitly noted.
     * - When Resume is tapped, starts a new segment so the timeline displays a dotted "Continued" line!
     */
    suspend fun startOrResumeTaskSession(task: TaskEntity, currentClockMin: Int = currentMinutesFromMidnight()) {
        if (task.activeSessionStartMin != null) return
        val updatedStartMin = if (task.modeEnum == TaskMode.TIMED_BLOCK) {
            task.startTimeMinutes ?: currentClockMin
        } else {
            null
        }
        dao.updateTask(
            task.copy(
                startTimeMinutes = updatedStartMin,
                activeSessionStartMin = currentClockMin
            )
        )
    }

    suspend fun pauseTaskSession(task: TaskEntity, currentClockMin: Int = currentMinutesFromMidnight()) {
        val startMin = task.activeSessionStartMin ?: return
        val endMin = currentClockMin.coerceAtLeast(startMin + 1)
        val newSeg = "$startMin-$endMin"
        val updatedSegments = if (task.sessionSegments.isBlank()) {
            newSeg
        } else {
            "${task.sessionSegments}||$newSeg"
        }
        dao.updateTask(
            task.copy(
                sessionSegments = updatedSegments,
                activeSessionStartMin = null
            )
        )
    }

    suspend fun finishTaskSessionAndComplete(task: TaskEntity, currentClockMin: Int = currentMinutesFromMidnight()) {
        val startMin = task.activeSessionStartMin
        val updatedSegments = if (startMin != null) {
            val endMin = currentClockMin.coerceAtLeast(startMin + 1)
            val newSeg = "$startMin-$endMin"
            if (task.sessionSegments.isBlank()) newSeg else "${task.sessionSegments}||$newSeg"
        } else {
            task.sessionSegments
        }
        dao.updateTask(
            task.copy(
                sessionSegments = updatedSegments,
                activeSessionStartMin = null,
                isCompleted = true
            )
        )
    }

    /**
     * Toggles a break loop split on any task so its timeline oval capsule halves
     * and connects via a dotted loop, or merges back into a single continuous oval.
     */
    suspend fun toggleTaskBreakLoop(task: TaskEntity) {
        val hasBreak = task.parsedSessionSegments.size > 1 ||
            (task.parsedSessionSegments.size == 1 && task.activeSessionStartMin != null)
        if (hasBreak) {
            dao.updateTask(
                task.copy(
                    sessionSegments = "",
                    activeSessionStartMin = null
                )
            )
        } else {
            val baseStart = task.startTimeMinutes ?: (9 * 60)
            val halfDur = (task.durationMinutes / 2).coerceAtLeast(10)
            val firstEnd = (baseStart + halfDur).coerceAtMost(23 * 60 + 40)
            val resumeStart = (firstEnd + 10).coerceAtMost(23 * 60 + 49)
            val secondEnd = (resumeStart + halfDur).coerceAtMost(23 * 60 + 59)
            dao.updateTask(
                task.copy(
                    startTimeMinutes = if (task.modeEnum == TaskMode.TIMED_BLOCK) (task.startTimeMinutes ?: baseStart) else null,
                    sessionSegments = "$baseStart-$firstEnd||$resumeStart-$secondEnd",
                    activeSessionStartMin = null
                )
            )
        }
    }

    suspend fun createFolder(
        name: String,
        codeTag: String,
        colorKey: String,
        iconKey: String,
        initialSections: List<String>
    ) {
        if (name.isBlank()) return
        val sectionsCsv = initialSections.ifEmpty { listOf("General", "Milestones") }.joinToString(";;")
        dao.insertFolder(
            FolderEntity(
                codeTag = codeTag.ifBlank { name.take(4).uppercase() },
                name = name.trim(),
                colorKey = colorKey,
                iconKey = iconKey,
                sectionsCsv = sectionsCsv
            )
        )
    }

    suspend fun addSectionToFolder(folder: FolderEntity, newSection: String) {
        if (newSection.isBlank()) return
        val updated = (folder.sections + newSection.trim()).distinct().joinToString(";;")
        dao.updateFolder(folder.copy(sectionsCsv = updated))
    }

    suspend fun deleteFolder(folderId: Long) {
        dao.deleteFolderById(folderId)
    }

    private fun currentMinutesFromMidnight(): Int {
        val now = LocalTime.now()
        return now.hour * 60 + now.minute
    }
}
