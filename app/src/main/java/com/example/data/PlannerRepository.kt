package com.example.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class PlannerRepository(private val dao: PlannerDao) {

    val allTasksFlow: Flow<List<TaskWithSubtasks>> = dao.observeAllTasksWithSubtasks()
    val allTemplatesFlow: Flow<List<DayTemplateEntity>> = dao.observeAllTemplates()

    suspend fun ensureSeedData(todayEpochDay: Long = LocalDate.now().toEpochDay()) {
        if (dao.countTasks() > 0) return

        // 1. Top "Anytime" Routine Bubbles (Inspired by Screenshots 5 & 6)
        dao.insertTask(
            TaskEntity(
                title = "Structure Tomorrow",
                notes = "Review inbox & line up top 3 timeless steps",
                dateEpochDay = todayEpochDay,
                taskMode = TaskMode.TOP_BUBBLE.name,
                durationMinutes = 10,
                sortOrder = 1,
                colorKey = "CORAL",
                iconKey = "CHECKLIST",
                subjectTag = "ROUTINE",
                recurrence = RecurrenceRule.DAILY.name,
                isCompleted = false
            )
        )
        dao.insertTask(
            TaskEntity(
                title = "Water Plants",
                notes = "Desk & balcony plants",
                dateEpochDay = todayEpochDay,
                taskMode = TaskMode.TOP_BUBBLE.name,
                durationMinutes = 10,
                sortOrder = 2,
                colorKey = "MINT",
                iconKey = "PLANT",
                subjectTag = "HOME",
                recurrence = RecurrenceRule.DAILY.name,
                isCompleted = false
            )
        )
        dao.insertTask(
            TaskEntity(
                title = "Call Mum",
                notes = "Quick catch-up call",
                dateEpochDay = todayEpochDay,
                taskMode = TaskMode.TOP_BUBBLE.name,
                durationMinutes = 15,
                sortOrder = 3,
                colorKey = "SKY",
                iconKey = "CALL",
                subjectTag = "FAMILY",
                recurrence = RecurrenceRule.NONE.name,
                isCompleted = true
            )
        )

        // 2. Hybrid Daily Spine (Mixing Timed Anchors + Timeless Anti-Paralysis Steps)
        dao.insertTask(
            TaskEntity(
                title = "Rise and Shine",
                notes = "Hydrate & sunlight before screens",
                dateEpochDay = todayEpochDay,
                taskMode = TaskMode.TIMED_BLOCK.name,
                startTimeMinutes = 8 * 60, // 08:00 AM
                durationMinutes = 15,
                sortOrder = 10,
                colorKey = "CORAL",
                iconKey = "ALARM",
                subjectTag = "MORNING",
                recurrence = RecurrenceRule.DAILY.name,
                isCompleted = true
            )
        )

        val yogaId = dao.insertTask(
            TaskEntity(
                title = "Yoga & Mobility Workout",
                notes = "Full body stretch for posture",
                dateEpochDay = todayEpochDay,
                taskMode = TaskMode.TIMED_BLOCK.name,
                startTimeMinutes = 8 * 60 + 15, // 08:15 AM
                durationMinutes = 30,
                sortOrder = 20,
                colorKey = "SKY",
                iconKey = "WORKOUT",
                subjectTag = "HEALTH",
                recurrence = RecurrenceRule.DAILY.name,
                isCompleted = true
            )
        )
        dao.insertSubtasks(
            listOf(
                SubtaskEntity(taskId = yogaId, title = " Thoracic spine warm-up", stageTag = "WARMUP", isCompleted = true, sortOrder = 1),
                SubtaskEntity(taskId = yogaId, title = "15m Sun Salutations", stageTag = "FLOW", isCompleted = true, sortOrder = 2)
            )
        )

        // Timeless Next Step #1 (Active UP NEXT — No clock anxiety!)
        val physicsId = dao.insertTask(
            TaskEntity(
                title = "Physics: Electric Charges & Fields",
                notes = "No clock limit — complete the steps in order and move on.",
                dateEpochDay = todayEpochDay,
                taskMode = TaskMode.TIMELESS_STEP.name,
                startTimeMinutes = null,
                durationMinutes = 60,
                sortOrder = 30,
                colorKey = "SKY",
                iconKey = "SCIENCE",
                subjectTag = "PHY 12",
                recurrence = RecurrenceRule.NONE.name,
                isCompleted = false
            )
        )
        dao.insertSubtasks(
            listOf(
                SubtaskEntity(taskId = physicsId, title = "Lecture Watched & Core Derivations", stageTag = "LECTURE", isCompleted = true, sortOrder = 1),
                SubtaskEntity(taskId = physicsId, title = "Anki Flashcards Active Recall", stageTag = "ANKI", isCompleted = false, sortOrder = 2),
                SubtaskEntity(taskId = physicsId, title = "NotebookLM Summary & 25 MCQs", stageTag = "MCQS", isCompleted = false, sortOrder = 3),
                SubtaskEntity(taskId = physicsId, title = "Revision 1 Mistake Log", stageTag = "REV 1", isCompleted = false, sortOrder = 4)
            )
        )

        // Timeless Next Step #2
        val mathId = dao.insertTask(
            TaskEntity(
                title = "Mathematics: Calculus Problem Set",
                notes = "Focus on integration techniques without watching the clock",
                dateEpochDay = todayEpochDay,
                taskMode = TaskMode.TIMELESS_STEP.name,
                startTimeMinutes = null,
                durationMinutes = 45,
                sortOrder = 40,
                colorKey = "YELLOW",
                iconKey = "BOOK",
                subjectTag = "MATH 12",
                recurrence = RecurrenceRule.WEEKDAYS.name,
                isCompleted = false
            )
        )
        dao.insertSubtasks(
            listOf(
                SubtaskEntity(taskId = mathId, title = "Review standard integration formulas", stageTag = "REV 1", isCompleted = false, sortOrder = 1),
                SubtaskEntity(taskId = mathId, title = "Solve 15 timed board-pattern problems", stageTag = "MOCK", isCompleted = false, sortOrder = 2)
            )
        )

        // Timed Anchor Block in the afternoon
        val meetingId = dao.insertTask(
            TaskEntity(
                title = "Study Group / Team Sync",
                notes = "Compare mock paper solutions",
                dateEpochDay = todayEpochDay,
                taskMode = TaskMode.TIMED_BLOCK.name,
                startTimeMinutes = 14 * 60 + 30, // 02:30 PM
                durationMinutes = 45,
                sortOrder = 50,
                colorKey = "PINK",
                iconKey = "MEETING",
                subjectTag = "COLLAB",
                recurrence = RecurrenceRule.NONE.name,
                isCompleted = false
            )
        )
        dao.insertSubtasks(
            listOf(
                SubtaskEntity(taskId = meetingId, title = "Review Electrochemistry doubts", stageTag = "STEP 1", isCompleted = false, sortOrder = 1),
                SubtaskEntity(taskId = meetingId, title = "Pick next mock test date", stageTag = "STEP 2", isCompleted = false, sortOrder = 2)
            )
        )

        // Timeless Evening Step
        dao.insertTask(
            TaskEntity(
                title = "Read a Book & Wind Down",
                notes = "20 pages in tablet reading mode",
                dateEpochDay = todayEpochDay,
                taskMode = TaskMode.TIMELESS_STEP.name,
                startTimeMinutes = null,
                durationMinutes = 45,
                sortOrder = 60,
                colorKey = "MINT",
                iconKey = "BOOK",
                subjectTag = "RECHARGE",
                recurrence = RecurrenceRule.DAILY.name,
                isCompleted = false
            )
        )

        // 3. Brain-Dump Inbox Tasks (Inspired by Screenshot 5 left sidebar)
        dao.insertTask(
            TaskEntity(
                title = "Do Laundry",
                notes = "Sort darks & start quick cycle",
                dateEpochDay = null,
                taskMode = TaskMode.TIMELESS_STEP.name,
                durationMinutes = 60,
                sortOrder = 1,
                colorKey = "SKY",
                iconKey = "LAUNDRY",
                subjectTag = "INBOX"
            )
        )
        val chemInboxId = dao.insertTask(
            TaskEntity(
                title = "Chemistry: Chemical Kinetics Notes",
                notes = "Zero & first-order half-life graphs",
                dateEpochDay = null,
                taskMode = TaskMode.TIMELESS_STEP.name,
                durationMinutes = 60,
                sortOrder = 2,
                colorKey = "ORANGE",
                iconKey = "SCIENCE",
                subjectTag = "CHEM 12"
            )
        )
        dao.insertSubtasks(
            listOf(
                SubtaskEntity(taskId = chemInboxId, title = "Arrhenius equation derivation", stageTag = "LECTURE", isCompleted = false, sortOrder = 1),
                SubtaskEntity(taskId = chemInboxId, title = "20 PYQ MCQs", stageTag = "MCQS", isCompleted = false, sortOrder = 2)
            )
        )
        dao.insertTask(
            TaskEntity(
                title = "Call Grandparents",
                notes = "Ask about weekend visit",
                dateEpochDay = null,
                taskMode = TaskMode.TIMELESS_STEP.name,
                durationMinutes = 30,
                sortOrder = 3,
                colorKey = "LAVENDER",
                iconKey = "CALL",
                subjectTag = "FAMILY"
            )
        )
        dao.insertTask(
            TaskEntity(
                title = "Go for a Run",
                notes = "Easy 5km outdoor pace",
                dateEpochDay = null,
                taskMode = TaskMode.TIMELESS_STEP.name,
                durationMinutes = 45,
                sortOrder = 4,
                colorKey = "CORAL",
                iconKey = "FITNESS",
                subjectTag = "HEALTH"
            )
        )

        // 4. Folder-Tabbed Day Blueprints / Syllabus Templates (Inspired by Screenshot 2)
        dao.insertTemplates(
            listOf(
                DayTemplateEntity(
                    tabCode = "FLOW 01",
                    title = "Zero-Clock Sequence Day",
                    subtitle = "5 Timeless Steps • Pure Anti-Paralysis Queue",
                    tagLabel = "TIMELESS FLOW",
                    colorKey = "YELLOW",
                    encodedItems = listOf(
                        "Morning Brain Clear & Coffee::TIMELESS_STEP::-1::20::YELLOW::COFFEE::Jot top 3 priorities;;Clear desk",
                        "Deep Block #1 (Hardest Task First)::TIMELESS_STEP::-1::60::SKY::BOLT::Break into 3 micro-steps;;Finish core draft",
                        "Midday Walk & Stretch::TIMELESS_STEP::-1::25::MINT::WORKOUT::Step outside;;Hydrate",
                        "Deep Block #2 (Hands-On Practice)::TIMELESS_STEP::-1::50::ORANGE::CODE::Complete practice set;;Review mistakes",
                        "Evening Reset & Structure Tomorrow::TOP_BUBBLE::-1::15::CORAL::CHECKLIST::Check off completed steps"
                    ).joinToString("||")
                ),
                DayTemplateEntity(
                    tabCode = "PHY 12",
                    title = "Physics Master Sprint",
                    subtitle = "4 Chapters • 7 Milestones per Block",
                    tagLabel = "PHYSICS",
                    colorKey = "SKY",
                    encodedItems = listOf(
                        "08:30 Morning Formula Warmup::TIMED_BLOCK::510::30::SKY::SCIENCE::Review Anki deck;;Write key constants",
                        "Electrostatics & Capacitance Deep Dive::TIMELESS_STEP::-1::60::SKY::BOOK::Lecture Watched;;Anki Flashcards;;25 MCQs",
                        "Current Electricity Problem Drill::TIMELESS_STEP::-1::50::YELLOW::BOLT::Kirchhoff loop problems;;Revision 1",
                        "16:00 Full Subject Mock Paper::TIMED_BLOCK::960::90::PINK::CHECKLIST::Timed section A;;Self-grade mistakes"
                    ).joinToString("||")
                ),
                DayTemplateEntity(
                    tabCode = "CHEM 12",
                    title = "Chemistry & Lab Hybrid",
                    subtitle = "4 Steps • Mixes Timed Anchors + Timeless Flow",
                    tagLabel = "CHEMISTRY",
                    colorKey = "ORANGE",
                    encodedItems = listOf(
                        "09:00 Organic Mechanisms Review::TIMED_BLOCK::540::45::ORANGE::SCIENCE::Named reactions sheet;;Practice conversions",
                        "Physical Chemistry Numericals::TIMELESS_STEP::-1::50::YELLOW::BOOK::Electrochemistry Nernst eq;;15 MCQs",
                        "Inorganic Trends & Exceptions::TIMELESS_STEP::-1::40::MINT::CHECKLIST::d- and f-block notes;;Anki review"
                    ).joinToString("||")
                ),
                DayTemplateEntity(
                    tabCode = "RESET 04",
                    title = "Calm Recharge & Admin",
                    subtitle = "4 Gentle Steps • Low-Pressure Recovery Day",
                    tagLabel = "RECHARGE",
                    colorKey = "MINT",
                    encodedItems = listOf(
                        "Water Plants & Tidy Workspace::TOP_BUBBLE::-1::15::MINT::PLANT::Open windows;;Clear desk",
                        "Light Reading & Coffee::TIMELESS_STEP::-1::40::MINT::COFFEE::Read 1 chapter",
                        "Inbox Zero & Week Planning::TIMELESS_STEP::-1::30::LAVENDER::CHECKLIST::Review Inbox items;;Pick Monday flow",
                        "Evening Mobility & Early Sleep::TIMELESS_STEP::-1::30::SKY::WORKOUT::15m stretch"
                    ).joinToString("||")
                )
            )
        )
    }

    suspend fun saveTaskWithSubtasks(
        task: TaskEntity,
        subtaskTitles: List<Pair<String, String>>
    ): Long {
        val nextOrder = if (task.id == 0L && task.dateEpochDay != null) {
            (dao.getMaxSortOrderForDay(task.dateEpochDay) ?: 0) + 10
        } else {
            task.sortOrder
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
        startTimeMinutes: Int? = null
    ) {
        val nextOrder = (dao.getMaxSortOrderForDay(targetEpochDay) ?: 0) + 10
        dao.updateTask(
            task.copy(
                dateEpochDay = targetEpochDay,
                taskMode = if (asTimed && startTimeMinutes != null) {
                    TaskMode.TIMED_BLOCK.name
                } else {
                    TaskMode.TIMELESS_STEP.name
                },
                startTimeMinutes = if (asTimed) startTimeMinutes else null,
                sortOrder = nextOrder
            )
        )
    }

    suspend fun moveTaskToInbox(task: TaskEntity) {
        dao.updateTask(
            task.copy(
                dateEpochDay = null,
                taskMode = TaskMode.TIMELESS_STEP.name,
                startTimeMinutes = null,
                isCompleted = false
            )
        )
    }

    suspend fun swapTaskSortOrder(first: TaskEntity, second: TaskEntity) {
        val firstOrder = first.sortOrder
        val secondOrder = second.sortOrder
        if (firstOrder == secondOrder) {
            dao.updateTask(first.copy(sortOrder = secondOrder + 5))
        } else {
            dao.updateTask(first.copy(sortOrder = secondOrder))
            dao.updateTask(second.copy(sortOrder = firstOrder))
        }
    }

    suspend fun deleteTask(taskId: Long) {
        dao.deleteTaskById(taskId)
    }

    suspend fun resetDayProgress(epochDay: Long) {
        dao.resetDayCompletion(epochDay)
    }

    suspend fun applyTemplateToDay(template: DayTemplateEntity, targetEpochDay: Long) {
        var currentOrder = (dao.getMaxSortOrderForDay(targetEpochDay) ?: 0) + 10
        val specs = template.decodeSpecs()
        for (spec in specs) {
            val newTaskId = dao.insertTask(
                TaskEntity(
                    title = spec.title,
                    notes = "From ${template.title} blueprint",
                    dateEpochDay = targetEpochDay,
                    taskMode = spec.mode.name,
                    startTimeMinutes = spec.startTimeMinutes,
                    durationMinutes = spec.durationMinutes,
                    sortOrder = currentOrder,
                    colorKey = spec.colorKey,
                    iconKey = spec.iconKey,
                    subjectTag = template.tabCode,
                    isCompleted = false
                )
            )
            if (spec.subtasks.isNotEmpty()) {
                dao.insertSubtasks(
                    spec.subtasks.mapIndexed { idx, subTitle ->
                        SubtaskEntity(
                            taskId = newTaskId,
                            title = subTitle,
                            stageTag = "STEP ${idx + 1}",
                            isCompleted = false,
                            sortOrder = idx + 1
                        )
                    }
                )
            }
            currentOrder += 10
        }
    }

    suspend fun saveDayAsTemplate(
        tabCode: String,
        title: String,
        tagLabel: String,
        colorKey: String,
        dayTasks: List<TaskWithSubtasks>
    ) {
        if (dayTasks.isEmpty()) return
        val encoded = dayTasks.joinToString("||") { item ->
            val t = item.task
            val subs = item.sortedSubtasks.joinToString(";;") { it.title }
            "${t.title}::${t.taskMode}::${t.startTimeMinutes ?: -1}::${t.durationMinutes}::${t.colorKey}::${t.iconKey}::$subs"
        }
        dao.insertTemplate(
            DayTemplateEntity(
                tabCode = tabCode.ifBlank { "FLOW Custom" },
                title = title.ifBlank { "Custom Day Structure" },
                subtitle = "${dayTasks.size} Steps • Saved from Day Flow",
                tagLabel = tagLabel.ifBlank { "CUSTOM FLOW" },
                colorKey = colorKey,
                encodedItems = encoded
            )
        )
    }
}
