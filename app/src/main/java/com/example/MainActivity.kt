package com.example

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.ViewTimeline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.TaskMode
import com.example.ui.PlannerUiState
import com.example.ui.PlannerViewModel
import com.example.ui.StruktDestination
import com.example.ui.components.AntiParalysisFocusScreen
import com.example.ui.components.DateAndWeekHeader
import com.example.ui.components.DotGridBackground
import com.example.ui.components.FlowStructureFilterBar
import com.example.ui.components.FoldersAndSectionsScreen
import com.example.ui.components.FreeTimeGapIndicator
import com.example.ui.components.HybridSpineTaskRow
import com.example.ui.components.NeoCard
import com.example.ui.components.NeoDashedDivider
import com.example.ui.components.NeoInboxPane
import com.example.ui.components.NeoPillButton
import com.example.ui.components.ScheduleInboxMiniPopup
import com.example.ui.components.StructuredTaskEditorDialog
import com.example.ui.components.TaskDetailPopupModal
import com.example.ui.components.TopAnytimeBubblesRow
import com.example.ui.theme.InkBlack
import com.example.ui.theme.InkMuted
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeoCoral
import com.example.ui.theme.NeoMintGreen
import com.example.ui.theme.NeoSkyBlue
import com.example.ui.theme.NeoYellow
import com.example.ui.theme.PureWhiteSurface
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val plannerViewModel: PlannerViewModel = viewModel()
                val uiState by plannerViewModel.uiState.collectAsStateWithLifecycle()
                StruktPlannerApp(
                    uiState = uiState,
                    viewModel = plannerViewModel
                )
            }
        }
    }
}

@Composable
fun StruktPlannerApp(
    uiState: PlannerUiState,
    viewModel: PlannerViewModel
) {
    val context = LocalContext.current

    BackHandler(
        enabled = uiState.isEditorVisible ||
            uiState.inspectingTask != null ||
            uiState.schedulingInboxTask != null ||
            uiState.selectedFolderId != null ||
            uiState.activeDestination != StruktDestination.FLOW
    ) {
        when {
            uiState.isEditorVisible -> viewModel.closeTaskSheet()
            uiState.inspectingTask != null -> viewModel.closeInspectTaskModal()
            uiState.schedulingInboxTask != null -> viewModel.closeScheduleInboxPopup()
            uiState.selectedFolderId != null -> viewModel.selectFolder(null)
            else -> viewModel.setDestination(StruktDestination.FLOW)
        }
    }

    LaunchedEffect(uiState.statusBannerMessage) {
        if (uiState.statusBannerMessage != null) {
            delay(2200L)
            viewModel.clearStatusBanner()
        }
    }

    DotGridBackground(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val naturalSplit = maxWidth >= 680.dp
            val isTabletSplitLayout = uiState.forceSplitPreview ?: naturalSplit

            val handleToggleOrientation = {
                val activity = context as? Activity
                if (activity != null) {
                    activity.requestedOrientation = if (isTabletSplitLayout) {
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    } else {
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    }
                }
                viewModel.toggleSplitViewMode(isTabletSplitLayout)
            }

            if (isTabletSplitLayout) {
                TabletLandscapeSplitLayout(
                    uiState = uiState,
                    viewModel = viewModel,
                    onToggleOrientation = handleToggleOrientation
                )
            } else {
                PortraitHandheldLayout(
                    uiState = uiState,
                    viewModel = viewModel,
                    onToggleOrientation = handleToggleOrientation
                )
            }

            // Minimal Toast Banner
            AnimatedVisibility(
                visible = uiState.statusBannerMessage != null,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(NeoMintGreen)
                        .border(1.8.dp, InkBlack, RoundedCornerShape(50))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = uiState.statusBannerMessage.orEmpty(),
                        style = MaterialTheme.typography.titleMedium,
                        color = InkBlack
                    )
                }
            }

            // 1. Small Schedule Mini-Popup from Inbox "+"
            uiState.schedulingInboxTask?.let { inboxTask ->
                ScheduleInboxMiniPopup(
                    task = inboxTask,
                    onDismiss = { viewModel.closeScheduleInboxPopup() },
                    onConfirmSchedule = { asTimed, startMin ->
                        viewModel.confirmScheduleInboxTask(inboxTask, asTimed, startMin)
                    }
                )
            }

            // 2. Clean Task & Subtasks Detail Modal when clicking a task on the timeline
            uiState.inspectingTask?.let { inspected ->
                TaskDetailPopupModal(
                    item = inspected,
                    onDismiss = { viewModel.closeInspectTaskModal() },
                    onToggleComplete = { viewModel.toggleTaskDone(inspected.task) },
                    onToggleSubtask = viewModel::toggleSubtaskDone,
                    onAddSubtask = { title -> viewModel.addQuickSubtask(inspected.task.id, title) },
                    onStartFocus = { viewModel.enterFocusForTask(inspected.task.id) },
                    onEditTask = { viewModel.openEditTaskSheet(inspected) }
                )
            }

            // 3. Progressive Sub-Popup Task Editor Sheet
            if (uiState.isEditorVisible) {
                StructuredTaskEditorDialog(
                    selectedDate = uiState.selectedLocalDate,
                    folders = uiState.folders,
                    editingItem = uiState.editingTask,
                    defaultMode = uiState.editorDefaultMode,
                    defaultToInbox = uiState.editorTargetInbox,
                    onDismiss = { viewModel.closeTaskSheet() },
                    onSave = { id, title, notes, toInbox, mode, startMin, duration, color, icon, folder, section, recur, dayOfMonth, subs, order ->
                        viewModel.saveTaskFromEditor(
                            existingId = id,
                            title = title,
                            notes = notes,
                            toInbox = toInbox,
                            taskMode = mode,
                            startTimeMinutes = startMin,
                            durationMinutes = duration,
                            colorKey = color,
                            iconKey = icon,
                            folderName = folder,
                            sectionName = section,
                            recurrence = recur,
                            recurrenceDayOfMonth = dayOfMonth,
                            subtasks = subs,
                            sortOrder = order
                        )
                    },
                    onMoveToInbox = { task -> viewModel.sendTaskBackToInbox(task) },
                    onDelete = { taskId -> viewModel.deleteTask(taskId) }
                )
            }
        }
    }
}

