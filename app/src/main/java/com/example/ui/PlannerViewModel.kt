package com.example.ui

import android.app.Application
import android.content.Context
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
import com.example.ui.components.GlobalClockFormat
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
data class PendingOverlapConflict(
    val newTaskTitle: String,
    val conflictingTask: TaskEntity,
    val requestedStartMin: Int,
    val requestedDurationMin: Int,
    val suggestedAutoShiftStartMin: Int,
    val pendingEditorEntity: TaskEntity? = null,
    val pendingEditorSubtasks: List<Pair<String, String>> = emptyList(),
    val pendingInboxTask: TaskEntity? = null
)

@Immutable
data class PlannerUiState(
    val selectedDateEpochDay: Long = LocalDate.now().toEpochDay(),
    val selectedLocalDate: LocalDate = LocalDate.now(),
    val activeDestination: StruktDestination = StruktDestination.FLOW,
    val flowFilter: FlowFilterMode = FlowFilterMode.ALL,
    val searchQuery: String = "",
    val topBubbles: List<TaskWithSubtasks> = emptyList(),
    val spineFlowTasks: List<TaskWithSubtasks> = emptyList(),
    val simultaneousTaskIds: Set<Long> = emptySet(),
    val freeGapMinutesBeforeIndex: Map<Int, Int> = emptyMap(),
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
    val editorPreselectedFolder: String? = null,
    val editorPreselectedSection: String? = null,
    val inspectingTask: TaskWithSubtasks? = null,
    val schedulingInboxTask: TaskEntity? = null,
    val pendingOverlapConflict: PendingOverlapConflict? = null,
    val focusedTaskId: Long? = null,
    val forceSplitPreview: Boolean? = null,
    val statusBannerMessage: String? = null,
    val totalDayTasksCount: Int = 0,
    val completedDayTasksCount: Int = 0,
    val dayReadinessPercent: Int = 0,
    val isSettingsVisible: Boolean = false,
    val isCleanupExportVisible: Boolean = false,
    val isMultiSelectActive: Boolean = false,
    val selectedTaskIds: Set<Long> = emptySet(),
    val use24HourFormat: Boolean = true,
    val startOfDayMinutes: Int = 8 * 60,
    val endOfDayMinutes: Int = 22 * 60
) {
    val activeFocusTask: TaskWithSubtasks?
        get() = allTasks.firstOrNull {
            it.task.id == focusedTaskId &&
                !it.task.isCompleted &&
                it.task.dateEpochDay != null &&
                selectedDateEpochDay !in it.task.excludedEpochDays
        } ?: upNextTask
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
    val editorPreselectedFolder: String? = null,
    val editorPreselectedSection: String? = null,
    val inspectingTaskId: Long? = null,
    val schedulingInboxTask: TaskEntity? = null,
    val pendingOverlapConflict: PendingOverlapConflict? = null,
    val focusedTaskId: Long? = null,
    val forceSplitPreview: Boolean? = null,
    val statusBannerMessage: String? = null,
    val isSettingsVisible: Boolean = false,
    val isCleanupExportVisible: Boolean = false,
    val isMultiSelectActive: Boolean = false,
    val selectedTaskIds: Set<Long> = emptySet(),
    val use24HourFormat: Boolean = true,
    val startOfDayMinutes: Int = 8 * 60,
    val endOfDayMinutes: Int = 22 * 60
)

class PlannerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PlannerRepository = PlannerRepository(
        PlannerDatabase.getInstance(application).plannerDao()
    )

    private val prefs = application.getSharedPreferences("strukt_settings_prefs", Context.MODE_PRIVATE)

    private val navState = MutableStateFlow(
        LocalNavState(
            use24HourFormat = prefs.getBoolean("use_24_hour", true),
            startOfDayMinutes = prefs.getInt("start_of_day_min", 8 * 60),
            endOfDayMinutes = prefs.getInt("end_of_day_min", 22 * 60)
        )
    )

    init {
        GlobalClockFormat.use24Hour = navState.value.use24HourFormat
        GlobalClockFormat.startOfDayMinutes = navState.value.startOfDayMinutes
        GlobalClockFormat.endOfDayMinutes = navState.value.endOfDayMinutes
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

        // 1. Timed tasks sort strictly chronologically by startTimeMinutes (e.g., 08:00 -> 08:15 -> 14:00)
        val timedTasks = spineCandidates
            .filter { it.task.modeEnum == TaskMode.TIMED_BLOCK && it.task.startTimeMinutes != null }
            .sortedWith(
                compareBy<TaskWithSubtasks> { it.task.startTimeMinutes ?: 0 }
                    .thenBy { effectiveTaskStepNumber(it.task) }
                    .thenBy { it.task.id }
            )

        // 2. Timeless tasks sort by their normalized # step number (1..N) and slot into the spine at their # position
        val timelessTasks = spineCandidates
            .filter { it.task.modeEnum != TaskMode.TIMED_BLOCK || it.task.startTimeMinutes == null }
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

        // Identify overlapping Timed tasks on the selected day so both display a "Simultaneous" badge
        val simultaneousIds = buildSet<Long> {
            for (i in timedTasks.indices) {
                val a = timedTasks[i].task
                val aStart = a.startTimeMinutes ?: continue
                val aEnd = aStart + a.durationMinutes.coerceAtLeast(5)
                for (j in i + 1 until timedTasks.size) {
                    val b = timedTasks[j].task
                    val bStart = b.startTimeMinutes ?: continue
                    val bEnd = bStart + b.durationMinutes.coerceAtLeast(5)
                    if (aStart < bEnd && bStart < aEnd) {
                        add(a.id)
                        add(b.id)
                    }
                }
            }
        }

        // Compute unscheduled free-time intervals ("3h free", "45m free") between consecutive Timed tasks in filteredSpine
        val freeGapsByIndex = buildMap<Int, Int> {
            var prevTimedIndex: Int? = null
            var prevTimedEndMin: Int? = null
            for (index in filteredSpine.indices) {
                val task = filteredSpine[index].task
                if (task.modeEnum == TaskMode.TIMED_BLOCK && task.startTimeMinutes != null) {
                    val currStart = task.startTimeMinutes
                    val lastEnd = prevTimedEndMin
                    val lastIdx = prevTimedIndex
                    if (lastEnd != null && lastIdx != null && currStart > lastEnd) {
                        // Place the free-time label on the connector immediately after the earlier Timed task
                        put(lastIdx + 1, currStart - lastEnd)
                    }
                    val currEnd = currStart + task.durationMinutes.coerceAtLeast(5)
                    prevTimedEndMin = maxOf(prevTimedEndMin ?: 0, currEnd)
                    prevTimedIndex = index
                }
            }
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
                    if (dayEpoch !in item.task.excludedEpochDays) {
                        colors.add(item.task.colorKey)
                    }
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
            simultaneousTaskIds = simultaneousIds,
            freeGapMinutesBeforeIndex = freeGapsByIndex,
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
            editorPreselectedFolder = nav.editorPreselectedFolder,
            editorPreselectedSection = nav.editorPreselectedSection,
            inspectingTask = inspected,
            schedulingInboxTask = nav.schedulingInboxTask,
            pendingOverlapConflict = nav.pendingOverlapConflict,
            focusedTaskId = nav.focusedTaskId,
            forceSplitPreview = nav.forceSplitPreview,
            statusBannerMessage = nav.statusBannerMessage,
            totalDayTasksCount = totalDayCount,
            completedDayTasksCount = completedDayCount,
            dayReadinessPercent = readinessPct,
            isSettingsVisible = nav.isSettingsVisible,
            isCleanupExportVisible = nav.isCleanupExportVisible,
            isMultiSelectActive = nav.isMultiSelectActive,
            selectedTaskIds = nav.selectedTaskIds,
            use24HourFormat = nav.use24HourFormat,
            startOfDayMinutes = nav.startOfDayMinutes,
            endOfDayMinutes = nav.endOfDayMinutes
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlannerUiState()
    )

    private fun matchesDate(task: TaskEntity, targetDate: LocalDate): Boolean {
        val taskEpoch = task.dateEpochDay ?: return false
        val targetEpoch = targetDate.toEpochDay()
        if (targetEpoch in task.excludedEpochDays) return false
        if (taskEpoch == targetEpoch) return true
        val originDate = LocalDate.ofEpochDay(taskEpoch)
        return when (task.recurrenceEnum) {
            RecurrenceRule.NONE -> false
            RecurrenceRule.DAILY -> targetEpoch >= taskEpoch
            RecurrenceRule.WEEKDAYS ->
                targetEpoch >= taskEpoch &&
                    targetDate.dayOfWeek != DayOfWeek.SATURDAY &&
                    targetDate.dayOfWeek != DayOfWeek.SUNDAY
            RecurrenceRule.WEEKLY ->
                targetEpoch >= taskEpoch && targetDate.dayOfWeek == originDate.dayOfWeek
            RecurrenceRule.MONTHLY ->
                targetDate.dayOfMonth == task.recurrenceDayOfMonth.coerceIn(1, targetDate.lengthOfMonth())
            RecurrenceRule.CUSTOM_DAYS -> {
                val interval = task.recurrenceIntervalDays.coerceAtLeast(1).toLong()
                val diff = targetEpoch - taskEpoch
                diff >= 0L && (diff % interval == 0L)
            }
        }
    }

    private fun findOverlapConflictOnDay(
        targetEpochDay: Long,
        excludeTaskId: Long,
        requestedStartMin: Int,
        requestedDurationMin: Int
    ): Pair<TaskEntity, Int>? {
        val targetDate = LocalDate.ofEpochDay(targetEpochDay)
        val dayTimedTasks = uiState.value.allTasks
            .map { it.task }
            .filter {
                it.id != excludeTaskId &&
                    it.modeEnum == TaskMode.TIMED_BLOCK &&
                    it.startTimeMinutes != null &&
                    matchesDate(it, targetDate)
            }
            .sortedBy { it.startTimeMinutes ?: 0 }

        val reqEnd = requestedStartMin + requestedDurationMin.coerceAtLeast(5)
        val firstConflict = dayTimedTasks.firstOrNull { existing ->
            val exStart = existing.startTimeMinutes ?: return@firstOrNull false
            val exEnd = exStart + existing.durationMinutes.coerceAtLeast(5)
            requestedStartMin < exEnd && exStart < reqEnd
        } ?: return null

        var candidateStart = (firstConflict.startTimeMinutes ?: requestedStartMin) +
            firstConflict.durationMinutes.coerceAtLeast(5)
        for (existing in dayTimedTasks) {
            val exStart = existing.startTimeMinutes ?: continue
            val exEnd = exStart + existing.durationMinutes.coerceAtLeast(5)
            val candEnd = candidateStart + requestedDurationMin.coerceAtLeast(5)
            if (candidateStart < exEnd && exStart < candEnd) {
                candidateStart = maxOf(candidateStart, exEnd)
            }
        }
        val safeSuggestedStart = candidateStart.coerceIn(0, 23 * 60 + 50)
        return firstConflict to safeSuggestedStart
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
        navState.update {
            it.copy(
                activeDestination = destination,
                isMultiSelectActive = false,
                selectedTaskIds = emptySet()
            )
        }
    }

    fun setFlowFilter(filter: FlowFilterMode) {
        navState.update { it.copy(flowFilter = filter) }
    }

    fun setSearchQuery(query: String) {
        navState.update { it.copy(searchQuery = query) }
    }

    fun selectFolder(folderId: Long?) {
        navState.update {
            it.copy(
                selectedFolderId = folderId,
                isMultiSelectActive = false,
                selectedTaskIds = emptySet()
            )
        }
    }

    fun toggleSplitViewMode(currentlySplit: Boolean) {
        navState.update { it.copy(forceSplitPreview = !currentlySplit) }
    }

    fun openCreateTaskSheet(
        defaultMode: TaskMode = TaskMode.TIMELESS_STEP,
        forInbox: Boolean = false,
        preselectedFolder: String? = null,
        preselectedSection: String? = null
    ) {
        navState.update {
            it.copy(
                editingTask = null,
                isEditorVisible = true,
                editorDefaultMode = defaultMode,
                editorTargetInbox = forInbox,
                editorPreselectedFolder = preselectedFolder,
                editorPreselectedSection = preselectedSection,
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
                editorPreselectedFolder = null,
                editorPreselectedSection = null,
                inspectingTaskId = null
            )
        }
    }

    fun closeTaskSheet() {
        navState.update {
            it.copy(
                isEditorVisible = false,
                editingTask = null,
                editorPreselectedFolder = null,
                editorPreselectedSection = null
            )
        }
    }

    fun openInspectTaskModal(taskId: Long) {
        if (navState.value.isMultiSelectActive) {
            toggleTaskSelection(taskId)
            return
        }
        navState.update { it.copy(inspectingTaskId = taskId) }
    }

    fun closeInspectTaskModal() {
        navState.update { it.copy(inspectingTaskId = null) }
    }

    fun openSettingsModal() {
        navState.update { it.copy(isSettingsVisible = true) }
    }

    fun closeSettingsModal() {
        navState.update { it.copy(isSettingsVisible = false) }
    }

    fun openCleanupExportModal() {
        navState.update {
            it.copy(
                isSettingsVisible = false,
                isCleanupExportVisible = true
            )
        }
    }

    fun closeCleanupExportModal() {
        navState.update { it.copy(isCleanupExportVisible = false) }
    }

    fun showStatusBanner(message: String) {
        navState.update { it.copy(statusBannerMessage = message) }
    }

    fun toggleMultiSelectMode() {
        navState.update {
            val next = !it.isMultiSelectActive
            it.copy(
                isMultiSelectActive = next,
                selectedTaskIds = if (next) it.selectedTaskIds else emptySet()
            )
        }
    }

    fun enterMultiSelectWithTask(taskId: Long) {
        navState.update {
            it.copy(
                isMultiSelectActive = true,
                selectedTaskIds = it.selectedTaskIds + taskId,
                inspectingTaskId = null
            )
        }
    }

    fun toggleTaskSelection(taskId: Long) {
        navState.update { state ->
            val updated = if (taskId in state.selectedTaskIds) {
                state.selectedTaskIds - taskId
            } else {
                state.selectedTaskIds + taskId
            }
            state.copy(
                isMultiSelectActive = true,
                selectedTaskIds = updated
            )
        }
    }

    fun selectOrDeselectAllTasks(visibleTaskIds: List<Long>) {
        if (visibleTaskIds.isEmpty()) return
        navState.update { state ->
            val allSelected = visibleTaskIds.all { it in state.selectedTaskIds }
            val nextSelected = if (allSelected) {
                state.selectedTaskIds - visibleTaskIds.toSet()
            } else {
                state.selectedTaskIds + visibleTaskIds
            }
            state.copy(
                isMultiSelectActive = true,
                selectedTaskIds = nextSelected
            )
        }
    }

    fun exitMultiSelectMode() {
        navState.update {
            it.copy(
                isMultiSelectActive = false,
                selectedTaskIds = emptySet()
            )
        }
    }

    fun bulkDeleteSelectedTasks() {
        val idsToDelete = navState.value.selectedTaskIds.toList()
        if (idsToDelete.isEmpty()) return
        viewModelScope.launch {
            repository.deleteTasksByIds(idsToDelete)
            navState.update {
                it.copy(
                    isMultiSelectActive = false,
                    selectedTaskIds = emptySet(),
                    focusedTaskId = if (it.focusedTaskId in idsToDelete) null else it.focusedTaskId,
                    inspectingTaskId = if (it.inspectingTaskId in idsToDelete) null else it.inspectingTaskId,
                    statusBannerMessage = "Deleted ${idsToDelete.size} task(s)"
                )
            }
        }
    }

    fun purgeCompletedTasks(taskIds: List<Long>) {
        if (taskIds.isEmpty()) return
        viewModelScope.launch {
            repository.deleteTasksByIds(taskIds)
            navState.update {
                it.copy(
                    focusedTaskId = if (it.focusedTaskId in taskIds) null else it.focusedTaskId,
                    inspectingTaskId = if (it.inspectingTaskId in taskIds) null else it.inspectingTaskId,
                    selectedTaskIds = it.selectedTaskIds - taskIds.toSet(),
                    statusBannerMessage = "Cleaned up ${taskIds.size} completed task(s)"
                )
            }
        }
    }

    fun updateGlobalSettings(
        use24Hour: Boolean,
        startOfDayMin: Int,
        endOfDayMin: Int
    ) {
        val safeStart = startOfDayMin.coerceIn(0, 22 * 60)
        val safeEnd = endOfDayMin.coerceIn(safeStart + 60, 23 * 60 + 59)
        prefs.edit()
            .putBoolean("use_24_hour", use24Hour)
            .putInt("start_of_day_min", safeStart)
            .putInt("end_of_day_min", safeEnd)
            .apply()
        GlobalClockFormat.use24Hour = use24Hour
        GlobalClockFormat.startOfDayMinutes = safeStart
        GlobalClockFormat.endOfDayMinutes = safeEnd
        navState.update {
            it.copy(
                use24HourFormat = use24Hour,
                startOfDayMinutes = safeStart,
                endOfDayMinutes = safeEnd,
                isSettingsVisible = false,
                statusBannerMessage = "Settings saved"
            )
        }
    }

    fun openScheduleInboxPopup(task: TaskEntity) {
        navState.update { it.copy(schedulingInboxTask = task) }
    }

    fun closeScheduleInboxPopup() {
        navState.update { it.copy(schedulingInboxTask = null) }
    }

    fun confirmScheduleInboxTask(
        task: TaskEntity,
        asTimed: Boolean,
        startTimeMinutes: Int?,
        durationMinutes: Int = task.durationMinutes
    ) {
        val targetDay = navState.value.selectedDateEpochDay
        val safeDuration = if (asTimed) durationMinutes.coerceIn(5, 720) else durationMinutes.coerceIn(0, 720)
        if (asTimed && startTimeMinutes != null) {
            val conflictPair = findOverlapConflictOnDay(
                targetEpochDay = targetDay,
                excludeTaskId = task.id,
                requestedStartMin = startTimeMinutes,
                requestedDurationMin = safeDuration
            )
            if (conflictPair != null) {
                val (conflictingTask, suggestedStart) = conflictPair
                navState.update {
                    it.copy(
                        schedulingInboxTask = null,
                        pendingOverlapConflict = PendingOverlapConflict(
                            newTaskTitle = task.title,
                            conflictingTask = conflictingTask,
                            requestedStartMin = startTimeMinutes,
                            requestedDurationMin = safeDuration,
                            suggestedAutoShiftStartMin = suggestedStart,
                            pendingInboxTask = task
                        )
                    )
                }
                return
            }
        }
        viewModelScope.launch {
            repository.scheduleInboxTaskToDay(
                task = task,
                targetEpochDay = targetDay,
                asTimed = asTimed,
                startTimeMinutes = startTimeMinutes,
                durationMinutes = safeDuration,
                allowSimultaneous = false
            )
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
        recurrenceIntervalDays: Int = 2,
        subtasks: List<Pair<String, String>>,
        sortOrder: Int
    ) {
        if (title.isBlank()) return
        val currentEpoch = navState.value.selectedDateEpochDay
        val targetEpoch = if (toInbox) null else currentEpoch
        val prevTask = navState.value.editingTask?.task
        val safeDuration = if (taskMode == TaskMode.TIMED_BLOCK) {
            durationMinutes.coerceIn(5, 720)
        } else {
            durationMinutes.coerceIn(0, 720)
        }
        val resolvedStartMin = if (taskMode == TaskMode.TIMED_BLOCK) (startTimeMinutes ?: 9 * 60) else null
        val entity = TaskEntity(
            id = existingId,
            title = title.trim(),
            notes = notes.trim(),
            dateEpochDay = targetEpoch,
            taskMode = taskMode.name,
            startTimeMinutes = resolvedStartMin,
            durationMinutes = safeDuration,
            sortOrder = sortOrder,
            colorKey = colorKey,
            iconKey = iconKey,
            folderName = folderName.ifBlank { "General" },
            sectionName = sectionName.ifBlank { "Core" },
            recurrence = recurrence.name,
            recurrenceDayOfMonth = recurrenceDayOfMonth.coerceIn(1, 31),
            recurrenceIntervalDays = recurrenceIntervalDays.coerceIn(2, 365),
            excludedEpochDaysCsv = prevTask?.excludedEpochDaysCsv.orEmpty(),
            allowSimultaneous = prevTask?.allowSimultaneous ?: false,
            isCompleted = prevTask?.isCompleted ?: false,
            sessionSegments = prevTask?.sessionSegments.orEmpty(),
            activeSessionStartMin = prevTask?.activeSessionStartMin
        )

        if (!toInbox && targetEpoch != null && taskMode == TaskMode.TIMED_BLOCK && resolvedStartMin != null) {
            val conflictPair = findOverlapConflictOnDay(
                targetEpochDay = targetEpoch,
                excludeTaskId = existingId,
                requestedStartMin = resolvedStartMin,
                requestedDurationMin = safeDuration
            )
            if (conflictPair != null) {
                val (conflictingTask, suggestedStart) = conflictPair
                navState.update {
                    it.copy(
                        isEditorVisible = false,
                        editingTask = null,
                        pendingOverlapConflict = PendingOverlapConflict(
                            newTaskTitle = entity.title,
                            conflictingTask = conflictingTask,
                            requestedStartMin = resolvedStartMin,
                            requestedDurationMin = safeDuration,
                            suggestedAutoShiftStartMin = suggestedStart,
                            pendingEditorEntity = entity,
                            pendingEditorSubtasks = subtasks
                        )
                    )
                }
                return
            }
        }

        viewModelScope.launch {
            repository.saveTaskWithSubtasks(entity, subtasks)
            navState.update {
                it.copy(
                    isEditorVisible = false,
                    editingTask = null,
                    editorPreselectedFolder = null,
                    editorPreselectedSection = null,
                    statusBannerMessage = if (toInbox) "Saved to Inbox" else "Saved to Flow"
                )
            }
        }
    }

    fun resolveOverlapAutoShift() {
        val conflict = navState.value.pendingOverlapConflict ?: return
        val targetDay = navState.value.selectedDateEpochDay
        viewModelScope.launch {
            if (conflict.pendingEditorEntity != null) {
                repository.saveTaskWithSubtasks(
                    task = conflict.pendingEditorEntity.copy(
                        startTimeMinutes = conflict.suggestedAutoShiftStartMin,
                        allowSimultaneous = false
                    ),
                    subtaskTitles = conflict.pendingEditorSubtasks
                )
            } else if (conflict.pendingInboxTask != null) {
                repository.scheduleInboxTaskToDay(
                    task = conflict.pendingInboxTask,
                    targetEpochDay = targetDay,
                    asTimed = true,
                    startTimeMinutes = conflict.suggestedAutoShiftStartMin,
                    durationMinutes = conflict.requestedDurationMin,
                    allowSimultaneous = false
                )
            }
            navState.update {
                it.copy(
                    pendingOverlapConflict = null,
                    statusBannerMessage = "Auto-shifted to ${com.example.ui.components.formatMinutesAsClock(conflict.suggestedAutoShiftStartMin)}"
                )
            }
        }
    }

    fun resolveOverlapAllowSimultaneous() {
        val conflict = navState.value.pendingOverlapConflict ?: return
        val targetDay = navState.value.selectedDateEpochDay
        viewModelScope.launch {
            repository.markTaskSimultaneous(conflict.conflictingTask)
            if (conflict.pendingEditorEntity != null) {
                repository.saveTaskWithSubtasks(
                    task = conflict.pendingEditorEntity.copy(
                        startTimeMinutes = conflict.requestedStartMin,
                        allowSimultaneous = true
                    ),
                    subtaskTitles = conflict.pendingEditorSubtasks
                )
            } else if (conflict.pendingInboxTask != null) {
                repository.scheduleInboxTaskToDay(
                    task = conflict.pendingInboxTask,
                    targetEpochDay = targetDay,
                    asTimed = true,
                    startTimeMinutes = conflict.requestedStartMin,
                    durationMinutes = conflict.requestedDurationMin,
                    allowSimultaneous = true
                )
            }
            navState.update {
                it.copy(
                    pendingOverlapConflict = null,
                    statusBannerMessage = "Scheduled as Simultaneous"
                )
            }
        }
    }

    fun dismissOverlapConflict() {
        navState.update { it.copy(pendingOverlapConflict = null) }
    }

    fun quickAddInboxTask(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.saveTaskWithSubtasks(
                task = TaskEntity(
                    title = title.trim(),
                    dateEpochDay = null,
                    taskMode = TaskMode.TIMELESS_STEP.name,
                    durationMinutes = 0,
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
                    focusedTaskId = if (it.focusedTaskId == task.id) null else it.focusedTaskId,
                    statusBannerMessage = "Moved to Inbox"
                )
            }
        }
    }

    fun toggleTaskDone(task: TaskEntity) {
        val todayEpoch = LocalDate.now().toEpochDay()
        viewModelScope.launch {
            val pulledFromFuture = repository.toggleTaskCompleted(task, todayEpoch)
            navState.update {
                it.copy(
                    focusedTaskId = if (it.focusedTaskId == task.id) null else it.focusedTaskId,
                    statusBannerMessage = if (pulledFromFuture) {
                        "Completed & pulled into Today's Flow"
                    } else {
                        it.statusBannerMessage
                    }
                )
            }
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
                    focusedTaskId = if (it.focusedTaskId == taskId) null else it.focusedTaskId,
                    statusBannerMessage = "Removed"
                )
            }
        }
    }

    fun deleteTaskTodayOnly(task: TaskEntity) {
        val currentEpoch = navState.value.selectedDateEpochDay
        viewModelScope.launch {
            repository.excludeRecurringTaskOnDay(task, currentEpoch)
            navState.update {
                it.copy(
                    isEditorVisible = false,
                    editingTask = null,
                    inspectingTaskId = null,
                    focusedTaskId = if (it.focusedTaskId == task.id) null else it.focusedTaskId,
                    statusBannerMessage = "Removed for today only"
                )
            }
        }
    }

    fun enterFocusForTask(taskId: Long?) {
        val currentUpNext = uiState.value.upNextTask?.task
        val lockedTaskId = currentUpNext?.id ?: taskId
        val jumpedOutOfOrder = taskId != null && currentUpNext != null && taskId != currentUpNext.id
        navState.update {
            it.copy(
                focusedTaskId = lockedTaskId,
                inspectingTaskId = null,
                activeDestination = StruktDestination.FOCUS,
                statusBannerMessage = if (jumpedOutOfOrder) {
                    "Following flow order • Up Next: ${currentUpNext?.title}"
                } else {
                    it.statusBannerMessage
                }
            )
        }
    }

    fun startOrResumeFocusTimer(task: TaskEntity) {
        val todayEpoch = LocalDate.now().toEpochDay()
        viewModelScope.launch {
            val (mergedMicroPause, pulledFromFuture) = repository.startOrResumeTaskSession(
                task = task,
                todayEpochDay = todayEpoch
            )
            navState.update {
                it.copy(
                    selectedDateEpochDay = if (pulledFromFuture) todayEpoch else it.selectedDateEpochDay,
                    focusedTaskId = task.id,
                    statusBannerMessage = when {
                        pulledFromFuture -> "Pulled into Today's Flow • Timer started"
                        mergedMicroPause -> "Resumed • Micro-pause (<60s) merged"
                        else -> it.statusBannerMessage
                    }
                )
            }
        }
    }

    fun pauseFocusTimer(task: TaskEntity) {
        viewModelScope.launch {
            val mergedShortBurst = repository.pauseTaskSession(task)
            navState.update {
                it.copy(
                    statusBannerMessage = if (mergedShortBurst) {
                        "Paused"
                    } else {
                        "Paused • Break loop noted on timeline"
                    }
                )
            }
        }
    }

    fun extendFocusTaskDuration(task: TaskEntity, deltaMinutes: Int) {
        viewModelScope.launch {
            repository.extendTaskDurationMinutes(task, deltaMinutes)
            navState.update {
                it.copy(statusBannerMessage = "+${deltaMinutes}m added to timer")
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
        val totalSpine = uiState.value.spineFlowTasks.size.coerceAtLeast(1)
        val clamped = newStepNumber.coerceIn(1, totalSpine)
        val currentSpineTasks = uiState.value.spineFlowTasks.map { it.task }
        viewModelScope.launch {
            repository.reorderDaySpineTask(currentSpineTasks, task, clamped)
            navState.update {
                it.copy(statusBannerMessage = "Step moved to #$clamped")
            }
        }
    }

    fun completeFocusedTaskAndAdvance() {
        val currentFocus = uiState.value.activeFocusTask ?: return
        val todayEpoch = LocalDate.now().toEpochDay()
        viewModelScope.launch {
            val pulledFromFuture = repository.finishTaskSessionAndComplete(
                task = currentFocus.task,
                todayEpochDay = todayEpoch
            )
            navState.update {
                it.copy(
                    selectedDateEpochDay = if (pulledFromFuture) todayEpoch else it.selectedDateEpochDay,
                    focusedTaskId = null,
                    statusBannerMessage = if (pulledFromFuture) {
                        "Completed & pulled into Today's Flow"
                    } else {
                        "Completed • Advancing to next"
                    }
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
