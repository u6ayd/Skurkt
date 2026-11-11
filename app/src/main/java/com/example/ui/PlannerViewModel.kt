package com.example.ui

import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FolderEntity
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

enum class StruktDestination {
    INBOX,
    FLOW,
    FOLDERS,
    FOCUS
}

enum class FlowFilterMode(val label: String) {
    ALL("All"),
    TIMELESS_ONLY("Timeless"),
    TIMED_ONLY("Timed")
}

fun effectiveTaskStepNumber(task: TaskEntity): Int {
    val raw = task.sortOrder
    return if (raw >= 20 && raw % 10 == 0) {
        (raw / 10).coerceIn(1, 99)
    } else {
        raw.coerceIn(1, 99)
    }
}

@Immutable
data class PlannerUiState(
    val selectedDateEpochDay: Long = LocalDate.now().toEpochDay(),
    val selectedLocalDate: LocalDate = LocalDate.now(),
    val activeDestination: StruktDestination = StruktDestination.FLOW,
    val flowFilter: FlowFilterMode = FlowFilterMode.ALL,
    val searchQuery: String = "",
    val topBubbles: List<TaskWithSubtasks> = emptyList(),
    val spineFlowTasks: List<TaskWithSubtasks> = emptyList(),
    val upNextTask: TaskWithSubtasks? = null,
    val inboxTasks: List<TaskWithSubtasks> = emptyList(),
    val allTasks: List<TaskWithSubtasks> = emptyList(),
    val folders: List<FolderEntity> = emptyList(),
    val selectedFolderId: Long? = null,
    val weekDotColorsByEpochDay: Map<Long, List<String>> = emptyMap(),
    val editingTask: TaskWithSubtasks? = null,
    val isEditorVisible: Boolean = false,
    val editorDefaultMode: TaskMode = TaskMode.TIMELESS_STEP,
    val editorTargetInbox: Boolean = false,
    val inspectingTask: TaskWithSubtasks? = null,
    val schedulingInboxTask: TaskEntity? = null,
    val focusedTaskId: Long? = null,
    val forceSplitPreview: Boolean? = null,
    val statusBannerMessage: String? = null,
    val totalDayTasksCount: Int = 0,
    val completedDayTasksCount: Int = 0,
    val dayReadinessPercent: Int = 0
) {
    val activeFocusTask: TaskWithSubtasks?
        get() = allTasks.firstOrNull { it.task.id == focusedTaskId } ?: upNextTask
}

