package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DayTemplateEntity
import com.example.data.PlannerDatabase
import com.example.data.PlannerRepository
import com.example.data.RecurrenceRule
import com.example.data.SubtaskEntity
import com.example.data.TaskEntity
import com.example.data.TaskMode
import com.example.data.TaskWithSubtasks
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate

enum class StruktDestination(val label: String) {
    FLOW("Day Flow"),
    INBOX("Inbox"),
    BLUEPRINTS("Blueprints & Matrix"),
    FOCUS("Focus Mode")
}

enum class FlowFilterMode(val label: String) {
    ALL("All Structure"),
    TIMELESS_ONLY("Timeless Steps"),
    TIMED_ONLY("Timed Slots")
}

data class PlannerUiState(
    val selectedDateEpochDay: Long = LocalDate.now().toEpochDay(),
    val activeDestination: StruktDestination = StruktDestination.FLOW,
    val flowFilter: FlowFilterMode = FlowFilterMode.ALL,
    val searchQuery: String = "",
    val topBubbles: List<TaskWithSubtasks> = emptyList(),
    val spineFlowTasks: List<TaskWithSubtasks> = emptyList(),
    val upNextTask: TaskWithSubtasks? = null,
    val inboxTasks: List<TaskWithSubtasks> = emptyList(),
    val allTasks: List<TaskWithSubtasks> = emptyList(),
    val templates: List<DayTemplateEntity> = emptyList(),
    val weekDotColorsByEpochDay: Map<Long, List<String>> = emptyMap(),
    val editingTask: TaskWithSubtasks? = null,
    val isEditorVisible: Boolean = false,
    val editorDefaultMode: TaskMode = TaskMode.TIMELESS_STEP,
    val editorTargetInbox: Boolean = false,
    val focusedTaskId: Long? = null,
    val focusStartedAtMs: Long? = null,
    val statusBannerMessage: String? = null
) {
    val selectedLocalDate: LocalDate
        get() = LocalDate.ofEpochDay(selectedDateEpochDay)

    val activeFocusTask: TaskWithSubtasks?
        get() = allTasks.firstOrNull { it.task.id == focusedTaskId } ?: upNextTask

    val totalDayTasksCount: Int
        get() = topBubbles.size + spineFlowTasks.size

    val completedDayTasksCount: Int
        get() = topBubbles.count { it.task.isCompleted } + spineFlowTasks.count { it.task.isCompleted }

    val dayReadinessPercent: Int
        get() = if (totalDayTasksCount == 0) 0 else ((completedDayTasksCount * 100f) / totalDayTasksCount).toInt()
}

private data class LocalNavState(
    val selectedDateEpochDay: Long = LocalDate.now().toEpochDay(),
    val activeDestination: StruktDestination = StruktDestination.FLOW,
    val flowFilter: FlowFilterMode = FlowFilterMode.ALL,
    val searchQuery: String = "",
    val editingTask: TaskWithSubtasks? = null,
    val isEditorVisible: Boolean = false,
    val editorDefaultMode: TaskMode = TaskMode.TIMELESS_STEP,
    val editorTargetInbox: Boolean = false,
    val focusedTaskId: Long? = null,
    val focusStartedAtMs: Long? = null,
    val statusBannerMessage: String? = null
)

class PlannerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PlannerRepository = PlannerRepository(
        PlannerDatabase.getInstance(application).plannerDao()
    )

    private val navState = MutableStateFlow(LocalNavState())

    init {
        viewModelScope.launch {
            repository.ensureSeedData(LocalDate.now().toEpochDay())
        }
    }

    val uiState: StateFlow<PlannerUiState> = combine(
        repository.allTasksFlow,
        repository.allTemplatesFlow,
        navState
    ) { allTasks, templates, nav ->
        val selectedDate = LocalDate.ofEpochDay(nav.selectedDateEpochDay)

        // Filter tasks that belong to the selected day (or match recurrence rules)
        val tasksForSelectedDay = allTasks.filter { item ->
            matchesDate(item.task, selectedDate)
        }

        val bubbles = tasksForSelectedDay
            .filter { it.task.modeEnum == TaskMode.TOP_BUBBLE }
            .sortedBy { it.task.sortOrder }

        val fullSpine = tasksForSelectedDay
            .filter { it.task.modeEnum != TaskMode.TOP_BUBBLE }
            .sortedWith(
                compareBy<TaskWithSubtasks> { it.task.sortOrder }
                    .thenBy { it.task.startTimeMinutes ?: 9999 }
                    .thenBy { it.task.id }
            )

        val filteredSpine = when (nav.flowFilter) {
            FlowFilterMode.ALL -> fullSpine
            FlowFilterMode.TIMELESS_ONLY -> fullSpine.filter { it.task.modeEnum == TaskMode.TIMELESS_STEP }
            FlowFilterMode.TIMED_ONLY -> fullSpine.filter { it.task.modeEnum == TaskMode.TIMED_BLOCK }
        }

        // First incomplete spine task (or bubble if all spine tasks are done) is the anti-paralysis UP NEXT task
        val nextUp = fullSpine.firstOrNull { !it.task.isCompleted }
            ?: bubbles.firstOrNull { !it.task.isCompleted }

        val inbox = allTasks
            .filter { it.task.dateEpochDay == null }
            .filter {
                nav.searchQuery.isBlank() ||
                    it.task.title.contains(nav.searchQuery, ignoreCase = true) ||
                    it.task.subjectTag.contains(nav.searchQuery, ignoreCase = true)
            }

        // Compute miniature task color dots for the 7-day week strip around selectedDate
        val startOfWeek = selectedDate.minusDays((selectedDate.dayOfWeek.value - 1).toLong())
        val weekDots = buildMap<Long, List<String>> {
            for (offset in 0L..6L) {
                val day = startOfWeek.plusDays(offset)
                val dayEpoch = day.toEpochDay()
                val colors = allTasks
                    .filter { matchesDate(it.task, day) }
                    .take(5)
                    .map { it.task.colorKey }
                put(dayEpoch, colors)
            }
        }

        PlannerUiState(
            selectedDateEpochDay = nav.selectedDateEpochDay,
            activeDestination = nav.activeDestination,
            flowFilter = nav.flowFilter,
            searchQuery = nav.searchQuery,
            topBubbles = bubbles,
            spineFlowTasks = filteredSpine,
            upNextTask = nextUp,
            inboxTasks = inbox,
            allTasks = allTasks,
            templates = templates,
            weekDotColorsByEpochDay = weekDots,
            editingTask = nav.editingTask,
            isEditorVisible = nav.isEditorVisible,
            editorDefaultMode = nav.editorDefaultMode,
            editorTargetInbox = nav.editorTargetInbox,
            focusedTaskId = nav.focusedTaskId,
            focusStartedAtMs = nav.focusStartedAtMs,
            statusBannerMessage = nav.statusBannerMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlannerUiState()
    )

    private fun matchesDate(task: TaskEntity, targetDate: LocalDate): Boolean {
        val taskEpoch = task.dateEpochDay ?: return false
        if (taskEpoch == targetDate.toEpochDay()) return true
        return when (task.recurrenceEnum) {
            RecurrenceRule.NONE -> false
            RecurrenceRule.DAILY -> true
            RecurrenceRule.WEEKDAYS -> targetDate.dayOfWeek != DayOfWeek.SATURDAY && targetDate.dayOfWeek != DayOfWeek.SUNDAY
            RecurrenceRule.WEEKENDS -> targetDate.dayOfWeek == DayOfWeek.SATURDAY || targetDate.dayOfWeek == DayOfWeek.SUNDAY
        }
    }

    fun selectDate(epochDay: Long) {
        navState.update { it.copy(selectedDateEpochDay = epochDay) }
    }

    fun jumpToToday() {
        navState.update { it.copy(selectedDateEpochDay = LocalDate.now().toEpochDay()) }
    }

    fun shiftWeek(weeksDelta: Long) {
        navState.update { state ->
            val nextDate = LocalDate.ofEpochDay(state.selectedDateEpochDay).plusWeeks(weeksDelta)
            state.copy(selectedDateEpochDay = nextDate.toEpochDay())
        }
    }

    fun setDestination(destination: StruktDestination) {
        navState.update { state ->
            val startMs = if (destination == StruktDestination.FOCUS && state.focusStartedAtMs == null) {
                System.currentTimeMillis()
            } else {
                state.focusStartedAtMs
            }
            state.copy(activeDestination = destination, focusStartedAtMs = startMs)
        }
    }

    fun setFlowFilter(filter: FlowFilterMode) {
        navState.update { it.copy(flowFilter = filter) }
    }

    fun setSearchQuery(query: String) {
        navState.update { it.copy(searchQuery = query) }
    }

    fun openCreateTaskSheet(
        defaultMode: TaskMode = TaskMode.TIMELESS_STEP,
        forInbox: Boolean = false
    ) {
        navState.update {
            it.copy(
                editingTask = null,
                isEditorVisible = true,
                editorDefaultMode = defaultMode,
                editorTargetInbox = forInbox
            )
        }
    }

    fun openEditTaskSheet(taskWithSubtasks: TaskWithSubtasks) {
        navState.update {
            it.copy(
                editingTask = taskWithSubtasks,
                isEditorVisible = true,
                editorDefaultMode = taskWithSubtasks.task.modeEnum,
                editorTargetInbox = taskWithSubtasks.task.dateEpochDay == null
            )
        }
    }

    fun closeTaskSheet() {
        navState.update { it.copy(isEditorVisible = false, editingTask = null) }
    }

    fun saveTaskFromEditor(
        existingId: Long,
        title: String,
        notes: String,
        toInbox: Boolean,
        taskMode: TaskMode,
        startTimeMinutes: Int?,
        durationMinutes: Int,
        colorKey: String,
        iconKey: String,
        subjectTag: String,
        recurrence: RecurrenceRule,
        subtasks: List<Pair<String, String>>,
        sortOrder: Int
    ) {
        if (title.isBlank()) return
        val currentEpoch = navState.value.selectedDateEpochDay
        val targetEpoch = if (toInbox) null else currentEpoch
        val entity = TaskEntity(
            id = existingId,
            title = title.trim(),
            notes = notes.trim(),
            dateEpochDay = targetEpoch,
            taskMode = taskMode.name,
            startTimeMinutes = if (taskMode == TaskMode.TIMED_BLOCK) (startTimeMinutes ?: 9 * 60) else null,
            durationMinutes = durationMinutes.coerceIn(5, 360),
            sortOrder = sortOrder,
            colorKey = colorKey,
            iconKey = iconKey,
            subjectTag = subjectTag.ifBlank { if (toInbox) "INBOX" else "FLOW" },
            recurrence = recurrence.name,
            isCompleted = navState.value.editingTask?.task?.isCompleted ?: false
        )
        viewModelScope.launch {
            repository.saveTaskWithSubtasks(entity, subtasks)
            navState.update {
                it.copy(
                    isEditorVisible = false,
                    editingTask = null,
                    statusBannerMessage = if (toInbox) "Saved to Brain-Dump Inbox" else "Added to Daily Structure"
                )
            }
        }
    }

    fun quickAddInboxTask(title: String, durationMinutes: Int = 30, colorKey: String = "SKY") {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.saveTaskWithSubtasks(
                task = TaskEntity(
                    title = title.trim(),
                    dateEpochDay = null,
                    taskMode = TaskMode.TIMELESS_STEP.name,
                    durationMinutes = durationMinutes,
                    colorKey = colorKey,
                    iconKey = "BOLT",
                    subjectTag = "INBOX"
                ),
                subtaskTitles = emptyList()
            )
        }
    }

    fun sendInboxTaskToSelectedDay(task: TaskEntity, asTimed: Boolean = false, startTimeMinutes: Int? = null) {
        val targetDay = navState.value.selectedDateEpochDay
        viewModelScope.launch {
            repository.scheduleInboxTaskToDay(task, targetDay, asTimed, startTimeMinutes)
            navState.update {
                it.copy(statusBannerMessage = "Scheduled '${task.title}' into Day Flow")
            }
        }
    }

    fun sendTaskBackToInbox(task: TaskEntity) {
        viewModelScope.launch {
            repository.moveTaskToInbox(task)
            navState.update {
                it.copy(
                    isEditorVisible = false,
                    editingTask = null,
                    statusBannerMessage = "Moved '${task.title}' back to Inbox"
                )
            }
        }
    }

    fun toggleTaskDone(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleTaskCompleted(task)
        }
    }

    fun toggleSubtaskDone(subtask: SubtaskEntity) {
        viewModelScope.launch {
            repository.toggleSubtaskCompleted(subtask)
        }
    }

    fun addQuickSubtask(taskId: Long, title: String) {
        viewModelScope.launch {
            repository.addQuickSubtask(taskId, title)
        }
    }

    fun moveSpineTask(taskId: Long, moveUp: Boolean) {
        val currentList = uiState.value.spineFlowTasks
        val index = currentList.indexOfFirst { it.task.id == taskId }
        if (index == -1) return
        val targetIndex = if (moveUp) index - 1 else index + 1
        if (targetIndex !in currentList.indices) return
        val first = currentList[index].task
        val second = currentList[targetIndex].task
        viewModelScope.launch {
            repository.swapTaskSortOrder(first, second)
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
            navState.update {
                it.copy(
                    isEditorVisible = false,
                    editingTask = null,
                    statusBannerMessage = "Task removed"
                )
            }
        }
    }

    fun enterFocusForTask(taskId: Long?) {
        navState.update {
            it.copy(
                focusedTaskId = taskId,
                focusStartedAtMs = System.currentTimeMillis(),
                activeDestination = StruktDestination.FOCUS
            )
        }
    }

    fun completeFocusedTaskAndAdvance() {
        val currentFocus = uiState.value.activeFocusTask ?: return
        viewModelScope.launch {
            if (!currentFocus.task.isCompleted) {
                repository.toggleTaskCompleted(currentFocus.task)
            }
            navState.update {
                it.copy(
                    focusedTaskId = null,
                    focusStartedAtMs = System.currentTimeMillis(),
                    statusBannerMessage = "Completed '${currentFocus.task.title}'! Advancing to next step."
                )
            }
        }
    }

    fun applyBlueprintToSelectedDay(template: DayTemplateEntity) {
        val targetDay = navState.value.selectedDateEpochDay
        viewModelScope.launch {
            repository.applyTemplateToDay(template, targetDay)
            navState.update {
                it.copy(
                    activeDestination = StruktDestination.FLOW,
                    statusBannerMessage = "Applied '${template.title}' to your day!"
                )
            }
        }
    }

    fun saveCurrentDayAsBlueprint(title: String, tabCode: String, colorKey: String) {
        val dayTasks = uiState.value.topBubbles + uiState.value.spineFlowTasks
        if (dayTasks.isEmpty()) return
        viewModelScope.launch {
            repository.saveDayAsTemplate(
                tabCode = tabCode,
                title = title,
                tagLabel = "CUSTOM FLOW",
                colorKey = colorKey,
                dayTasks = dayTasks
            )
            navState.update {
                it.copy(statusBannerMessage = "Saved current day as '${title}' blueprint!")
            }
        }
    }

    fun resetSelectedDayProgress() {
        val targetDay = navState.value.selectedDateEpochDay
        viewModelScope.launch {
            repository.resetDayProgress(targetDay)
            navState.update {
                it.copy(statusBannerMessage = "Reset completion for selected day")
            }
        }
    }

    fun clearStatusBanner() {
        navState.update { it.copy(statusBannerMessage = null) }
    }
}
