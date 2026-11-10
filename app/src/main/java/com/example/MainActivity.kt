package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.FolderSpecial
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.TaskMode
import com.example.ui.PlannerUiState
import com.example.ui.PlannerViewModel
import com.example.ui.StruktDestination
import com.example.ui.components.AntiParalysisFocusScreen
import com.example.ui.components.BlueprintsAndMatrixScreen
import com.example.ui.components.DateAndWeekHeader
import com.example.ui.components.DotGridBackground
import com.example.ui.components.FlowStructureFilterBar
import com.example.ui.components.FreeTimeGapIndicator
import com.example.ui.components.HybridSpineTaskRow
import com.example.ui.components.NeoCard
import com.example.ui.components.NeoDashedDivider
import com.example.ui.components.NeoInboxPane
import com.example.ui.components.NeoPillButton
import com.example.ui.components.PinnedUpNextBanner
import com.example.ui.components.StructuredTaskEditorDialog
import com.example.ui.components.TopAnytimeBubblesRow
import com.example.ui.theme.InkBlack
import com.example.ui.theme.InkCharcoal
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
    // Handle system Back button when on secondary tabs or modals
    BackHandler(enabled = uiState.isEditorVisible || uiState.activeDestination != StruktDestination.FLOW) {
        if (uiState.isEditorVisible) {
            viewModel.closeTaskSheet()
        } else {
            viewModel.setDestination(StruktDestination.FLOW)
        }
    }

    // Auto-dismiss transient toast banner after 3 seconds
    LaunchedEffect(uiState.statusBannerMessage) {
        if (uiState.statusBannerMessage != null) {
            delay(3000L)
            viewModel.clearStatusBanner()
        }
    }

    DotGridBackground(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            // Responsive threshold: Redmi Pad Pro 5G in horizontal/landscape or wide tablet split-view
            val isTabletSplitLayout = maxWidth >= 760.dp

            if (isTabletSplitLayout) {
                TabletLandscapeSplitLayout(
                    uiState = uiState,
                    viewModel = viewModel
                )
            } else {
                PortraitHandheldLayout(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }

            // Status Toast Banner at Top Center
            AnimatedVisibility(
                visible = uiState.statusBannerMessage != null,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(NeoMintGreen)
                        .border(2.dp, InkBlack, RoundedCornerShape(50))
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = uiState.statusBannerMessage.orEmpty(),
                        style = MaterialTheme.typography.titleMedium,
                        color = InkBlack
                    )
                }
            }

            // Task Creation / Edit Modal Sheet (Inspired by Screenshot 7)
            if (uiState.isEditorVisible) {
                StructuredTaskEditorDialog(
                    selectedDate = uiState.selectedLocalDate,
                    editingItem = uiState.editingTask,
                    defaultMode = uiState.editorDefaultMode,
                    defaultToInbox = uiState.editorTargetInbox,
                    onDismiss = { viewModel.closeTaskSheet() },
                    onSave = { id, title, notes, toInbox, mode, startMin, duration, color, icon, tag, recur, subs, order ->
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
                            subjectTag = tag,
                            recurrence = recur,
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
 * Redmi Pad Pro 5G Horizontal Split-Screen Layout (Inspired by Screenshot 5 iPad view + Screenshot 1 Trackerly Top Bar):
 * - Left Column: Always-accessible Brain-Dump Inbox + Readiness Stats + one-tap "+" to schedule into the day.
 * - Right Area: Top Navigation Bar + Date/Week Ribbon + Main White Structured Sheet (or Blueprints/Focus).
 */
@Composable
private fun TabletLandscapeSplitLayout(
    uiState: PlannerUiState,
    viewModel: PlannerViewModel
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left Sidebar (330.dp width) — Persistent Brain-Dump Inbox & Quick Readiness
        Box(
            modifier = Modifier
                .width(340.dp)
                .fillMaxHeight()
        ) {
            NeoInboxPane(
                inboxTasks = uiState.inboxTasks,
                dayReadinessPercent = uiState.dayReadinessPercent,
                completedCount = uiState.completedDayTasksCount,
                totalCount = uiState.totalDayTasksCount,
                onQuickAddInbox = { title -> viewModel.quickAddInboxTask(title) },
                onOpenFullCreateForInbox = {
                    viewModel.openCreateTaskSheet(defaultMode = TaskMode.TIMELESS_STEP, forInbox = true)
                },
                onSendToDayTimeless = { task -> viewModel.sendInboxTaskToSelectedDay(task) },
                onEditInboxTask = { item -> viewModel.openEditTaskSheet(item) }
            )
        }

        // Vertical Divider Line
        Box(
            modifier = Modifier
                .width(2.dp)
                .fillMaxHeight()
                .padding(vertical = 12.dp)
                .background(InkBlack.copy(alpha = 0.15f))
        )

        // Right Main Workspace (Day Flow / Blueprints & Matrix / Focus Mode)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Brand & Workspace Switcher Bar (Inspired by Screenshot 1: "Trackerly STUDY OS")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        NeoPillButton(
                            text = "Day Flow",
                            icon = Icons.Default.ViewTimeline,
                            selected = uiState.activeDestination == StruktDestination.FLOW,
                            activeColor = NeoYellow,
                            onClick = { viewModel.setDestination(StruktDestination.FLOW) },
                            modifier = Modifier.testTag("tablet_nav_flow")
                        )
                        NeoPillButton(
                            text = "Blueprints & Matrix",
                            icon = Icons.Default.FolderSpecial,
                            selected = uiState.activeDestination == StruktDestination.BLUEPRINTS,
                            activeColor = NeoSkyBlue,
                            onClick = { viewModel.setDestination(StruktDestination.BLUEPRINTS) },
                            modifier = Modifier.testTag("tablet_nav_blueprints")
                        )
                        NeoPillButton(
                            text = "Focus Now",
                            icon = Icons.Default.CenterFocusStrong,
                            selected = uiState.activeDestination == StruktDestination.FOCUS,
                            activeColor = NeoMintGreen,
                            onClick = { viewModel.setDestination(StruktDestination.FOCUS) },
                            modifier = Modifier.testTag("tablet_nav_focus")
                        )
                    }

                    NeoPillButton(
                        text = "+ New Step",
                        icon = Icons.Default.Add,
                        selected = true,
                        activeColor = NeoCoral,
                        onClick = {
                            viewModel.openCreateTaskSheet(
                                defaultMode = TaskMode.TIMELESS_STEP,
                                forInbox = false
                            )
                        },
                        modifier = Modifier.testTag("tablet_new_step_button")
                    )
                }

                when (uiState.activeDestination) {
                    StruktDestination.BLUEPRINTS -> {
                        BlueprintsAndMatrixScreen(
                            allTasks = uiState.allTasks,
                            templates = uiState.templates,
                            searchQuery = uiState.searchQuery,
                            onSearchQueryChange = viewModel::setSearchQuery,
                            onApplyTemplate = viewModel::applyBlueprintToSelectedDay,
                            onSaveCurrentDayAsTemplate = viewModel::saveCurrentDayAsBlueprint,
                            onToggleSubtask = viewModel::toggleSubtaskDone
                        )
                    }

                    StruktDestination.FOCUS -> {
                        AntiParalysisFocusScreen(
                            focusItem = uiState.activeFocusTask,
                            onBackToFlow = { viewModel.setDestination(StruktDestination.FLOW) },
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

            // Floating "+" Coral Action Button in bottom-right corner (Matches Screenshot 5)
            if (uiState.activeDestination == StruktDestination.FLOW) {
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
 * Portrait / Vertical Handheld Layout (Inspired by Screenshot 6):
 * Full-width content + Structured's floating bottom pill navigation bar + bottom-right Coral "+" FAB.
 */
@Composable
private fun PortraitHandheldLayout(
    uiState: PlannerUiState,
    viewModel: PlannerViewModel
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
                        onOpenFullCreateForInbox = {
                            viewModel.openCreateTaskSheet(
                                defaultMode = TaskMode.TIMELESS_STEP,
                                forInbox = true
                            )
                        },
                        onSendToDayTimeless = { task ->
                            viewModel.sendInboxTaskToSelectedDay(task)
                            viewModel.setDestination(StruktDestination.FLOW)
                        },
                        onEditInboxTask = { item -> viewModel.openEditTaskSheet(item) }
                    )
                }

                StruktDestination.BLUEPRINTS -> {
                    BlueprintsAndMatrixScreen(
                        allTasks = uiState.allTasks,
                        templates = uiState.templates,
                        searchQuery = uiState.searchQuery,
                        onSearchQueryChange = viewModel::setSearchQuery,
                        onApplyTemplate = viewModel::applyBlueprintToSelectedDay,
                        onSaveCurrentDayAsTemplate = viewModel::saveCurrentDayAsBlueprint,
                        onToggleSubtask = viewModel::toggleSubtaskDone
                    )
                }

                StruktDestination.FOCUS -> {
                    AntiParalysisFocusScreen(
                        focusItem = uiState.activeFocusTask,
                        onBackToFlow = { viewModel.setDestination(StruktDestination.FLOW) },
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

        // Structured-Style Floating Bottom Pill Bar + "+" FAB (Directly inspired by Screenshot 6)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Floating Pill Bar
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .drawBehind {
                        drawRoundRect(
                            color = InkBlack,
                            topLeft = Offset(3.5.dp.toPx(), 3.5.dp.toPx()),
                            size = size,
                            cornerRadius = CornerRadius(30.dp.toPx(), 30.dp.toPx())
                        )
                    }
                    .clip(RoundedCornerShape(50))
                    .background(PureWhiteSurface)
                    .border(2.dp, InkBlack, RoundedCornerShape(50))
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val navItems = listOf(
                    Triple(StruktDestination.INBOX, "Inbox", Icons.Default.Inbox),
                    Triple(StruktDestination.FLOW, "Timeline", Icons.Default.ViewTimeline),
                    Triple(StruktDestination.BLUEPRINTS, "Blueprints", Icons.Default.FolderSpecial),
                    Triple(StruktDestination.FOCUS, "Focus", Icons.Default.CenterFocusStrong)
                )
                navItems.forEach { (dest, label, icon) ->
                    val selected = uiState.activeDestination == dest
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (selected) NeoYellow else PureWhiteSurface)
                            .then(
                                if (selected) Modifier.border(1.5.dp, InkBlack, RoundedCornerShape(50))
                                else Modifier
                            )
                            .clickable(role = Role.Tab) { viewModel.setDestination(dest) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("bottom_nav_${dest.name.lowercase()}"),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = InkBlack,
                            modifier = Modifier.size(19.dp)
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 10.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = InkBlack
                            )
                        )
                    }
                }
            }

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
 * Main Day Flow Sheet combining the 7-Day Week Ribbon, Pinned Anti-Paralysis Banner,
 * Top Anytime Bubbles, and Unified Timeless + Timed Vertical Capsule Spine.
 */
@Composable
private fun DayFlowSheetContent(
    uiState: PlannerUiState,
    viewModel: PlannerViewModel
) {
    Column(modifier = Modifier.fillMaxSize()) {
        DateAndWeekHeader(
            selectedDate = uiState.selectedLocalDate,
            weekDotColorsByEpochDay = uiState.weekDotColorsByEpochDay,
            onSelectDate = viewModel::selectDate,
            onShiftWeek = viewModel::shiftWeek,
            onJumpToToday = viewModel::jumpToToday,
            onResetDay = viewModel::resetSelectedDayProgress
        )

        Spacer(modifier = Modifier.height(10.dp))

        // White Neo-Brutalist Sheet Container (Matches Screenshot 5 & 6 white timeline sheet with 2.dp ink border)
        NeoCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            backgroundColor = PureWhiteSurface,
            cornerRadius = 26.dp,
            shadowOffset = 5.dp,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Sheet Handle Pill at Top Center (Like Screenshot 5 & 6)
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(44.dp)
                                .height(5.dp)
                                .clip(RoundedCornerShape(50))
                                .background(InkBlack.copy(alpha = 0.2f))
                        )
                    }
                }

                // 1. Pinned Anti-Paralysis "UP NEXT" Card
                if (uiState.upNextTask != null) {
                    item {
                        PinnedUpNextBanner(
                            upNext = uiState.upNextTask,
                            onEnterFocus = {
                                viewModel.enterFocusForTask(uiState.upNextTask.task.id)
                            },
                            onCompleteNow = {
                                viewModel.toggleTaskDone(uiState.upNextTask.task)
                            }
                        )
                    }
                }

                // 2. Top Circular "Anytime" Routine Bubbles (Like Screenshot 5 & 6)
                item {
                    TopAnytimeBubblesRow(
                        bubbles = uiState.topBubbles,
                        onToggleBubble = viewModel::toggleTaskDone,
                        onEditBubble = viewModel::openEditTaskSheet,
                        onAddBubble = {
                            viewModel.openCreateTaskSheet(
                                defaultMode = TaskMode.TOP_BUBBLE,
                                forInbox = false
                            )
                        }
                    )
                }

                item {
                    NeoDashedDivider()
                }

                // 3. Structure Mode Filter & Quick Add Strip (All / Timeless Steps / Timed Slots)
                item {
                    FlowStructureFilterBar(
                        currentFilter = uiState.flowFilter,
                        onSelectFilter = viewModel::setFlowFilter,
                        onAddTimelessStep = {
                            viewModel.openCreateTaskSheet(
                                defaultMode = TaskMode.TIMELESS_STEP,
                                forInbox = false
                            )
                        },
                        onAddTimedBlock = {
                            viewModel.openCreateTaskSheet(
                                defaultMode = TaskMode.TIMED_BLOCK,
                                forInbox = false
                            )
                        }
                    )
                }

                // 4. Unified Timeless + Timed Vertical Capsule Spine
                if (uiState.spineFlowTasks.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No Tasks in This View Yet",
                                style = MaterialTheme.typography.displaySmall,
                                color = InkBlack
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Add a Timeless Step (so you know what's next without clock pressure), a Timed Slot, or load a Day Blueprint.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = InkMuted
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                NeoPillButton(
                                    text = "+ Timeless Step",
                                    selected = true,
                                    activeColor = NeoYellow,
                                    onClick = {
                                        viewModel.openCreateTaskSheet(
                                            defaultMode = TaskMode.TIMELESS_STEP,
                                            forInbox = false
                                        )
                                    }
                                )
                                NeoPillButton(
                                    text = "Load Day Blueprint",
                                    selected = false,
                                    onClick = {
                                        viewModel.setDestination(StruktDestination.BLUEPRINTS)
                                    }
                                )
                            }
                        }
                    }
                } else {
                    itemsIndexed(
                        items = uiState.spineFlowTasks,
                        key = { _, item -> item.task.id }
                    ) { index, item ->
                        val isUpNext = uiState.upNextTask?.task?.id == item.task.id

                        // Check if there is a free-time gap between the previous timed task and this timed task
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
                            canMoveUp = index > 0,
                            canMoveDown = index < uiState.spineFlowTasks.lastIndex,
                            onToggleComplete = { viewModel.toggleTaskDone(item.task) },
                            onToggleSubtask = viewModel::toggleSubtaskDone,
                            onAddQuickSubtask = { subTitle ->
                                viewModel.addQuickSubtask(item.task.id, subTitle)
                            },
                            onMoveUp = { viewModel.moveSpineTask(item.task.id, moveUp = true) },
                            onMoveDown = { viewModel.moveSpineTask(item.task.id, moveUp = false) },
                            onEditTask = { viewModel.openEditTaskSheet(item) }
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
            .size(58.dp)
            .drawBehind {
                drawCircle(
                    color = InkBlack,
                    radius = size.minDimension / 2f,
                    center = Offset(center.x + 3.5.dp.toPx(), center.y + 3.5.dp.toPx())
                )
            }
            .clip(CircleShape)
            .background(NeoCoral)
            .border(2.5.dp, InkBlack, CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .testTag("fab_add_task"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Task to Structure",
            tint = InkBlack,
            modifier = Modifier.size(28.dp)
        )
    }
}
