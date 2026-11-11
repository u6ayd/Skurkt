package com.example

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.ui.components.HybridSpineTaskRow
import com.example.ui.components.NeoCard
import com.example.ui.components.NeoDashedDivider
import com.example.ui.components.NeoInboxPane
import com.example.ui.components.NeoPillButton
import com.example.ui.components.ScheduleInboxMiniPopup
import com.example.ui.components.SpineConnectorSegment
import com.example.ui.components.StructuredTaskEditorDialog
import com.example.ui.components.TaskDetailPopupModal
import com.example.ui.components.TopAnytimeBubblesRow
import com.example.ui.components.smoothPressClickable
import com.example.ui.theme.InkBlack
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavRailDark
import com.example.ui.theme.NeoCoral
import com.example.ui.theme.NeoMintGreen
import com.example.ui.theme.NeoYellow
import com.example.ui.theme.PureWhiteSurface
import com.example.ui.theme.sdp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
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
        modifier = Modifier.fillMaxSize()
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            // Portrait (maxWidth <= maxHeight) ALWAYS shows the 1-screen vertical layout with the bottom bar (on both phone and tablet).
            // Landscape (maxWidth > maxHeight) shows the 2-column split screen (Left Dark Rail + Persistent Inbox + Main Workspace).
            val isLandscapeSplitLayout = maxWidth > maxHeight
            val isTabletDevice = minOf(maxWidth, maxHeight) >= 600.dp

            if (isLandscapeSplitLayout) {
                TabletLandscapeSplitLayout(
                    uiState = uiState,
                    viewModel = viewModel,
                    isTabletDevice = isTabletDevice
                )
            } else {
                PortraitHandheldLayout(
                    uiState = uiState,
                    viewModel = viewModel
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
                        .border(1.5.dp, InkBlack, RoundedCornerShape(50))
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
                val spineIdx = uiState.spineFlowTasks.indexOfFirst { it.task.id == inspected.task.id }
                val currentStep = if (spineIdx >= 0) spineIdx + 1 else 1
                TaskDetailPopupModal(
                    item = inspected,
                    currentStepNumber = currentStep,
                    onUpdateStepNumber = { newStep -> viewModel.updateTaskStepNumber(inspected.task, newStep) },
                    onDismiss = { viewModel.closeInspectTaskModal() },
                    onToggleComplete = { viewModel.toggleTaskDone(inspected.task) },
                    onToggleBreakLoop = { viewModel.toggleTaskBreakLoop(inspected.task) },
                    onToggleSubtask = viewModel::toggleSubtaskDone,
                    onAddSubtask = { title -> viewModel.addQuickSubtask(inspected.task.id, title) },
                    onStartFocus = { viewModel.enterFocusForTask(inspected.task.id) },
                    onEditTask = { viewModel.openEditTaskSheet(inspected) },
                    onDeleteTask = {
                        viewModel.deleteTask(inspected.task.id)
                        viewModel.closeInspectTaskModal()
                    }
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
 * Redmi Pad Pro 5G Tablet Layout (Inspired by Learnify Left Dark Rail + Structured Split-View):
 * - Left Dark Navigation Rail with larger (26.dp) icons and yellow active badge.
 * - Persistent Inbox Column + Main Right Workspace.
 */
@Composable
private fun TabletLandscapeSplitLayout(
    uiState: PlannerUiState,
    viewModel: PlannerViewModel,
    isTabletDevice: Boolean
) {
    val railWidth = if (isTabletDevice) 88.dp else 72.dp
    val inboxWidth = if (isTabletDevice) 380.dp else 310.dp

    Row(modifier = Modifier.fillMaxSize()) {
        // Learnify-Inspired Permanent Left Navigation Rail (extends edge-to-edge vertically)
        Column(
            modifier = Modifier
                .width(railWidth)
                .fillMaxHeight()
                .background(NavRailDark)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.sdp)
            ) {
                val railItems = listOf(
                    Triple(StruktDestination.FLOW, "Timeline", Icons.Default.ViewTimeline),
                    Triple(StruktDestination.FOLDERS, "Folders", Icons.Default.Folder),
                    Triple(StruktDestination.FOCUS, "Focus", Icons.Default.CenterFocusStrong)
                )
                railItems.forEach { (dest, desc, icon) ->
                    val selected = uiState.activeDestination == dest
                    Box(
                        modifier = Modifier
                            .size(48.sdp)
                            .smoothPressClickable(role = Role.Tab) { viewModel.setDestination(dest) }
                            .clip(RoundedCornerShape(14.sdp))
                            .background(if (selected) NeoYellow else NavRailDark)
                            .testTag("tablet_nav_${dest.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = desc,
                            tint = if (selected) InkBlack else PureWhiteSurface,
                            modifier = Modifier.size(26.sdp)
                        )
                    }
                }
            }

            // Bottom "+" Add Action inside the Permanent Rail
            Box(
                modifier = Modifier
                    .size(48.sdp)
                    .smoothPressClickable(role = Role.Button) {
                        viewModel.openCreateTaskSheet(
                            defaultMode = TaskMode.TIMELESS_STEP,
                            forInbox = false
                        )
                    }
                    .clip(RoundedCornerShape(14.sdp))
                    .background(NeoCoral)
                    .border(1.5.dp, PureWhiteSurface.copy(alpha = 0.25f), RoundedCornerShape(14.sdp))
                    .testTag("tablet_new_step_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task",
                    tint = PureWhiteSurface,
                    modifier = Modifier.size(26.sdp)
                )
            }
        }

        // Left Inbox Column (380.dp on tablet, 310.dp on phone landscape)
        Box(
            modifier = Modifier
                .width(inboxWidth)
                .fillMaxHeight()
                .statusBarsPadding()
                .navigationBarsPadding()
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

        // Clean 1.5.dp Vertical Divider Line
        Box(
            modifier = Modifier
                .width(1.5.dp)
                .fillMaxHeight()
                .background(InkBlack.copy(alpha = 0.12f))
        )

        // Right Main Workspace (centered within max 680.dp so single-column cards never over-stretch)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 0.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .fillMaxSize()
            ) {
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
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

/**
 * Portrait Handheld Layout with a Permanent Full-Width Straight Bottom Bar & Larger (26.dp) Icons.
 */
@Composable
private fun PortraitHandheldLayout(
    uiState: PlannerUiState,
    viewModel: PlannerViewModel
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Main Content takes remaining height flush to the permanent bottom bar's top line,
        // centered within max 680.dp so tablet portrait (800.dp) has balanced margins while phone fills 100%.
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 0.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .fillMaxSize()
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
                            viewModel = viewModel
                        )
                    }
                }
            }
        }

        // Permanent Full-Width Straight Docked Bottom Navigation Bar with Proportional Scaling
        PermanentStraightBottomBar(
            activeDestination = uiState.activeDestination,
            onSelectDestination = viewModel::setDestination,
            onAddTaskClick = {
                viewModel.openCreateTaskSheet(
                    defaultMode = TaskMode.TIMELESS_STEP,
                    forInbox = uiState.activeDestination == StruktDestination.INBOX
                )
            }
        )
    }
}