/**
 * Redmi Pad Pro 5G Horizontal Split-Screen Layout (Matches Screenshot 5 iPad view):
 * Left Sidebar: Brain-dump Inbox.
 * Right Workspace: Clean Month Header + Week Strip + White Timeline Sheet (or Folders/Focus).
 */
@Composable
private fun TabletLandscapeSplitLayout(
    uiState: PlannerUiState,
    viewModel: PlannerViewModel,
    onToggleOrientation: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Left Sidebar — Inbox Panel
        Box(
            modifier = Modifier
                .width(300.dp)
                .fillMaxHeight()
        ) {
            NeoInboxPane(
                inboxTasks = uiState.inboxTasks,
                dayReadinessPercent = uiState.dayReadinessPercent,
                completedCount = uiState.completedDayTasksCount,
                totalCount = uiState.totalDayTasksCount,
                onQuickAddInbox = { title -> viewModel.quickAddInboxTask(title) },
                onOpenSchedulePopup = { task -> viewModel.openScheduleInboxPopup(task) },
                onEditInboxTask = { item -> viewModel.openEditTaskSheet(item) }
            )
        }

        // Subtle Vertical Divider
        Box(
            modifier = Modifier
                .width(1.5.dp)
                .fillMaxHeight()
                .padding(vertical = 12.dp)
                .background(InkBlack.copy(alpha = 0.12f))
        )

        // Right Main Workspace
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Centered Geometric Icon-Only Switcher Bar at Top Right
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GeometricIconNavPill(
                        activeDestination = uiState.activeDestination,
                        includeInbox = false,
                        onSelectDestination = viewModel::setDestination
                    )
                }

                when (uiState.activeDestination) {
                    StruktDestination.FOLDERS -> {
                        FoldersAndSectionsScreen(
                            allTasks = uiState.allTasks,
                            folders = uiState.folders,
                            selectedFolderId = uiState.selectedFolderId,
                            searchQuery = uiState.searchQuery,
                            onSearchQueryChange = viewModel::setSearchQuery,
                            onSelectFolder = viewModel::selectFolder,
                            onCreateFolder = viewModel::createNewFolder,
                            onAddSectionToFolder = viewModel::addSectionToFolder,
                            onTaskClick = { item -> viewModel.openInspectTaskModal(item.task.id) },
                            onToggleTaskDone = viewModel::toggleTaskDone
                        )
                    }

                    StruktDestination.FOCUS -> {
                        AntiParalysisFocusScreen(
                            focusItem = uiState.activeFocusTask,
                            onBackToFlow = { viewModel.setDestination(StruktDestination.FLOW) },
                            onStartOrResumeSession = viewModel::startOrResumeFocusTimer,
                            onPauseSession = viewModel::pauseFocusTimer,
                            onToggleSubtask = viewModel::toggleSubtaskDone,
                            onAddQuickSubtask = viewModel::addQuickSubtask,
                            onCompleteAndAdvance = viewModel::completeFocusedTaskAndAdvance
                        )
                    }

                    else -> {
                        DayFlowSheetContent(
                            uiState = uiState,
                            viewModel = viewModel,
                            onToggleOrientation = onToggleOrientation
                        )
                    }
                }
            }

            if (uiState.activeDestination != StruktDestination.FOCUS) {
                NeoFloatingAddButton(
                    onClick = {
                        viewModel.openCreateTaskSheet(
                            defaultMode = TaskMode.TIMELESS_STEP,
                            forInbox = false
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(18.dp)
                )
            }
        }
    }
}

