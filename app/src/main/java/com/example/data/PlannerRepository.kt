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
                completedEpochDaysCsv = todayEpochDay.toString(),
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
                completedEpochDaysCsv = todayEpochDay.toString(),
                isCompleted = true
            )
        )
        dao.insertSubtasks(
            listOf(
                SubtaskEntity(taskId = yogaId, title = "Warm-up stretch", stageTag = "STEP 1", isCompleted = true, sortOrder = 1),
                SubtaskEntity(taskId = yogaId, title = "Core mobility", stageTag = "STEP 2", isCompleted = true, sortOrder = 2)
            )
        )

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

        val physicsSegs = "${14 * 60}-${14 * 60 + 37}||${14 * 60 + 47}-${15 * 60 + 15}"
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
                sessionSegments = physicsSegs,
                sessionSegmentsByDayCsv = "$todayEpochDay:$physicsSegs"
            )
        )
        dao.insertSubtasks(
            listOf(
                SubtaskEntity(taskId = physicsId, title = "Lecture Watched", stageTag = "LECTURE", isCompleted = true, sortOrder = 1),
                SubtaskEntity(taskId = physicsId, title = "Anki Flashcards", stageTag = "ANKI", isCompleted = false, sortOrder = 2),
                SubtaskEntity(taskId = physicsId, title = "25 Chapter MCQs", stageTag = "MCQS", isCompleted = false, sortOrder = 3)
            )
        )

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
            nextDaySortOrder(dao.getMaxSortOrderForDay(task.dateEpochDay))
        } else {
            normalizeTaskSortOrder(task.sortOrder)
        }

        val cleanedInputs = subtaskTitles
            .map { (rawTitle, rawStage) -> rawTitle.trim() to rawStage.trim() }
            .filter { it.first.isNotEmpty() }

        if (task.id == 0L) {
            val newTaskId = dao.insertTask(task.copy(sortOrder = nextOrder))
            val newSubtasks = cleanedInputs.mapIndexed { idx, (title, stage) ->
                SubtaskEntity(
                    taskId = newTaskId,
                    title = title,
                    stageTag = stage.ifBlank { "STEP ${idx + 1}" },
                    isCompleted = false,
                    sortOrder = idx + 1
                )
            }
            if (newSubtasks.isNotEmpty()) {
                dao.insertSubtasks(newSubtasks)
            }
            return newTaskId
        }

        val taskId = task.id
        val existingSubtasks = dao.getSubtasksForTask(taskId)
        dao.updateTask(task.copy(sortOrder = nextOrder))

        val consumedExistingIds = mutableSetOf<Long>()
        val matchedByIndex = arrayOfNulls<SubtaskEntity>(cleanedInputs.size)

        // Pass 1: Match by exact/case-insensitive title so reordered subtasks keep their ID & completion state
        for (idx in cleanedInputs.indices) {
            val targetTitle = cleanedInputs[idx].first
            val titleMatch = existingSubtasks.firstOrNull { existing ->
                existing.id !in consumedExistingIds &&
                    existing.title.trim().equals(targetTitle, ignoreCase = true)
            }
            if (titleMatch != null) {
                consumedExistingIds.add(titleMatch.id)
                matchedByIndex[idx] = titleMatch
            }
        }

        // Pass 2: Match remaining slots positionally so in-place renamed subtasks keep their ID & completion state
        for (idx in cleanedInputs.indices) {
            if (matchedByIndex[idx] == null && idx < existingSubtasks.size) {
                val positionalCandidate = existingSubtasks[idx]
                if (positionalCandidate.id !in consumedExistingIds) {
                    consumedExistingIds.add(positionalCandidate.id)
                    matchedByIndex[idx] = positionalCandidate
                }
            }
        }

        val idsToDelete = existingSubtasks
            .map { it.id }
            .filter { it !in consumedExistingIds }
        if (idsToDelete.isNotEmpty()) {
            dao.deleteSubtasksByIds(idsToDelete)
        }

        val reconciledEntities = cleanedInputs.mapIndexed { idx, (title, stage) ->
            val matched = matchedByIndex[idx]
            SubtaskEntity(
                id = matched?.id ?: 0L,
                taskId = taskId,
                title = title,
                stageTag = stage.ifBlank { matched?.stageTag?.ifBlank { null } ?: "STEP ${idx + 1}" },
                isCompleted = matched?.isCompleted ?: false,
                sortOrder = idx + 1
            )
        }
        if (reconciledEntities.isNotEmpty()) {
            dao.insertSubtasks(reconciledEntities)
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

    private val realtimeLock = Any()
    private val lastPauseRealtimeByTaskDay = mutableMapOf<Pair<Long, Long>, Long>()
    private val lastStartRealtimeByTaskDay = mutableMapOf<Pair<Long, Long>, Long>()

    private fun clearRealtimeForTask(taskId: Long) {
        synchronized(realtimeLock) {
            lastPauseRealtimeByTaskDay.keys.removeAll { it.first == taskId }
            lastStartRealtimeByTaskDay.keys.removeAll { it.first == taskId }
        }
    }

    suspend fun toggleTaskCompleted(
        task: TaskEntity,
        targetEpochDay: Long = LocalDate.now().toEpochDay(),
        todayEpochDay: Long = LocalDate.now().toEpochDay()
    ): Boolean {
        if (task.recurrenceEnum != RecurrenceRule.NONE) {
            val markingDone = !task.isCompletedOnDay(targetEpochDay)
            dao.updateTask(task.withUpdatedDayCompletion(targetEpochDay, markingDone))
            return false
        }

        val markingDone = !task.isCompleted
        val isFutureDated = task.dateEpochDay != null && task.dateEpochDay > todayEpochDay
        val targetDateEpoch = if (markingDone && isFutureDated) todayEpochDay else task.dateEpochDay
        val updatedOrder = if (markingDone && isFutureDated) {
            nextDaySortOrder(dao.getMaxSortOrderForDay(todayEpochDay))
        } else {
            task.sortOrder
        }
        dao.updateTask(
            task.copy(
                dateEpochDay = targetDateEpoch,
                sortOrder = updatedOrder,
                isCompleted = markingDone
            )
        )
        return markingDone && isFutureDated
    }

    suspend fun extendTaskDurationMinutes(task: TaskEntity, deltaMinutes: Int) {
        val nextDuration = (task.durationMinutes + deltaMinutes).coerceIn(1, 720)
        dao.updateTask(task.copy(durationMinutes = nextDuration))
    }

    suspend fun toggleSubtaskCompleted(subtask: SubtaskEntity) {
        dao.setSubtaskCompleted(subtask.id, !subtask.isCompleted)
    }

    suspend fun addQuickSubtask(taskId: Long, title: String, stageTag: String = "STEP") {
        if (title.isBlank()) return
        val nextSortOrder = ((dao.getMaxSubtaskSortOrder(taskId) ?: 0) + 1).coerceAtLeast(1)
        dao.insertSubtask(
            SubtaskEntity(
                taskId = taskId,
                title = title.trim(),
                stageTag = stageTag,
                isCompleted = false,
                sortOrder = nextSortOrder
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
        val nextOrder = nextDaySortOrder(dao.getMaxSortOrderForDay(targetEpochDay))
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
        clearRealtimeForTask(task.id)
        dao.updateTask(
            task.copy(
                dateEpochDay = null,
                taskMode = TaskMode.TIMELESS_STEP.name,
                startTimeMinutes = null,
                isCompleted = false,
                completedEpochDaysCsv = "",
                sessionSegments = "",
                sessionSegmentsByDayCsv = "",
                activeSessionStartMin = null,
                activeSessionEpochDay = null,
                allowSimultaneous = false
            )
        )
    }

    suspend fun deleteTask(taskId: Long) {
        clearRealtimeForTask(taskId)
        dao.deleteTaskById(taskId)
    }

    suspend fun deleteTasksByIds(taskIds: List<Long>) {
        if (taskIds.isEmpty()) return
        taskIds.forEach { id -> clearRealtimeForTask(id) }
        dao.deleteTasksByIds(taskIds)
    }

    suspend fun startOrResumeTaskSession(
        task: TaskEntity,
        targetEpochDay: Long = LocalDate.now().toEpochDay(),
        currentClockMin: Int = currentMinutesFromMidnight(),
        todayEpochDay: Long = LocalDate.now().toEpochDay()
    ): Pair<Boolean, Boolean> {
        val isRecurring = task.recurrenceEnum != RecurrenceRule.NONE
        val isFutureDatedOneTime = !isRecurring && task.dateEpochDay != null && task.dateEpochDay > todayEpochDay
        val effectiveEpochDay = if (isFutureDatedOneTime) todayEpochDay else targetEpochDay

        if (task.activeSessionStartMinForDay(effectiveEpochDay) != null) return false to false
        val nowRealtime = android.os.SystemClock.elapsedRealtime()
        val targetDateEpoch = if (isFutureDatedOneTime) todayEpochDay else task.dateEpochDay
        val updatedOrder = if (isFutureDatedOneTime) {
            nextDaySortOrder(dao.getMaxSortOrderForDay(todayEpochDay))
        } else {
            task.sortOrder
        }

        val updatedStartMin = if (task.modeEnum == TaskMode.TIMED_BLOCK) {
            task.startTimeMinutes ?: currentClockMin
        } else {
            null
        }

        val key = task.id to effectiveEpochDay
        val existingSegs = task.parsedSessionSegmentsForDay(effectiveEpochDay)
        val isMicroPause = synchronized(realtimeLock) {
            val lastPauseMs = lastPauseRealtimeByTaskDay[key]
            val micro = lastPauseMs != null &&
                (nowRealtime - lastPauseMs) < 60_000L &&
                existingSegs.isNotEmpty()
            lastStartRealtimeByTaskDay[key] = nowRealtime
            lastPauseRealtimeByTaskDay.remove(key)
            micro
        }

        if (isMicroPause) {
            val lastSegStart = existingSegs.last().first
            val remainingSegs = existingSegs.dropLast(1).joinToString("||") { "${it.first}-${it.second}" }
            dao.updateTask(
                task
                    .withUpdatedDaySession(
                        epochDay = effectiveEpochDay,
                        newSegments = remainingSegs,
                        newActiveStartMin = lastSegStart
                    )
                    .copy(
                        dateEpochDay = targetDateEpoch,
                        sortOrder = updatedOrder,
                        startTimeMinutes = updatedStartMin
                    )
            )
            return true to isFutureDatedOneTime
        }

        val resolvedActiveStartMin = if (existingSegs.isNotEmpty() && currentClockMin < existingSegs.last().second) {
            currentClockMin + 1440
        } else {
            currentClockMin
        }

        dao.updateTask(
            task
                .withUpdatedDaySession(
                    epochDay = effectiveEpochDay,
                    newSegments = task.sessionSegmentsForDay(effectiveEpochDay),
                    newActiveStartMin = resolvedActiveStartMin
                )
                .copy(
                    dateEpochDay = targetDateEpoch,
                    sortOrder = updatedOrder,
                    startTimeMinutes = updatedStartMin
                )
        )
        return false to isFutureDatedOneTime
    }

    suspend fun pauseTaskSession(
        task: TaskEntity,
        targetEpochDay: Long = LocalDate.now().toEpochDay(),
        currentClockMin: Int = currentMinutesFromMidnight()
    ): Boolean {
        val effectiveEpochDay = task.activeSessionEpochDay ?: targetEpochDay
        val startMin = task.activeSessionStartMinForDay(effectiveEpochDay) ?: task.activeSessionStartMin ?: return false
        val nowRealtime = android.os.SystemClock.elapsedRealtime()
        val key = task.id to effectiveEpochDay
        val existingSegs = task.parsedSessionSegmentsForDay(effectiveEpochDay)

        val isShortBurst = synchronized(realtimeLock) {
            val lastStartMs = lastStartRealtimeByTaskDay[key]
            lastPauseRealtimeByTaskDay[key] = nowRealtime
            existingSegs.isNotEmpty() && lastStartMs != null && (nowRealtime - lastStartMs) < 60_000L
        }

        if (isShortBurst) {
            val prevSegStart = existingSegs.last().first
            val prevSegEnd = maxOf(existingSegs.last().second, computeSessionEndMinute(prevSegStart, currentClockMin))
            val mergedList = existingSegs.dropLast(1) + (prevSegStart to prevSegEnd)
            val updatedSegments = mergedList.joinToString("||") { "${it.first}-${it.second}" }
            dao.updateTask(
                task.withUpdatedDaySession(
                    epochDay = effectiveEpochDay,
                    newSegments = updatedSegments,
                    newActiveStartMin = null
                )
            )
            return true
        }

        val endMin = computeSessionEndMinute(startMin, currentClockMin)
        val newSeg = "$startMin-$endMin"
        val daySegments = task.sessionSegmentsForDay(effectiveEpochDay)
        val updatedSegments = if (daySegments.isBlank()) {
            newSeg
        } else {
            "$daySegments||$newSeg"
        }
        dao.updateTask(
            task.withUpdatedDaySession(
                epochDay = effectiveEpochDay,
                newSegments = updatedSegments,
                newActiveStartMin = null
            )
        )
        return false
    }

    suspend fun finishTaskSessionAndComplete(
        task: TaskEntity,
        targetEpochDay: Long = LocalDate.now().toEpochDay(),
        currentClockMin: Int = currentMinutesFromMidnight(),
        todayEpochDay: Long = LocalDate.now().toEpochDay()
    ): Boolean {
        val isRecurring = task.recurrenceEnum != RecurrenceRule.NONE
        val isFutureDatedOneTime = !isRecurring && task.dateEpochDay != null && task.dateEpochDay > todayEpochDay
        val effectiveEpochDay = if (isFutureDatedOneTime) todayEpochDay else (task.activeSessionEpochDay ?: targetEpochDay)

        synchronized(realtimeLock) {
            lastPauseRealtimeByTaskDay.remove(task.id to effectiveEpochDay)
            lastStartRealtimeByTaskDay.remove(task.id to effectiveEpochDay)
        }

        val targetDateEpoch = if (isFutureDatedOneTime) todayEpochDay else task.dateEpochDay
        val updatedOrder = if (isFutureDatedOneTime) {
            nextDaySortOrder(dao.getMaxSortOrderForDay(todayEpochDay))
        } else {
            task.sortOrder
        }
        val startMin = task.activeSessionStartMinForDay(effectiveEpochDay) ?: task.activeSessionStartMin
        val daySegments = task.sessionSegmentsForDay(effectiveEpochDay)
        val updatedSegments = if (startMin != null) {
            val endMin = computeSessionEndMinute(startMin, currentClockMin)
            val newSeg = "$startMin-$endMin"
            if (daySegments.isBlank()) newSeg else "$daySegments||$newSeg"
        } else {
            daySegments
        }
        dao.updateTask(
            task
                .withUpdatedDaySession(
                    epochDay = effectiveEpochDay,
                    newSegments = updatedSegments,
                    newActiveStartMin = null
                )
                .withUpdatedDayCompletion(
                    epochDay = effectiveEpochDay,
                    completed = true
                )
                .copy(
                    dateEpochDay = targetDateEpoch,
                    sortOrder = updatedOrder
                )
        )
        return isFutureDatedOneTime
    }

    suspend fun toggleTaskBreakLoop(
        task: TaskEntity,
        targetEpochDay: Long = LocalDate.now().toEpochDay()
    ) {
        val hasBreak = task.hasBreakLoopOnDay(targetEpochDay)
        if (hasBreak) {
            dao.updateTask(
                task.withUpdatedDaySession(
                    epochDay = targetEpochDay,
                    newSegments = "",
                    newActiveStartMin = null
                )
            )
        } else {
            val baseStart = task.startTimeMinutes ?: (9 * 60)
            val halfDur = (task.durationMinutes / 2).coerceAtLeast(10)
            val firstEnd = (baseStart + halfDur).coerceAtMost(23 * 60 + 40)
            val resumeStart = (firstEnd + 10).coerceAtMost(23 * 60 + 49)
            val secondEnd = (resumeStart + halfDur).coerceAtMost(23 * 60 + 59)
            dao.updateTask(
                task
                    .withUpdatedDaySession(
                        epochDay = targetEpochDay,
                        newSegments = "$baseStart-$firstEnd||$resumeStart-$secondEnd",
                        newActiveStartMin = null
                    )
                    .copy(
                        startTimeMinutes = if (task.modeEnum == TaskMode.TIMED_BLOCK) (task.startTimeMinutes ?: baseStart) else null
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

    private fun currentMinutesFromMidnight(): Int {
        val now = LocalTime.now()
        return now.hour * 60 + now.minute
    }
}