/**
 * Permanent, full-width straight docked bottom bar separated by a clean 1.5.dp line,
 * featuring proportionally scaled icons (26.sdp) and a dedicated "+" button.
 */
@Composable
private fun PermanentStraightBottomBar(
    activeDestination: StruktDestination,
    onSelectDestination: (StruktDestination) -> Unit,
    onAddTaskClick: () -> Unit
) {
    val navItems = listOf(
        Triple(StruktDestination.INBOX, "Inbox", Icons.Default.Inbox),
        Triple(StruktDestination.FLOW, "Timeline", Icons.Default.ViewTimeline),
        Triple(StruktDestination.FOLDERS, "Folders", Icons.Default.Folder),
        Triple(StruktDestination.FOCUS, "Focus", Icons.Default.CenterFocusStrong)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PureWhiteSurface)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Straight full-width horizontal line separating the permanent bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(InkBlack)
        )

        Row(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxWidth()
                .height(66.sdp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { (dest, contentDesc, icon) ->
                val selected = activeDestination == dest
                Box(
                    modifier = Modifier
                        .size(48.sdp)
                        .smoothPressClickable(role = Role.Tab) { onSelectDestination(dest) }
                        .clip(RoundedCornerShape(14.sdp))
                        .background(if (selected) NeoYellow else PureWhiteSurface)
                        .then(
                            if (selected) Modifier.border(1.5.dp, InkBlack, RoundedCornerShape(14.sdp))
                            else Modifier
                        )
                        .testTag("bottom_nav_${dest.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = contentDesc,
                        tint = InkBlack,
                        modifier = Modifier.size(26.sdp)
                    )
                }
            }

            // Symmetrical 48.sdp Coral "+" Button inside the permanent bar
            Box(
                modifier = Modifier
                    .size(48.sdp)
                    .smoothPressClickable(role = Role.Button, onClick = onAddTaskClick)
                    .clip(RoundedCornerShape(14.sdp))
                    .background(NeoCoral)
                    .border(1.5.dp, InkBlack, RoundedCornerShape(14.sdp))
                    .testTag("fab_add_task"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task",
                    tint = PureWhiteSurface,
                    modifier = Modifier.size(26.sdp)
                )
            }
        }
    }
}