/**
 * Portrait Handheld Layout (Matches Screenshot 6 + Centered Icon-Only Bottom Nav Bar).
 */
@Composable
private fun PortraitHandheldLayout(
    uiState: PlannerUiState,
    viewModel: PlannerViewModel,
    onToggleOrientation: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            when (uiState.activeDestination) {
                StruktDestination.INBOX -> {
                    NeoInboxPane(
                        inboxTasks = uiState.inboxTasks,
                        dayReadinessPercent = uiState.dayReadinessPercent,
                        completedCount = uiState.completedDayTasksCount,
                        totalCount = uiState.totalDayTasksCount,
                        onQuickAddInbox = { title -> viewModel.quickAddInboxTask(title) },
                        onOpenSchedulePopup = { task -> viewModel.openScheduleInboxPopup(task) },
                        onEditInboxTask = { item -> viewModel.openEditTaskSheet(item) }
                    )
                }

                StruktDestination.FOLDERS -> {
                    FoldersAndSectionsScreen(
                        allTasks = uiState.allTasks,
                        folders = uiState.folders,
                        selectedFolderId = uiState.selectedFolderId,
                        searchQuery = uiState.searchQuery,
                        onSearchQueryChange = viewModel::setSearchQuery,
                        onSelectFolder = viewModel::selectFolder,
                        onCreateFolder = viewModel::createNewFolder,
                        onAddSectionToFolder = viewModel::addSectionToFolder,
                        onTaskClick = { item -> viewModel.openInspectTaskModal(item.task.id) },
                        onToggleTaskDone = viewModel::toggleTaskDone
                    )
                }

                StruktDestination.FOCUS -> {
                    AntiParalysisFocusScreen(
                        focusItem = uiState.activeFocusTask,
                        onBackToFlow = { viewModel.setDestination(StruktDestination.FLOW) },
                        onStartOrResumeSession = viewModel::startOrResumeFocusTimer,
                        onPauseSession = viewModel::pauseFocusTimer,
                        onToggleSubtask = viewModel::toggleSubtaskDone,
                        onAddQuickSubtask = viewModel::addQuickSubtask,
                        onCompleteAndAdvance = viewModel::completeFocusedTaskAndAdvance
                    )
                }

                StruktDestination.FLOW -> {
                    DayFlowSheetContent(
                        uiState = uiState,
                        viewModel = viewModel,
                        onToggleOrientation = onToggleOrientation
                    )
                }
            }
        }

        // Geometric Centered Icon-Only Bottom Bar + "+" FAB
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GeometricIconNavPill(
                activeDestination = uiState.activeDestination,
                includeInbox = true,
                onSelectDestination = viewModel::setDestination,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(12.dp))

            NeoFloatingAddButton(
                onClick = {
                    viewModel.openCreateTaskSheet(
                        defaultMode = TaskMode.TIMELESS_STEP,
                        forInbox = uiState.activeDestination == StruktDestination.INBOX
                    )
                }
            )
        }
    }
}

/**
 * Geometric Icon-Only Navigation Pill — Every icon is strictly centered and aligned with zero text clutter.
 */