private data class LocalNavState(
    val selectedDateEpochDay: Long = LocalDate.now().toEpochDay(),
    val activeDestination: StruktDestination = StruktDestination.FLOW,
    val flowFilter: FlowFilterMode = FlowFilterMode.ALL,
    val searchQuery: String = "",
    val selectedFolderId: Long? = null,
    val editingTask: TaskWithSubtasks? = null,
    val isEditorVisible: Boolean = false,
    val editorDefaultMode: TaskMode = TaskMode.TIMELESS_STEP,
    val editorTargetInbox: Boolean = false,
    val inspectingTaskId: Long? = null,
    val schedulingInboxTask: TaskEntity? = null,
    val focusedTaskId: Long? = null,
    val forceSplitPreview: Boolean? = null,
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
        repository.allFoldersFlow,
        navState
    ) { allTasks, folders, nav ->
        val selectedDate = LocalDate.ofEpochDay(nav.selectedDateEpochDay)

        val tasksForSelectedDay = allTasks.filter { item ->
            matchesDate(item.task, selectedDate)
        }

        val bubbles = tasksForSelectedDay
            .filter { it.task.modeEnum == TaskMode.TOP_BUBBLE }
            .sortedBy { it.task.sortOrder }

        val spineCandidates = tasksForSelectedDay
            .filter { it.task.modeEnum != TaskMode.TOP_BUBBLE }

        // 1. Timed tasks sort strictly chronologically by startTimeMinutes (e.g., 08:00 -> 08:15 -> 09:00)
        val timedTasks = spineCandidates
            .filter { it.task.modeEnum == TaskMode.TIMED_BLOCK || it.task.startTimeMinutes != null }
            .sortedWith(
                compareBy<TaskWithSubtasks> { it.task.startTimeMinutes ?: 0 }
                    .thenBy { it.task.id }
            )

        // 2. Timeless tasks sort by their # step number (1..99) and slot into the spine at their # position
        val timelessTasks = spineCandidates
            .filter { it.task.modeEnum != TaskMode.TIMED_BLOCK && it.task.startTimeMinutes == null }
            .sortedWith(
                compareBy<TaskWithSubtasks> { effectiveTaskStepNumber(it.task) }
                    .thenBy { it.task.id }
            )

        val fullSpine = buildList {
            addAll(timedTasks)
            for (timeless in timelessTasks) {
                val desiredStep = effectiveTaskStepNumber(timeless.task)
                val insertIndex = (desiredStep - 1).coerceIn(0, size)
                add(insertIndex, timeless)
            }
        }

        val filteredSpine = when (nav.flowFilter) {
            FlowFilterMode.ALL -> fullSpine
            FlowFilterMode.TIMELESS_ONLY -> fullSpine.filter { it.task.modeEnum == TaskMode.TIMELESS_STEP }
            FlowFilterMode.TIMED_ONLY -> fullSpine.filter { it.task.modeEnum == TaskMode.TIMED_BLOCK }
        }

        val nextUp = fullSpine.firstOrNull { !it.task.isCompleted }
            ?: bubbles.firstOrNull { !it.task.isCompleted }

        val inbox = allTasks
            .filter { it.task.dateEpochDay == null }
            .filter {
                nav.searchQuery.isBlank() ||
                    it.task.title.contains(nav.searchQuery, ignoreCase = true) ||
                    it.task.folderName.contains(nav.searchQuery, ignoreCase = true)
            }

        // O(1) pre-partitioning for fast multi-week dot calculation (avoids 113 x N full scans)
        val scheduledTasks = allTasks.filter { it.task.dateEpochDay != null }
        val nonRecurringByDay = scheduledTasks
            .filter { it.task.recurrenceEnum == RecurrenceRule.NONE }
            .groupBy { it.task.dateEpochDay!! }
        val recurringTasks = scheduledTasks
            .filter { it.task.recurrenceEnum != RecurrenceRule.NONE }

        val startOfCurrentWeek = selectedDate.minusDays((selectedDate.dayOfWeek.value - 1).toLong())
        val weekDots = buildMap<Long, List<String>> {
            for (dayOffset in -56L..56L) {
                val day = startOfCurrentWeek.plusDays(dayOffset)
                val dayEpoch = day.toEpochDay()
                val direct = nonRecurringByDay[dayEpoch].orEmpty()
                val colors = ArrayList<String>(4)
                for (item in direct) {
                    if (colors.size >= 4) break
                    colors.add(item.task.colorKey)
                }
                if (colors.size < 4) {
                    for (rec in recurringTasks) {
                        if (colors.size >= 4) break
                        if (rec.task.dateEpochDay != dayEpoch && matchesDate(rec.task, day)) {
                            colors.add(rec.task.colorKey)
                        }
                    }
                }
                if (colors.isNotEmpty()) {
                    put(dayEpoch, colors)
                }
            }
        }

        val inspected = nav.inspectingTaskId?.let { id ->
            allTasks.firstOrNull { it.task.id == id }
        }

        val totalDayCount = bubbles.size + filteredSpine.size
        val completedDayCount = bubbles.count { it.task.isCompleted } + filteredSpine.count { it.task.isCompleted }
        val readinessPct = if (totalDayCount == 0) 0 else ((completedDayCount * 100f) / totalDayCount).toInt()

        PlannerUiState(
            selectedDateEpochDay = nav.selectedDateEpochDay,
            selectedLocalDate = selectedDate,
            activeDestination = nav.activeDestination,
            flowFilter = nav.flowFilter,
            searchQuery = nav.searchQuery,
            topBubbles = bubbles,
            spineFlowTasks = filteredSpine,
            upNextTask = nextUp,
            inboxTasks = inbox,
            allTasks = allTasks,
            folders = folders,
            selectedFolderId = nav.selectedFolderId,
            weekDotColorsByEpochDay = weekDots,
            editingTask = nav.editingTask,
            isEditorVisible = nav.isEditorVisible,
            editorDefaultMode = nav.editorDefaultMode,
            editorTargetInbox = nav.editorTargetInbox,
            inspectingTask = inspected,
            schedulingInboxTask = nav.schedulingInboxTask,
            focusedTaskId = nav.focusedTaskId,
            forceSplitPreview = nav.forceSplitPreview,
            statusBannerMessage = nav.statusBannerMessage,
            totalDayTasksCount = totalDayCount,
            completedDayTasksCount = completedDayCount,
            dayReadinessPercent = readinessPct
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlannerUiState()
    )

    private fun matchesDate(task: TaskEntity, targetDate: LocalDate): Boolean {
        val taskEpoch = task.dateEpochDay ?: return false
        if (taskEpoch == targetDate.toEpochDay()) return true
        val originDate = LocalDate.ofEpochDay(taskEpoch)
        return when (task.recurrenceEnum) {
            RecurrenceRule.NONE -> false
            RecurrenceRule.DAILY -> true
            RecurrenceRule.WEEKDAYS ->
                targetDate.dayOfWeek != DayOfWeek.SATURDAY && targetDate.dayOfWeek != DayOfWeek.SUNDAY
            RecurrenceRule.WEEKLY ->
                targetDate.dayOfWeek == originDate.dayOfWeek
            RecurrenceRule.MONTHLY ->
                targetDate.dayOfMonth == task.recurrenceDayOfMonth.coerceIn(1, targetDate.lengthOfMonth())
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
        navState.update { it.copy(activeDestination = destination) }
    }

    fun setFlowFilter(filter: FlowFilterMode) {
        navState.update { it.copy(flowFilter = filter) }
    }

    fun setSearchQuery(query: String) {
        navState.update { it.copy(searchQuery = query) }
    }

    fun selectFolder(folderId: Long?) {
        navState.update { it.copy(selectedFolderId = folderId) }
    }

    fun toggleSplitViewMode(currentlySplit: Boolean) {
        navState.update { it.copy(forceSplitPreview = !currentlySplit) }
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
                editorTargetInbox = forInbox,
                inspectingTaskId = null
            )
        }
    }

    fun openEditTaskSheet(taskWithSubtasks: TaskWithSubtasks) {
        navState.update {
            it.copy(
                editingTask = taskWithSubtasks,
                isEditorVisible = true,
                editorDefaultMode = taskWithSubtasks.task.modeEnum,
                editorTargetInbox = taskWithSubtasks.task.dateEpochDay == null,
                inspectingTaskId = null
            )
        }
    }

    fun closeTaskSheet() {
        navState.update { it.copy(isEditorVisible = false, editingTask = null) }
    }

    fun openInspectTaskModal(taskId: Long) {
        navState.update { it.copy(inspectingTaskId = taskId) }
    }

    fun closeInspectTaskModal() {
        navState.update { it.copy(inspectingTaskId = null) }
    }

    fun openScheduleInboxPopup(task: TaskEntity) {
        navState.update { it.copy(schedulingInboxTask = task) }
    }

    fun closeScheduleInboxPopup() {
        navState.update { it.copy(schedulingInboxTask = null) }
    }

    fun confirmScheduleInboxTask(task: TaskEntity, asTimed: Boolean, startTimeMinutes: Int?) {
        val targetDay = navState.value.selectedDateEpochDay
        viewModelScope.launch {
            repository.scheduleInboxTaskToDay(task, targetDay, asTimed, startTimeMinutes)
            navState.update {
                it.copy(
                    schedulingInboxTask = null,
                    statusBannerMessage = "Scheduled into Flow"
                )
            }
        }
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
        folderName: String,
        sectionName: String,
        recurrence: RecurrenceRule,
        recurrenceDayOfMonth: Int,
        subtasks: List<Pair<String, String>>,
        sortOrder: Int
    ) {
        if (title.isBlank()) return
        val currentEpoch = navState.value.selectedDateEpochDay
        val targetEpoch = if (toInbox) null else currentEpoch
        val prevTask = navState.value.editingTask?.task
        val entity = TaskEntity(
            id = existingId,
            title = title.trim(),
            notes = notes.trim(),
            dateEpochDay = targetEpoch,
            taskMode = taskMode.name,
            startTimeMinutes = if (taskMode == TaskMode.TIMED_BLOCK) (startTimeMinutes ?: 9 * 60) else prevTask?.startTimeMinutes,
            durationMinutes = durationMinutes.coerceIn(1, 720),
            sortOrder = sortOrder,
            colorKey = colorKey,
            iconKey = iconKey,
            folderName = folderName.ifBlank { "General" },
            sectionName = sectionName.ifBlank { "Core" },
            recurrence = recurrence.name,
            recurrenceDayOfMonth = recurrenceDayOfMonth.coerceIn(1, 31),
            isCompleted = prevTask?.isCompleted ?: false,
            sessionSegments = prevTask?.sessionSegments.orEmpty(),
            activeSessionStartMin = prevTask?.activeSessionStartMin
        )
        viewModelScope.launch {
            repository.saveTaskWithSubtasks(entity, subtasks)
            navState.update {
                it.copy(
                    isEditorVisible = false,
                    editingTask = null,
                    statusBannerMessage = if (toInbox) "Saved to Inbox" else "Saved to Flow"
                )
            }
        }
    }

    fun quickAddInboxTask(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.saveTaskWithSubtasks(
                task = TaskEntity(
                    title = title.trim(),
                    dateEpochDay = null,
                    taskMode = TaskMode.TIMELESS_STEP.name,
                    durationMinutes = 30,
                    colorKey = "SKY",
                    iconKey = "BOLT",
                    folderName = "General",
                    sectionName = "Inbox"
                ),
                subtaskTitles = emptyList()
            )
        }
    }

    fun sendTaskBackToInbox(task: TaskEntity) {
        viewModelScope.launch {
            repository.moveTaskToInbox(task)
            navState.update {
                it.copy(
                    isEditorVisible = false,
                    editingTask = null,
                    inspectingTaskId = null,
                    statusBannerMessage = "Moved to Inbox"
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

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
            navState.update {
                it.copy(
                    isEditorVisible = false,
                    editingTask = null,
                    inspectingTaskId = null,
                    statusBannerMessage = "Removed"
                )
            }
        }
    }

    fun enterFocusForTask(taskId: Long?) {
        navState.update {
            it.copy(
                focusedTaskId = taskId,
                inspectingTaskId = null,
                activeDestination = StruktDestination.FOCUS
            )
        }
    }

    fun startOrResumeFocusTimer(task: TaskEntity) {
        viewModelScope.launch {
            repository.startOrResumeTaskSession(task)
        }
    }

    fun pauseFocusTimer(task: TaskEntity) {
        viewModelScope.launch {
            repository.pauseTaskSession(task)
            navState.update {
                it.copy(statusBannerMessage = "Paused • Break loop noted on timeline")
            }
        }
    }

    fun toggleTaskBreakLoop(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleTaskBreakLoop(task)
            val hasBreak = task.parsedSessionSegments.size > 1 ||
                (task.parsedSessionSegments.size == 1 && task.activeSessionStartMin != null)
            navState.update {
                it.copy(
                    statusBannerMessage = if (hasBreak) "Break loop cleared" else "Break loop added to capsule"
                )
            }
        }
    }

    fun updateTaskStepNumber(task: TaskEntity, newStepNumber: Int) {
        val clamped = newStepNumber.coerceIn(1, 99)
        viewModelScope.launch {
            repository.updateTaskStepOrder(task, clamped)
            navState.update {
                it.copy(statusBannerMessage = "Step set to #$clamped")
            }
        }
    }

    fun completeFocusedTaskAndAdvance() {
        val currentFocus = uiState.value.activeFocusTask ?: return
        viewModelScope.launch {
            repository.finishTaskSessionAndComplete(currentFocus.task)
            navState.update {
                it.copy(
                    focusedTaskId = null,
                    statusBannerMessage = "Completed • Advancing to next"
                )
            }
        }
    }

    fun createNewFolder(name: String, codeTag: String, colorKey: String, iconKey: String, sections: List<String>) {
        viewModelScope.launch {
            repository.createFolder(name, codeTag, colorKey, iconKey, sections)
            navState.update {
                it.copy(statusBannerMessage = "Folder created")
            }
        }
    }

    fun addSectionToFolder(folder: FolderEntity, sectionName: String) {
        viewModelScope.launch {
            repository.addSectionToFolder(folder, sectionName)
        }
    }

    fun clearStatusBanner() {
        navState.update { it.copy(statusBannerMessage = null) }
    }
}