@Composable
private fun DayFlowSheetContent(
    uiState: PlannerUiState,
    viewModel: PlannerViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 8.dp)
    ) {
        DateAndWeekHeader(
            selectedDate = uiState.selectedLocalDate,
            weekDotColorsByEpochDay = uiState.weekDotColorsByEpochDay,
            onSelectDate = viewModel::selectDate
        )

        Spacer(modifier = Modifier.height(8.dp))

        NeoCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            backgroundColor = PureWhiteSurface,
            cornerRadius = 24.dp,
            contentPadding = PaddingValues(start = 10.dp, end = 16.dp, top = 14.dp, bottom = 14.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
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

                if (uiState.topBubbles.isNotEmpty()) {
                    item {
                        TopAnytimeBubblesRow(
                            bubbles = uiState.topBubbles,
                            onToggleBubbleDone = { item ->
                                viewModel.toggleTaskDone(item.task)
                            },
                            onInspectBubble = { item ->
                                viewModel.openInspectTaskModal(item.task.id)
                            },
                            onEditBubble = { item ->
                                viewModel.openEditTaskSheet(item)
                            },
                            onDeleteBubble = { taskId ->
                                viewModel.deleteTask(taskId)
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    item {
                        NeoDashedDivider()
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                item {
                    FlowStructureFilterBar(
                        currentFilter = uiState.flowFilter,
                        onSelectFilter = viewModel::setFlowFilter
                    )
                    // Pushed downwards for clean breathing room before the vertical timeline spine begins
                    Spacer(modifier = Modifier.height(22.dp))
                }

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
                            val hasTimeGap = if (
                                prev.modeEnum == TaskMode.TIMED_BLOCK &&
                                curr.modeEnum == TaskMode.TIMED_BLOCK &&
                                prev.startTimeMinutes != null &&
                                curr.startTimeMinutes != null
                            ) {
                                val prevEnd = prev.startTimeMinutes + prev.durationMinutes
                                (curr.startTimeMinutes - prevEnd) > 0
                            } else {
                                false
                            }

                            SpineConnectorSegment(
                                isDottedBreak = hasTimeGap,
                                height = if (hasTimeGap) 26.dp else 14.dp
                            )
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
