package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.FolderEntity
import com.example.data.TaskEntity
import com.example.data.TaskMode
import com.example.data.TaskWithSubtasks
import com.example.ui.theme.InkBlack
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.InkMuted
import com.example.ui.theme.NeoCoral
import com.example.ui.theme.NeoMintGreen
import com.example.ui.theme.NeoPalette
import com.example.ui.theme.NeoRosePink
import com.example.ui.theme.NeoSkyBlue
import com.example.ui.theme.NeoYellow
import com.example.ui.theme.PureWhiteSurface
import com.example.ui.theme.SoftSheetSurface
import com.example.ui.theme.neoColorForKey
import com.example.ui.theme.sdp
import com.example.ui.theme.ssp

@Composable
fun NeoInboxPane(
    inboxTasks: List<TaskWithSubtasks>,
    dayReadinessPercent: Int,
    completedCount: Int,
    totalCount: Int,
    onQuickAddInbox: (String) -> Unit,
    onOpenSchedulePopup: (TaskEntity) -> Unit,
    onEditInboxTask: (TaskWithSubtasks) -> Unit,
    isMultiSelectActive: Boolean = false,
    selectedTaskIds: Set<Long> = emptySet(),
    onToggleMultiSelectMode: () -> Unit = {},
    onToggleSelectTask: (Long) -> Unit = {},
    onEnterMultiSelectWithTask: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var brainDumpText by remember { mutableStateOf("") }
    val uniformCorner = RoundedCornerShape(18.dp)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(NeoCoral)
                    .border(1.5.dp, InkBlack, RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Inbox,
                    contentDescription = null,
                    tint = PureWhiteSurface,
                    modifier = Modifier.size(18.sdp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Inbox",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = PureWhiteSurface
                )
            }

            if (inboxTasks.isNotEmpty() || isMultiSelectActive) {
                NeoPillButton(
                    text = if (isMultiSelectActive) "Done" else "Select",
                    icon = Icons.Default.Checklist,
                    selected = isMultiSelectActive,
                    activeColor = NeoYellow,
                    onClick = onToggleMultiSelectMode,
                    modifier = Modifier.testTag("inbox_multi_select_toggle")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            NeoCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                backgroundColor = NeoYellow,
                cornerRadius = 18.dp,
                contentPadding = PaddingValues(14.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "DAY FLOW",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                        color = InkBlack
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$dayReadinessPercent%",
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                        color = InkBlack
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    NeoProgressBar(progress = dayReadinessPercent / 100f, height = 6.dp)
                }
            }

            NeoCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                backgroundColor = NeoSkyBlue,
                cornerRadius = 18.dp,
                contentPadding = PaddingValues(14.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "STEPS DONE",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                        color = InkBlack
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$completedCount / $totalCount",
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                        color = InkBlack
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    NeoProgressBar(
                        progress = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount,
                        height = 6.dp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.sdp)
                    .clip(uniformCorner)
                    .background(PureWhiteSurface, uniformCorner)
                    .border(1.5.dp, InkBlack, uniformCorner)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                OutlinedTextField(
                    value = brainDumpText,
                    onValueChange = { brainDumpText = it },
                    placeholder = {
                        Text(
                            "Brain dump",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal),
                            color = InkMuted
                        )
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    shape = uniformCorner,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                        focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("inbox_quick_input")
                )
            }

            Box(
                modifier = Modifier
                    .size(52.sdp)
                    .clip(uniformCorner)
                    .background(NeoMintGreen, uniformCorner)
                    .border(1.5.dp, InkBlack, uniformCorner)
                    .clickable {
                        if (brainDumpText.isNotBlank()) {
                            onQuickAddInbox(brainDumpText)
                            brainDumpText = ""
                        }
                    }
                    .testTag("inbox_quick_submit_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = InkBlack,
                    modifier = Modifier.size(24.sdp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(
                items = inboxTasks,
                key = { it.task.id },
                contentType = { "inbox_task" }
            ) { item ->
                val task = item.task
                val color = neoColorForKey(task.colorKey)
                val isSelected = task.id in selectedTaskIds
                NeoCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .smoothCombinedClickable(
                            role = Role.Button,
                            onLongClick = { onEnterMultiSelectWithTask(task.id) },
                            onClick = {
                                if (isMultiSelectActive) {
                                    onToggleSelectTask(task.id)
                                } else {
                                    onEditInboxTask(item)
                                }
                            }
                        ),
                    backgroundColor = if (isSelected) NeoYellow else PureWhiteSurface,
                    cornerRadius = 18.dp,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) PureWhiteSurface else color)
                                    .border(1.5.dp, InkBlack, RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.Check else iconForKey(task.iconKey),
                                    contentDescription = null,
                                    tint = InkBlack,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = formatDurationBadge(task.durationMinutes),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Normal),
                                    color = InkMuted
                                )
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        lineHeight = 19.ssp
                                    ),
                                    color = InkBlack,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        if (isMultiSelectActive) {
                            NeoCircleCheckbox(
                                checked = isSelected,
                                accentColor = NeoMintGreen,
                                onCheckedChange = { onToggleSelectTask(task.id) }
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SoftSheetSurface)
                                    .border(1.5.dp, InkBlack, RoundedCornerShape(12.dp))
                                    .clickable { onOpenSchedulePopup(task) }
                                    .testTag("schedule_inbox_task_${task.id}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Schedule into Flow",
                                    tint = InkBlack,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleInboxMiniPopup(
    task: TaskEntity,
    onDismiss: () -> Unit,
    onConfirmSchedule: (asTimed: Boolean, startTimeMinutes: Int?, durationMinutes: Int) -> Unit
) {
    val clockConfig = LocalClockFormat.current
    var selectedMode by remember(task.id) { mutableStateOf(TaskMode.TIMELESS_STEP) }
    var startTimeMin by remember(task.id) {
        val defaultStart = (clockConfig.startOfDayMinutes + 60).coerceIn(0, 22 * 60)
        mutableIntStateOf(task.startTimeMinutes ?: defaultStart)
    }
    var durationMin by remember(task.id) {
        mutableIntStateOf(0)
    }
    var errorBanner by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        NeoCard(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth(),
            backgroundColor = PureWhiteSurface,
            cornerRadius = 22.dp,
            contentPadding = PaddingValues(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Schedule into Flow",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = InkBlack
                        )
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = InkMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = InkBlack,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(20.dp)
                            .smoothPressClickable(role = Role.Button, onClick = onDismiss)
                    )
                }

                UnifiedTimeAndDurationPickerSection(
                    toInbox = false,
                    taskMode = selectedMode,
                    startTimeMinutes = startTimeMin,
                    durationMinutes = durationMin,
                    showBubbleOption = false,
                    onModeChange = { newMode ->
                        errorBanner = null
                        selectedMode = newMode
                        if (newMode == TaskMode.TIMELESS_STEP) {
                            durationMin = 0
                        } else if (newMode == TaskMode.TIMED_BLOCK && durationMin <= 0) {
                            durationMin = 60
                        }
                    },
                    onStartTimeChange = {
                        errorBanner = null
                        startTimeMin = it
                    },
                    onDurationChange = {
                        errorBanner = null
                        durationMin = it
                    }
                )

                errorBanner?.let { msg ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NeoYellow)
                            .border(1.5.dp, InkBlack, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = InkBlack
                        )
                    }
                }

                NeoPillButton(
                    text = "Schedule into Day Flow",
                    selected = true,
                    activeColor = NeoMintGreen,
                    onClick = {
                        val isTimed = selectedMode == TaskMode.TIMED_BLOCK
                        val finalDur = if (isTimed) durationMin.coerceIn(5, 720) else durationMin.coerceIn(0, 720)
                        if (isTimed && (startTimeMin + finalDur) > clockConfig.endOfDayMinutes) {
                            errorBanner =
                                "Task ends past End of Day (${formatMinutesAsClock(clockConfig.endOfDayMinutes, clockConfig.use24Hour)}). Adjust time or Settings."
                            return@NeoPillButton
                        }
                        onConfirmSchedule(
                            isTimed,
                            if (isTimed) startTimeMin else null,
                            finalDur
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun FoldersAndSectionsScreen(
    allTasks: List<TaskWithSubtasks>,
    folders: List<FolderEntity>,
    selectedFolderId: Long?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSelectFolder: (Long?) -> Unit,
    onCreateFolder: (String, String, String, String, List<String>) -> Unit,
    onAddSectionToFolder: (FolderEntity, String) -> Unit,
    onCreateTaskInFolderSection: (folderName: String, sectionName: String) -> Unit = { _, _ -> },
    onOpenCleanupExport: () -> Unit = {},
    isMultiSelectActive: Boolean = false,
    selectedTaskIds: Set<Long> = emptySet(),
    onToggleMultiSelectMode: () -> Unit = {},
    onToggleSelectTask: (Long) -> Unit = {},
    onEnterMultiSelectWithTask: (Long) -> Unit = {},
    onTaskClick: (TaskWithSubtasks) -> Unit,
    onToggleTaskDone: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateFolderPopup by remember { mutableStateOf(false) }
    var showAddSectionPopup by remember { mutableStateOf(false) }

    val (doneSubtasks, totalSubtasksCount, completedTasks, overallReadiness) = remember(allTasks) {
        var doneSubs = 0
        var totalSubs = 0
        var doneTasks = 0
        for (item in allTasks) {
            doneSubs += item.completedSubtasksCount
            totalSubs += item.subtasks.size
            if (item.task.isCompleted) doneTasks++
        }
        val total = allTasks.size.coerceAtLeast(1)
        val readiness = ((doneTasks * 100f) / total).toInt()
        listOf(doneSubs, totalSubs, doneTasks, readiness)
    }

    val tasksByFolderKey = remember(allTasks) {
        allTasks.groupBy { it.task.folderName.trim().lowercase() }
    }

    val folderStatsByKey = remember(tasksByFolderKey) {
        tasksByFolderKey.mapValues { (_, list) ->
            val done = list.count { it.task.isCompleted }
            val pct = if (list.isEmpty()) 0 else ((done * 100f) / list.size).toInt()
            Triple(list.size, done, pct)
        }
    }

    val selectedFolder = remember(folders, selectedFolderId) {
        folders.firstOrNull { it.id == selectedFolderId }
    }

    val filteredFolders = remember(folders, searchQuery) {
        if (searchQuery.isBlank()) {
            folders
        } else {
            folders.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                    it.codeTag.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val selectedFolderTasks = remember(selectedFolder, tasksByFolderKey) {
        selectedFolder?.let { tasksByFolderKey[it.name.trim().lowercase()].orEmpty() }.orEmpty()
    }

    val selectedFolderSectionsList = remember(selectedFolder, selectedFolderTasks) {
        if (selectedFolder == null) {
            emptyList()
        } else {
            (selectedFolder.sections + selectedFolderTasks.map { it.task.sectionName })
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .distinct()
        }
    }

    val selectedSectionTasksByKey = remember(selectedFolderTasks) {
        selectedFolderTasks.groupBy { it.task.sectionName.trim().lowercase() }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 100.dp)
        ) {
            if (selectedFolder == null) {
                item(key = "folders_stat_grid", contentType = "stat_grid") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            NeoCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(94.sdp),
                                backgroundColor = NeoYellow,
                                cornerRadius = 18.dp,
                                contentPadding = PaddingValues(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "OVERALL READINESS",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontSize = 10.3.ssp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = InkBlack
                                    )
                                    Text(
                                        text = "$overallReadiness%",
                                        style = MaterialTheme.typography.displayMedium.copy(
                                            fontSize = 26.6.ssp,
                                            lineHeight = 32.5.ssp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = InkBlack
                                    )
                                }
                            }

                            NeoCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(94.sdp),
                                backgroundColor = NeoSkyBlue,
                                cornerRadius = 18.dp,
                                contentPadding = PaddingValues(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "MILESTONES DONE",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontSize = 10.3.ssp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = InkBlack
                                    )
                                    Text(
                                        text = "$doneSubtasks / $totalSubtasksCount",
                                        style = MaterialTheme.typography.displayMedium.copy(
                                            fontSize = 26.6.ssp,
                                            lineHeight = 32.5.ssp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = InkBlack
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            NeoCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(94.sdp),
                                backgroundColor = NeoMintGreen,
                                cornerRadius = 18.dp,
                                contentPadding = PaddingValues(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "TASKS COMPLETED",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontSize = 10.3.ssp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = InkBlack
                                    )
                                    Text(
                                        text = "$completedTasks / ${allTasks.size}",
                                        style = MaterialTheme.typography.displayMedium.copy(
                                            fontSize = 26.6.ssp,
                                            lineHeight = 32.5.ssp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = InkBlack
                                    )
                                }
                            }

                            NeoCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(94.sdp),
                                backgroundColor = NeoRosePink,
                                cornerRadius = 18.dp,
                                contentPadding = PaddingValues(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "FOLDERS",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontSize = 10.3.ssp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = InkBlack
                                    )
                                    Text(
                                        text = "${folders.size}",
                                        style = MaterialTheme.typography.displayMedium.copy(
                                            fontSize = 26.6.ssp,
                                            lineHeight = 32.5.ssp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = InkBlack
                                    )
                                }
                            }
                        }
                    }
                }

                item(key = "folders_search_bar", contentType = "search_bar") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = onSearchQueryChange,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = InkBlack
                                    )
                                },
                                placeholder = {
                                    Text(
                                        "Search...",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 13.4.ssp,
                                            fontWeight = FontWeight.Normal
                                        ),
                                        color = InkMuted
                                    )
                                },
                                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.4.ssp),
                                singleLine = true,
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .background(PureWhiteSurface, RoundedCornerShape(18.dp))
                            )

                            NeoPillButton(
                                text = "+ Folder",
                                icon = Icons.Default.CreateNewFolder,
                                selected = true,
                                activeColor = NeoYellow,
                                onClick = { showCreateFolderPopup = true },
                                modifier = Modifier.testTag("create_folder_button")
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            NeoPillButton(
                                text = "Cleanup & Export (PDF / CSV)",
                                icon = Icons.Default.Description,
                                selected = false,
                                onClick = onOpenCleanupExport,
                                modifier = Modifier.testTag("open_cleanup_export_button")
                            )
                        }
                    }
                }

                items(
                    items = filteredFolders,
                    key = { it.id },
                    contentType = { "folder_card" }
                ) { folder ->
                    val (folderTaskCount, _, folderPct) = folderStatsByKey[folder.name.trim().lowercase()]
                        ?: Triple(0, 0, 0)

                    NeoFolderCard(
                        tabCode = folder.codeTag,
                        title = folder.name,
                        subtitle = "${folder.sections.size} Sections • $folderTaskCount Tasks",
                        progressPercent = folderPct,
                        fillColor = neoColorForKey(folder.colorKey),
                        onCardClick = { onSelectFolder(folder.id) },
                        modifier = Modifier
                            .testTag("folder_card_${folder.id}")
                    )
                }
            } else {
                val (folderTaskCount, folderDone, folderPct) = folderStatsByKey[selectedFolder.name.trim().lowercase()]
                    ?: Triple(0, 0, 0)
                val accent = neoColorForKey(selectedFolder.colorKey)
                val defaultSection = selectedFolderSectionsList.firstOrNull()
                    ?: selectedFolder.sections.firstOrNull()
                    ?: "Core"

                item(key = "selected_folder_header_${selectedFolder.id}", contentType = "folder_header") {
                    NeoCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = accent,
                        cornerRadius = 22.dp,
                        contentPadding = PaddingValues(18.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                NeoPillButton(
                                    text = "Folders",
                                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                                    onClick = { onSelectFolder(null) }
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(InkBlack)
                                        .padding(horizontal = 11.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = selectedFolder.codeTag.uppercase(),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                                        color = PureWhiteSurface
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (selectedFolderTasks.isNotEmpty() || isMultiSelectActive) {
                                        NeoPillButton(
                                            text = if (isMultiSelectActive) "Done" else "Select",
                                            icon = Icons.Default.Checklist,
                                            selected = isMultiSelectActive,
                                            activeColor = NeoYellow,
                                            onClick = onToggleMultiSelectMode
                                        )
                                    }
                                    NeoPillButton(
                                        text = "+ Section",
                                        selected = true,
                                        activeColor = NeoYellow,
                                        onClick = { showAddSectionPopup = true }
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = selectedFolder.name,
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontSize = 18.5.ssp,
                                            lineHeight = 24.ssp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = InkBlack
                                    )
                                    Text(
                                        text = "$folderDone / $folderTaskCount tasks completed",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontSize = 11.4.ssp,
                                            lineHeight = 15.5.ssp,
                                            fontWeight = FontWeight.Normal
                                        ),
                                        color = InkCharcoal
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    NeoPillButton(
                                        text = "+ Add Task",
                                        icon = Icons.Default.Add,
                                        selected = true,
                                        activeColor = PureWhiteSurface,
                                        onClick = {
                                            onCreateTaskInFolderSection(selectedFolder.name, defaultSection)
                                        },
                                        modifier = Modifier.testTag("folder_header_add_task_${selectedFolder.id}")
                                    )

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(PureWhiteSurface)
                                            .border(1.5.dp, InkBlack, RoundedCornerShape(50))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "$folderPct%",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontSize = 12.4.ssp,
                                                fontWeight = FontWeight.Medium
                                            ),
                                            color = InkBlack
                                        )
                                    }
                                }
                            }

                            NeoProgressBar(progress = folderPct / 100f)
                        }
                    }
                }

                items(
                    items = selectedFolderSectionsList,
                    key = { "section_${selectedFolder.id}_$it" },
                    contentType = { "folder_section" }
                ) { sectionName ->
                    val sectionTasks = selectedSectionTasksByKey[sectionName.trim().lowercase()].orEmpty()

                    NeoCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = PureWhiteSurface,
                        cornerRadius = 20.dp,
                        contentPadding = PaddingValues(18.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(9.dp))
                                            .background(accent)
                                            .border(1.5.dp, InkBlack, RoundedCornerShape(9.dp))
                                            .padding(horizontal = 12.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = sectionName,
                                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.4.ssp),
                                            color = InkBlack
                                        )
                                    }
                                    Text(
                                        text = "${sectionTasks.count { it.task.isCompleted }}/${sectionTasks.size} done",
                                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.4.ssp),
                                        color = InkMuted
                                    )
                                }

                                NeoPillButton(
                                    text = "+ Add Task",
                                    icon = Icons.Default.Add,
                                    selected = true,
                                    activeColor = NeoYellow,
                                    onClick = {
                                        onCreateTaskInFolderSection(selectedFolder.name, sectionName)
                                    },
                                    modifier = Modifier.testTag("section_add_task_${sectionName.lowercase()}")
                                )
                            }

                            if (sectionTasks.isEmpty()) {
                                Text(
                                    text = "No tasks in this section yet. Tap '+ Add Task' to create one.",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.4.ssp),
                                    color = InkMuted
                                )
                            } else {
                                sectionTasks.forEach { item ->
                                    val t = item.task
                                    val isSelected = t.id in selectedTaskIds
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(if (isSelected) NeoYellow else SoftSheetSurface)
                                            .border(
                                                if (isSelected) 1.5.dp else 1.dp,
                                                if (isSelected) InkBlack else InkBlack.copy(alpha = 0.12f),
                                                RoundedCornerShape(14.dp)
                                            )
                                            .smoothCombinedClickable(
                                                role = Role.Button,
                                                onLongClick = { onEnterMultiSelectWithTask(t.id) },
                                                onClick = {
                                                    if (isMultiSelectActive) {
                                                        onToggleSelectTask(t.id)
                                                    } else {
                                                        onTaskClick(item)
                                                    }
                                                }
                                            )
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = t.title,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontSize = 15.4.ssp,
                                                    lineHeight = 20.ssp,
                                                    textDecoration = if (t.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                                ),
                                                color = if (t.isCompleted) InkMuted else InkBlack,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (item.subtasks.isNotEmpty()) {
                                                Text(
                                                    text = "${item.completedSubtasksCount}/${item.subtasks.size} subtasks",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.4.ssp),
                                                    color = InkMuted
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        NeoCircleCheckbox(
                                            checked = if (isMultiSelectActive) isSelected else t.isCompleted,
                                            accentColor = if (isMultiSelectActive) NeoMintGreen else accent,
                                            onCheckedChange = {
                                                if (isMultiSelectActive) {
                                                    onToggleSelectTask(t.id)
                                                } else {
                                                    onToggleTaskDone(t)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateFolderPopup) {
        CreateFolderPopupModal(
            onDismiss = { showCreateFolderPopup = false },
            onCreate = { name, code, colorKey, sections ->
                onCreateFolder(name, code, colorKey, "BOOK", sections)
                showCreateFolderPopup = false
            }
        )
    }

    if (showAddSectionPopup && selectedFolder != null) {
        AddSectionPopupModal(
            folderName = selectedFolder.name,
            onDismiss = { showAddSectionPopup = false },
            onAdd = { secName ->
                onAddSectionToFolder(selectedFolder, secName)
                showAddSectionPopup = false
            }
        )
    }
}

@Composable
private fun CreateFolderPopupModal(
    onDismiss: () -> Unit,
    onCreate: (name: String, code: String, colorKey: String, sections: List<String>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var colorKey by remember { mutableStateOf("SKY") }
    var sectionsInput by remember { mutableStateOf("Chapters, Practice, Milestones") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        NeoCard(
            modifier = Modifier
                .widthIn(max = 380.dp)
                .fillMaxWidth(),
            backgroundColor = PureWhiteSurface,
            cornerRadius = 20.dp,
            contentPadding = PaddingValues(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "New Folder",
                    style = MaterialTheme.typography.titleLarge,
                    color = InkBlack
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Folder name (e.g. Physics)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    placeholder = { Text("Code (e.g. PHY 12)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = sectionsInput,
                    onValueChange = { sectionsInput = it },
                    placeholder = { Text("Sections (comma separated)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NeoPalette.forEach { opt ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(opt.fill)
                                .border(if (colorKey == opt.key) 2.5.dp else 1.dp, InkBlack, CircleShape)
                                .clickable { colorKey = opt.key }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NeoPillButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                    NeoPillButton(
                        text = "Create",
                        selected = true,
                        activeColor = NeoMintGreen,
                        onClick = {
                            if (name.isNotBlank()) {
                                val secs = sectionsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                onCreate(name, code, colorKey, secs)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddSectionPopupModal(
    folderName: String,
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var sectionName by remember { mutableStateOf("") }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        NeoCard(
            modifier = Modifier
                .widthIn(max = 340.dp)
                .fillMaxWidth(),
            backgroundColor = PureWhiteSurface,
            cornerRadius = 20.dp,
            contentPadding = PaddingValues(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Add Section to $folderName",
                    style = MaterialTheme.typography.titleLarge,
                    color = InkBlack
                )
                OutlinedTextField(
                    value = sectionName,
                    onValueChange = { sectionName = it },
                    placeholder = { Text("Section name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                NeoPillButton(
                    text = "Add Section",
                    selected = true,
                    activeColor = NeoYellow,
                    onClick = {
                        if (sectionName.isNotBlank()) onAdd(sectionName)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
