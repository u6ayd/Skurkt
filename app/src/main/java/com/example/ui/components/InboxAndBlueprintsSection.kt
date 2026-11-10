package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DayTemplateEntity
import com.example.data.SubtaskEntity
import com.example.data.TaskEntity
import com.example.data.TaskMode
import com.example.data.TaskWithSubtasks
import com.example.data.decodeSpecs
import com.example.ui.theme.InkBlack
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.InkMuted
import com.example.ui.theme.NeoCoral
import com.example.ui.theme.NeoMintGreen
import com.example.ui.theme.NeoRosePink
import com.example.ui.theme.NeoSkyBlue
import com.example.ui.theme.NeoTangerine
import com.example.ui.theme.NeoYellow
import com.example.ui.theme.PureWhiteSurface
import com.example.ui.theme.SoftSheetSurface
import com.example.ui.theme.neoColorForKey

/**
 * Tablet Left Sidebar & Standalone Inbox Pane (Directly inspired by Screenshot 5's left Inbox pane,
 * fused with Trackerly's 2.dp ink borders and hard offset shadows).
 */
@Composable
fun NeoInboxPane(
    inboxTasks: List<TaskWithSubtasks>,
    dayReadinessPercent: Int,
    completedCount: Int,
    totalCount: Int,
    onQuickAddInbox: (String) -> Unit,
    onOpenFullCreateForInbox: () -> Unit,
    onSendToDayTimeless: (TaskEntity) -> Unit,
    onEditInboxTask: (TaskWithSubtasks) -> Unit,
    modifier: Modifier = Modifier
) {
    var brainDumpText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Top Header: Structured-style Coral "Inbox" Pill + "+ Detail" button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .drawBehind {
                        drawRoundRect(
                            color = InkBlack,
                            topLeft = Offset(3.dp.toPx(), 3.dp.toPx()),
                            size = size,
                            cornerRadius = CornerRadius(50f, 50f)
                        )
                    }
                    .clip(RoundedCornerShape(50))
                    .background(NeoCoral)
                    .border(2.dp, InkBlack, RoundedCornerShape(50))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Inbox,
                    contentDescription = null,
                    tint = InkBlack,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Inbox (${inboxTasks.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = InkBlack
                )
            }

            NeoPillButton(
                text = "+ Detailed",
                onClick = onOpenFullCreateForInbox,
                selected = false,
                inactiveColor = PureWhiteSurface,
                shadowOffset = 2.dp,
                modifier = Modifier.testTag("inbox_detailed_add_button")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Mini Readiness Summary Strip (Inspired by Screenshot 1's yellow & blue stat cards)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NeoCard(
                modifier = Modifier.weight(1f),
                backgroundColor = NeoYellow,
                cornerRadius = 14.dp,
                shadowOffset = 3.dp,
                contentPadding = PaddingValues(10.dp)
            ) {
                Column {
                    Text(
                        text = "DAY FLOW",
                        style = MaterialTheme.typography.labelMedium.copy(color = InkBlack),
                        fontSize = 10.sp
                    )
                    Text(
                        text = "$dayReadinessPercent%",
                        style = MaterialTheme.typography.displaySmall,
                        color = InkBlack
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    NeoProgressBar(progress = dayReadinessPercent / 100f, height = 7.dp)
                }
            }

            NeoCard(
                modifier = Modifier.weight(1f),
                backgroundColor = NeoSkyBlue,
                cornerRadius = 14.dp,
                shadowOffset = 3.dp,
                contentPadding = PaddingValues(10.dp)
            ) {
                Column {
                    Text(
                        text = "STEPS DONE",
                        style = MaterialTheme.typography.labelMedium.copy(color = InkBlack),
                        fontSize = 10.sp
                    )
                    Text(
                        text = "$completedCount / $totalCount",
                        style = MaterialTheme.typography.displaySmall,
                        color = InkBlack
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Zero-Clock Ready",
                        style = MaterialTheme.typography.labelMedium.copy(color = InkCharcoal),
                        fontSize = 10.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Brain-Dump Quick Input Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = brainDumpText,
                onValueChange = { brainDumpText = it },
                placeholder = {
                    Text(
                        "Brain-dump task (no date needed)...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkMuted
                    )
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .weight(1f)
                    .background(PureWhiteSurface, RoundedCornerShape(12.dp))
                    .testTag("inbox_quick_input")
            )
            IconButton(
                onClick = {
                    if (brainDumpText.isNotBlank()) {
                        onQuickAddInbox(brainDumpText)
                        brainDumpText = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(NeoMintGreen)
                    .border(2.dp, InkBlack, RoundedCornerShape(12.dp))
                    .testTag("inbox_quick_submit_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add to Inbox",
                    tint = InkBlack
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Unscheduled Inbox Cards List (Matches Screenshot 5: "Do Laundry 1 hr +", "Do Homework 1 hr +")
        if (inboxTasks.isEmpty()) {
            NeoCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = PureWhiteSurface,
                contentPadding = PaddingValues(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Inbox Zero",
                        style = MaterialTheme.typography.displaySmall,
                        color = InkBlack
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Drop tasks here anytime without worrying about when to do them. Tap + on any card to slot it into your day's sequence.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkMuted
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                items(inboxTasks, key = { it.task.id }) { item ->
                    val task = item.task
                    val color = neoColorForKey(task.colorKey)
                    NeoCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = PureWhiteSurface,
                        cornerRadius = 16.dp,
                        shadowOffset = 3.dp,
                        onClick = { onEditInboxTask(item) },
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
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(color)
                                        .border(2.dp, InkBlack, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = iconForKey(task.iconKey),
                                        contentDescription = null,
                                        tint = InkBlack,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "${formatDurationBadge(task.durationMinutes)} • ${task.subjectTag}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = InkMuted
                                    )
                                    Text(
                                        text = task.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = InkBlack,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // "+" Button that immediately sends the Inbox task to the selected Day Flow (like Screenshot 5)
                            IconButton(
                                onClick = { onSendToDayTimeless(task) },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(color.copy(alpha = 0.35f))
                                    .border(2.dp, InkBlack, CircleShape)
                                    .testTag("schedule_inbox_task_${task.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Send to Selected Day Flow",
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

/**
 * Trackerly-Inspired Master Matrix, 7-Stage Conversion Funnel & Folder-Tabbed Day Blueprints
 * (Directly inspired by Screenshots 1, 2, 3, and 4).
 */
@Composable
fun BlueprintsAndMatrixScreen(
    allTasks: List<TaskWithSubtasks>,
    templates: List<DayTemplateEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onApplyTemplate: (DayTemplateEntity) -> Unit,
    onSaveCurrentDayAsTemplate: (String, String, String) -> Unit,
    onToggleSubtask: (SubtaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeSubTab by remember { mutableStateOf(0) } // 0 = Folder Blueprints, 1 = Master Matrix & Funnel
    var showSaveDialog by remember { mutableStateOf(false) }
    var customTitle by remember { mutableStateOf("") }
    var customCode by remember { mutableStateOf("FLOW 02") }

    val allSubtasks = allTasks.flatMap { it.subtasks }
    val doneSubtasks = allSubtasks.count { it.isCompleted }
    val totalSubtasks = allSubtasks.size.coerceAtLeast(1)
    val completedTasks = allTasks.count { it.task.isCompleted }
    val totalTasks = allTasks.size.coerceAtLeast(1)
    val overallReadiness = ((completedTasks * 100f) / totalTasks).toInt()
    val timelessCount = allTasks.count { it.task.modeEnum == TaskMode.TIMELESS_STEP }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp)
    ) {
        // 1. Master Readiness Header Card (Directly inspired by Screenshot 1: "Academic Master Tracker")
        item {
            NeoCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = PureWhiteSurface,
                cornerRadius = 22.dp,
                shadowOffset = 5.dp,
                contentPadding = PaddingValues(18.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(NeoSkyBlue)
                                    .border(2.dp, InkBlack, RoundedCornerShape(50))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "STRUKT MASTER OS 2026",
                                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
                                    color = InkBlack
                                )
                            }
                            Text(
                                text = "${templates.size} Blueprints • ${allTasks.size} Total Tasks",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = InkCharcoal
                            )
                        }

                        NeoPillButton(
                            text = "Save Today as Blueprint",
                            icon = Icons.Default.BookmarkAdd,
                            onClick = { showSaveDialog = !showSaveDialog },
                            selected = showSaveDialog,
                            activeColor = NeoYellow,
                            shadowOffset = 2.dp,
                            modifier = Modifier.testTag("save_day_blueprint_button")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Structure & Milestone Master Tracker",
                        style = MaterialTheme.typography.displayMedium,
                        color = InkBlack
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    NeoDashedDivider()
                    Spacer(modifier = Modifier.height(14.dp))

                    // 2x2 Grid of Candy Neo-Brutalist Stat Cards (Yellow, Sky Blue, Mint Green, Rose Pink from Screenshot 1)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        NeoCard(
                            modifier = Modifier.weight(1f),
                            backgroundColor = NeoYellow,
                            cornerRadius = 16.dp,
                            shadowOffset = 3.dp,
                            contentPadding = PaddingValues(14.dp)
                        ) {
                            Column {
                                Text(
                                    text = "OVERALL READINESS",
                                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 10.sp),
                                    color = InkBlack
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$overallReadiness%",
                                    style = MaterialTheme.typography.displayMedium,
                                    color = InkBlack
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                NeoProgressBar(progress = overallReadiness / 100f)
                            }
                        }

                        NeoCard(
                            modifier = Modifier.weight(1f),
                            backgroundColor = NeoSkyBlue,
                            cornerRadius = 16.dp,
                            shadowOffset = 3.dp,
                            contentPadding = PaddingValues(14.dp)
                        ) {
                            Column {
                                Text(
                                    text = "MILESTONES DONE",
                                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 10.sp),
                                    color = InkBlack
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$doneSubtasks / ${allSubtasks.size}",
                                    style = MaterialTheme.typography.displayMedium,
                                    color = InkBlack
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Across ${allTasks.size} Active Tasks",
                                    style = MaterialTheme.typography.labelMedium.copy(color = InkCharcoal)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        NeoCard(
                            modifier = Modifier.weight(1f),
                            backgroundColor = NeoMintGreen,
                            cornerRadius = 16.dp,
                            shadowOffset = 3.dp,
                            contentPadding = PaddingValues(14.dp)
                        ) {
                            Column {
                                Text(
                                    text = "TIMELESS QUEUE STEPS",
                                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 10.sp),
                                    color = InkBlack
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$timelessCount Steps",
                                    style = MaterialTheme.typography.displayMedium,
                                    color = InkBlack
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Zero Clock Pressure",
                                    style = MaterialTheme.typography.labelMedium.copy(color = InkCharcoal)
                                )
                            }
                        }

                        NeoCard(
                            modifier = Modifier.weight(1f),
                            backgroundColor = NeoRosePink,
                            cornerRadius = 16.dp,
                            shadowOffset = 3.dp,
                            contentPadding = PaddingValues(14.dp)
                        ) {
                            Column {
                                Text(
                                    text = "COMPLETED BLOCKS",
                                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 10.sp),
                                    color = InkBlack
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$completedTasks / ${allTasks.size}",
                                    style = MaterialTheme.typography.displayMedium,
                                    color = InkBlack
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "All Subtasks Tracked",
                                    style = MaterialTheme.typography.labelMedium.copy(color = InkCharcoal)
                                )
                            }
                        }
                    }

                    if (showSaveDialog) {
                        Spacer(modifier = Modifier.height(14.dp))
                        NeoCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = SoftSheetSurface,
                            cornerRadius = 14.dp,
                            shadowOffset = 2.dp,
                            contentPadding = PaddingValues(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Save Selected Day's Flow as a Folder Blueprint",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = InkBlack
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = customCode,
                                        onValueChange = { customCode = it },
                                        label = { Text("Tab Code") },
                                        singleLine = true,
                                        modifier = Modifier.width(120.dp)
                                    )
                                    OutlinedTextField(
                                        value = customTitle,
                                        onValueChange = { customTitle = it },
                                        label = { Text("Blueprint Title") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                NeoPillButton(
                                    text = "Save Blueprint Folder",
                                    selected = true,
                                    activeColor = NeoMintGreen,
                                    onClick = {
                                        onSaveCurrentDayAsTemplate(
                                            customTitle.ifBlank { "Custom Flow Day" },
                                            customCode.ifBlank { "FLOW 99" },
                                            "SKY"
                                        )
                                        customTitle = ""
                                        showSaveDialog = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Mode Switcher Pills (Matches Screenshot 1: "Courses & Folders" vs "Master Matrix & Funnel")
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NeoPillButton(
                    text = "Day Blueprints & Folders",
                    icon = Icons.Default.FolderOpen,
                    selected = activeSubTab == 0,
                    activeColor = NeoYellow,
                    onClick = { activeSubTab = 0 },
                    modifier = Modifier.testTag("tab_blueprints_folders")
                )
                NeoPillButton(
                    text = "Master Matrix & Funnel",
                    icon = Icons.Default.Insights,
                    selected = activeSubTab == 1,
                    activeColor = NeoYellow,
                    onClick = { activeSubTab = 1 },
                    modifier = Modifier.testTag("tab_master_matrix")
                )
            }
        }

        // Search Bar (Matches Screenshot 1 & 2 rounded Neo-Brutalist search field)
        item {
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
                        "Search blueprints, subjects, or task milestones (e.g. Physics, Chemistry, Deep Work)...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkMuted
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PureWhiteSurface, RoundedCornerShape(50))
            )
        }

        if (activeSubTab == 0) {
            // Folder-Tabbed Blueprint Cards (Matches Screenshot 2: "PHY 12 Physics", "CHEM 12 Chemistry", etc.)
            item {
                Text(
                    text = "Reusable Day Blueprints",
                    style = MaterialTheme.typography.displayMedium,
                    color = InkBlack
                )
                Text(
                    text = "Tap any folder card to load its timeless & timed sequence directly into your selected day.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkMuted
                )
            }

            val filteredTemplates = templates.filter {
                searchQuery.isBlank() ||
                    it.title.contains(searchQuery, ignoreCase = true) ||
                    it.tabCode.contains(searchQuery, ignoreCase = true) ||
                    it.tagLabel.contains(searchQuery, ignoreCase = true)
            }

            items(filteredTemplates, key = { it.id }) { tmpl ->
                val specs = tmpl.decodeSpecs()
                NeoFolderCard(
                    tabCode = tmpl.tabCode,
                    title = tmpl.title,
                    subtitle = tmpl.subtitle,
                    progressPercent = overallReadiness,
                    tagLabel = tmpl.tagLabel,
                    actionLabel = "LOAD ${specs.size} STEPS INTO DAY",
                    fillColor = neoColorForKey(tmpl.colorKey),
                    onCardClick = { onApplyTemplate(tmpl) },
                    modifier = Modifier.testTag("blueprint_card_${tmpl.id}")
                )
            }
        } else {
            // 7-Stage Conversion Funnel & Interactive Milestone Matrix (Matches Screenshots 3 & 4)
            item {
                NeoCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = PureWhiteSurface,
                    cornerRadius = 20.dp,
                    shadowOffset = 4.dp,
                    contentPadding = PaddingValues(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Milestone Conversion Funnel",
                                    style = MaterialTheme.typography.displaySmall,
                                    color = InkBlack
                                )
                                Text(
                                    text = "Tracking subtask & stage completion across your structured tasks",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = InkMuted
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(NeoRosePink.copy(alpha = 0.4f))
                                    .border(1.5.dp, InkBlack, RoundedCornerShape(50))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Master Pipeline",
                                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
                                    color = InkBlack
                                )
                            }
                        }

                        val stageLabels = listOf(
                            "1" to "Timeless Steps Completed",
                            "2" to "Timed Schedule Anchors Done",
                            "3" to "Subtask Milestones Checked",
                            "4" to "Daily Routine Bubbles Done"
                        )
                        stageLabels.forEachIndexed { idx, (num, label) ->
                            val ratio = when (idx) {
                                0 -> {
                                    val list = allTasks.filter { it.task.modeEnum == TaskMode.TIMELESS_STEP }
                                    if (list.isEmpty()) 0f else list.count { it.task.isCompleted }.toFloat() / list.size
                                }
                                1 -> {
                                    val list = allTasks.filter { it.task.modeEnum == TaskMode.TIMED_BLOCK }
                                    if (list.isEmpty()) 0f else list.count { it.task.isCompleted }.toFloat() / list.size
                                }
                                2 -> doneSubtasks.toFloat() / totalSubtasks
                                else -> {
                                    val list = allTasks.filter { it.task.modeEnum == TaskMode.TOP_BUBBLE }
                                    if (list.isEmpty()) 0f else list.count { it.task.isCompleted }.toFloat() / list.size
                                }
                            }
                            val pct = (ratio * 100).toInt()
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(InkBlack),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = num,
                                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
                                                color = PureWhiteSurface
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = InkBlack
                                        )
                                    }
                                    Text(
                                        text = "$pct%",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = InkCharcoal
                                    )
                                }
                                NeoProgressBar(progress = ratio, fillColor = InkBlack)
                            }
                        }
                    }
                }
            }

            // Interactive Chapter / Task Milestone Matrix (Inspired by Screenshot 3)
            val tasksWithSubs = allTasks.filter {
                it.subtasks.isNotEmpty() && (
                    searchQuery.isBlank() ||
                        it.task.title.contains(searchQuery, ignoreCase = true) ||
                        it.task.subjectTag.contains(searchQuery, ignoreCase = true)
                    )
            }
            items(tasksWithSubs, key = { it.task.id }) { item ->
                val t = item.task
                val fill = neoColorForKey(t.colorKey)
                NeoCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = PureWhiteSurface,
                    cornerRadius = 18.dp,
                    shadowOffset = 4.dp,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Colored Header Band (like Screenshot 3's sky-blue "PHY 12 Physics" header)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(fill)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(PureWhiteSurface)
                                        .border(2.dp, InkBlack, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = t.subjectTag,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = InkBlack
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = t.title,
                                        style = MaterialTheme.typography.displaySmall.copy(fontSize = 19.sp),
                                        color = InkBlack
                                    )
                                    Text(
                                        text = "${item.subtasks.count { it.isCompleted }} of ${item.subtasks.size} Milestones Completed",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = InkCharcoal
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(InkBlack)
                        )

                        // Subtask Checkboxes Row/List
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item.sortedSubtasks.forEachIndexed { idx, sub ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onToggleSubtask(sub) },
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "${idx + 1}",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = InkMuted,
                                            modifier = Modifier.width(24.dp)
                                        )
                                        Text(
                                            text = sub.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = InkBlack
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = sub.stageTag.uppercase(),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = InkMuted
                                        )
                                        NeoSquareCheckbox(
                                            checked = sub.isCompleted,
                                            onCheckedChange = { onToggleSubtask(sub) },
                                            accentColor = fill
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
}