@Composable
private fun GeometricIconNavPill(
    activeDestination: StruktDestination,
    includeInbox: Boolean,
    onSelectDestination: (StruktDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val navItems = buildList {
        if (includeInbox) add(Triple(StruktDestination.INBOX, "Inbox", Icons.Default.Inbox))
        add(Triple(StruktDestination.FLOW, "Timeline", Icons.Default.ViewTimeline))
        add(Triple(StruktDestination.FOLDERS, "Folders", Icons.Default.Folder))
        add(Triple(StruktDestination.FOCUS, "Focus", Icons.Default.CenterFocusStrong))
    }

    Row(
        modifier = modifier
            .height(54.dp)
            .drawBehind {
                drawRoundRect(
                    color = InkBlack,
                    topLeft = Offset(3.dp.toPx(), 3.dp.toPx()),
                    size = size,
                    cornerRadius = CornerRadius(27.dp.toPx(), 27.dp.toPx())
                )
            }
            .clip(RoundedCornerShape(50))
            .background(PureWhiteSurface)
            .border(1.8.dp, InkBlack, RoundedCornerShape(50))
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        navItems.forEach { (dest, contentDesc, icon) ->
            val selected = activeDestination == dest
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (selected) NeoCoral else PureWhiteSurface)
                    .then(
                        if (selected) Modifier.border(1.5.dp, InkBlack, CircleShape)
                        else Modifier
                    )
                    .clickable(role = Role.Tab) { onSelectDestination(dest) }
                    .testTag("bottom_nav_${dest.name.lowercase()}"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDesc,
                    tint = InkBlack,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Clean Single-Window Day Flow Sheet (Removed separate upper "Do This Next" card so the calendar/sheet never shifts!).
 */
@Composable
private fun DayFlowSheetContent(
    uiState: PlannerUiState,
    viewModel: PlannerViewModel,
    onToggleOrientation: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        DateAndWeekHeader(
            selectedDate = uiState.selectedLocalDate,
            weekDotColorsByEpochDay = uiState.weekDotColorsByEpochDay,
            onSelectDate = viewModel::selectDate,
            onShiftWeek = viewModel::shiftWeek,
            onJumpToToday = viewModel::jumpToToday,
            onToggleOrientationMode = onToggleOrientation
        )

        Spacer(modifier = Modifier.height(8.dp))

        NeoCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            backgroundColor = PureWhiteSurface,
            cornerRadius = 26.dp,
            shadowOffset = 4.dp,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Sheet Handle Pill at Top Center (Matches Screenshot 5 & 6)
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(50))
                                .background(InkBlack.copy(alpha = 0.16f))
                        )
                    }
                }

                // 1. Top Routine Bubbles (Clean, no "+ Add Bubble" clutter)
                if (uiState.topBubbles.isNotEmpty()) {
                    item {
                        TopAnytimeBubblesRow(
                            bubbles = uiState.topBubbles,
                            onBubbleClick = { item ->
                                viewModel.openInspectTaskModal(item.task.id)
                            }
                        )
                    }
                    item {
                        NeoDashedDivider()
                    }
                }

                // 2. Minimal Filter Bar (All / Timeless / Timed)
                item {
                    FlowStructureFilterBar(
                        currentFilter = uiState.flowFilter,
                        onSelectFilter = viewModel::setFlowFilter
                    )
                }

                // 3. Unified Timeless + Timed Vertical Capsule Spine
                if (uiState.spineFlowTasks.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No Tasks Scheduled",
                                style = MaterialTheme.typography.titleLarge,
                                color = InkBlack
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            NeoPillButton(
                                text = "+ Add Task",
                                selected = true,
                                activeColor = NeoYellow,
                                onClick = {
                                    viewModel.openCreateTaskSheet(
                                        defaultMode = TaskMode.TIMELESS_STEP,
                                        forInbox = false
                                    )
                                }
                            )
                        }
                    }
                } else {
                    itemsIndexed(
                        items = uiState.spineFlowTasks,
                        key = { _, item -> item.task.id }
                    ) { index, item ->
                        val isUpNext = uiState.upNextTask?.task?.id == item.task.id

                        if (index > 0) {
                            val prev = uiState.spineFlowTasks[index - 1].task
                            val curr = item.task
                            if (
                                prev.modeEnum == TaskMode.TIMED_BLOCK &&
                                curr.modeEnum == TaskMode.TIMED_BLOCK &&
                                prev.startTimeMinutes != null &&
                                curr.startTimeMinutes != null
                            ) {
                                val prevEnd = prev.startTimeMinutes + prev.durationMinutes
                                val gap = curr.startTimeMinutes - prevEnd
                                if (gap >= 15) {
                                    FreeTimeGapIndicator(
                                        gapMinutes = gap,
                                        onFillGapClick = {
                                            viewModel.openCreateTaskSheet(
                                                defaultMode = TaskMode.TIMELESS_STEP,
                                                forInbox = false
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        HybridSpineTaskRow(
                            item = item,
                            sequenceNumber = index + 1,
                            isUpNext = isUpNext,
                            onToggleComplete = { viewModel.toggleTaskDone(item.task) },
                            onOpenTaskDetail = { viewModel.openInspectTaskModal(item.task.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NeoFloatingAddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(54.dp)
            .drawBehind {
                drawCircle(
                    color = InkBlack,
                    radius = size.minDimension / 2f,
                    center = Offset(center.x + 3.dp.toPx(), center.y + 3.dp.toPx())
                )
            }
            .clip(CircleShape)
            .background(NeoCoral)
            .border(2.dp, InkBlack, CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .testTag("fab_add_task"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Task",
            tint = PureWhiteSurface,
            modifier = Modifier.size(26.dp)
        )
    }
}
