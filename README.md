# Strukt ⚡

> **A Soft Neo-Brutalist Visual Day & Sequence Planner for Android**  
> Inspired by **Structured** and **Learnify**, engineered in Kotlin & Jetpack Compose for both handheld phones and 12.1-inch tablets (**Redmi Pad Pro 5G**).

---

## 📖 Table of Contents

1. [Overview & Core Philosophy](#-overview--core-philosophy)
2. [Project Directory & File-by-File Architecture](#-project-directory--file-by-file-architecture)
3. [Database Schema & Data Model (Room SQLite)](#-database-schema--data-model-room-sqlite)
4. [Timeline Spine & Sequence Ordering Logic](#-timeline-spine--sequence-ordering-logic)
5. [Responsive & Adaptive Multi-Device Architecture](#-responsive--adaptive-multi-device-architecture)
6. [Design System: Color Palette & Typography Hierarchy](#-design-system-color-palette--typography-hierarchy)
7. [Screen-by-Screen Guide & User Flows](#-screen-by-screen-guide--user-flows)
8. [Building, Testing & CI/CD](#-building-testing--cicd)

---

## 🎯 Overview & Core Philosophy

**Strukt** combines two distinct productivity paradigms into a single cohesive visual interface:

1. **Structured-Style Visual Day Flow**:
   - A continuous vertical timeline spine that unifies **Timed Blocks** (anchored to clock intervals like `10:00 – 11:00`) and **Timeless Workflow Steps** (ordered by sequence rank like `#1`, `#3`, `#6` without forcing artificial clock deadlines).
   - **Top Daily Bubbles** (*Structure Tomorrow*, *Water Plants*, *Call Mum*) sit above the timeline spine for recurring or anytime habits.
   - **Halved Capsules & Dotted Break Loops**: When a task is paused for a break (either live from the Focus Timer or manually via the 1-tap *Split Break Loop* toggle), its vertical oval capsule splits into a **top half-oval** and **bottom half-oval** connected by a smooth cubic-Bezier roller-coaster dotted loop.

2. **Learnify-Inspired Subject Folders & Sections**:
   - Tasks are organized into color-coded **Folders** (*Physics*, *Chemistry*, *Mathematics*, *Reading*, *Health & Sport*) and granular **Sections** (e.g., *Electrostatics*, *Optics*, *Organic*, *Calculus*).
   - Summary stat cards track **Overall Readiness (%)**, **Milestones Done (Subtasks)**, **Tasks Completed**, and **Active Folders**.

---

## 🗂 Project Directory & File-by-File Architecture

```text
app/src/main/
├── AndroidManifest.xml                  # App configuration, fullSensor orientation support, adaptive icon
├── java/com/example/
│   ├── MainActivity.kt                  # Edge-to-edge setup, Orientation-Driven Layout switch, Root Navigation
│   ├── data/
│   │   ├── PlannerEntities.kt           # Room Entities (TaskEntity, SubtaskEntity, FolderEntity), Enums & Relations
│   │   ├── PlannerDao.kt                # Reactive Room DAOs returning Kotlin Flows + transactional queries
│   │   ├── PlannerDatabase.kt           # Singleton Room database (version 4) with initial seed data
│   │   └── PlannerRepository.kt         # Data repository mediating all CRUD, scheduling, and session operations
│   └── ui/
│       ├── PlannerViewModel.kt          # MVVM StateFlow holder, sequence sorting, recurrence & timer logic
│       ├── components/
│       │   ├── NeoComponents.kt         # Core Neo-Brutalist primitives (NeoCard, NeoFolderCard, NeoPillButton, etc.)
│       │   ├── TimelineFlowSection.kt   # Week pager, Top Bubbles, Spine Ovals, Dotted Loop Canvas, Task Popup Modal
│       │   ├── InboxAndBlueprintsSection.kt # Brain-dump Inbox pane, Schedule mini-popup, Folders & Sections screen
│       │   └── TaskEditorAndFocusModal.kt   # Progressive 5-row Pencil Task Editor, Sub-Popups, Focus Timer screen
│       └── theme/
│           ├── Color.kt                 # Soft Neo-Brutalist pastel palette, ink borders, color-key lookup helpers
│           ├── Type.kt                  # Variable fonts (Outfit & JetBrains Mono), weight axes, .sdp/.ssp scaling
│           └── Theme.kt                 # Material 3 color scheme + automatic tablet/phone LocalUiScale provider
└── res/
    ├── font/
    │   ├── outfit.ttf                   # Bundled geometric sans-serif variable font (weights 400, 500, 600, 700)
    │   └── jetbrains_mono.ttf           # Bundled monospace variable font
    ├── drawable/                        # Custom adaptive launcher foreground & vector assets
    └── values/
        ├── strings.xml                  # App name ("Strukt") and UI string resources
        └── themes.xml                   # Edge-to-edge transparent system bar styling
```

### Detailed File Responsibilities

| File | Key Classes / Composables | What It Does |
|---|---|---|
| **`MainActivity.kt`** | `MainActivity`, `StruktPlannerApp`, `PortraitHandheldLayout`, `TabletLandscapeSplitLayout`, `PermanentStraightBottomBar`, `DayFlowSheetContent` | Configures transparent edge-to-edge system bars (`enableEdgeToEdge`), observes `PlannerViewModel.uiState`, switches between Portrait 1-screen and Landscape 2-column split layouts, and hosts top-level modal dialogs. |
| **`PlannerEntities.kt`** | `TaskEntity`, `SubtaskEntity`, `FolderEntity`, `TaskWithSubtasks`, `TaskMode`, `RecurrenceRule` | Defines the SQLite tables, enums (`TIMELESS_STEP`, `TIMED_BLOCK`, `TOP_BUBBLE`), recurrence rules (`NONE`, `DAILY`, `WEEKDAYS`, `WEEKLY`, `MONTHLY`), and session interval parser (`parsedSessionSegments`). |
| **`PlannerDao.kt`** | `PlannerDao` | Exposes reactive `Flow<List<TaskWithSubtasks>>` and `Flow<List<FolderEntity>>` streams plus `suspend` functions for inserting, updating, and deleting tasks, subtasks, and folders. |
| **`PlannerDatabase.kt`** | `PlannerDatabase` | Configures the local Room database (`strukt_planner_v4.db`) and seeds realistic initial folders (*Physics*, *Chemistry*, *Mathematics*, *Reading*, *Health & Sport*) and sample tasks on first launch. |
| **`PlannerRepository.kt`** | `PlannerRepository` | Encapsulates business operations: toggling task completion, splitting/merging break loops, recording live focus intervals, promoting Inbox tasks into the daily flow, and updating sequence step numbers. |
| **`PlannerViewModel.kt`** | `PlannerViewModel`, `PlannerUiState`, `StruktDestination`, `FlowFilterMode` | Combines Room database flows with UI filter states (`selectedDateEpochDay`, `flowFilter`, `searchQuery`, `selectedFolderId`, `activeFocusTaskId`) into a single immutable `StateFlow<PlannerUiState>`. |
| **`NeoComponents.kt`** | `DotGridBackground`, `NeoCard`, `NeoFolderCard`, `NeoPillButton`, `NeoProgressBar`, `NeoSquareCheckbox`, `NeoCircleCheckbox`, `TaskIconCatalog` | Reusable Soft Neo-Brutalist UI building blocks with `1.5.dp` `#1A1A1A` borders, spring-animated press feedback (`smoothPressClickable`, `smoothCombinedClickable`), and Pure White (`#FFFFFF`) bordered progress tracks. |
| **`TimelineFlowSection.kt`** | `DateAndWeekHeader`, `TopAnytimeBubblesRow`, `FlowStructureFilterBar`, `SpineConnectorSegment`, `DottedBreakLoopCanvas`, `HybridSpineTaskRow`, `TaskDetailPopupModal` | Renders the swipeable 7-day week header, top anytime bubbles, shifted-left vertical spine with unbroken oval capsules or split half-ovals + cubic-Bezier dotted break loops, and the unified Task Action & Detail Modal. |
| **`InboxAndBlueprintsSection.kt`** | `NeoInboxPane`, `ScheduleInboxMiniPopup`, `FoldersAndSectionsScreen`, `CreateFolderPopupModal`, `AddSectionPopupModal` | Renders the unscheduled Brain Dump Inbox, the 1-tap schedule mini-popup, the 2×2 readiness stat cards, the searchable Folders list, and the Folder Detail view with per-section task cards. |
| **`TaskEditorAndFocusModal.kt`** | `StructuredTaskEditorDialog`, `TimeAndDurationSubPopup`, `ColorAndIconSubPopup`, `FolderAndSectionSubPopup`, `RecurrenceSubPopup`, `SubtasksSubPopup`, `AntiParalysisFocusScreen` | Implements the progressive Pencil Task Editor (where each row opens a dedicated sub-popup) and the Focus Timer screen with tabular numerals (`tnum`), subtask checklist, and live break-interval recording. |

---

## 🗄 Database Schema & Data Model (Room SQLite)

Strukt uses **Room SQLite** (`PlannerDatabase`) for 100% offline, device-local persistence.

### 1. `tasks` Table (`TaskEntity`)
| Column | Type | Description |
|---|---|---|
| `id` | `Long` (PK) | Auto-generated unique task ID. |
| `title` | `String` | Task title (wraps up to 2 lines on timeline/cards, full multi-line in Focus Mode). |
| `notes` | `String` | Optional contextual notes. |
| `dateEpochDay` | `Long?` | `LocalDate.toEpochDay()` when scheduled on a day, or `null` when stored in the **Inbox**. |
| `taskMode` | `String` | `"TIMELESS_STEP"` (numbered `#1, #4...`), `"TIMED_BLOCK"` (clock-anchored), or `"TOP_BUBBLE"` (top habit circle). |
| `startTimeMinutes` | `Int?` | Minutes from midnight (`0..1439`, e.g., `600` = `10:00`) for `TIMED_BLOCK` tasks; `null` for timeless/bubbles. |
| `durationMinutes` | `Int` | Planned duration in minutes (determines oval capsule height on the spine and countdown target in Focus Mode). |
| `colorKey` | `String` | Palette key: `"CORAL"`, `"YELLOW"`, `"SKY"`, `"MINT"`, `"SAGE"`, `"PEACH"`, or `"ROSE"`. |
| `iconKey` | `String` | Icon key mapped in `TaskIconCatalog` (`"SCIENCE"`, `"BOOK"`, `"WORKOUT"`, `"CODE"`, `"PLANT"`, `"CALL"`, etc.). |
| `folderName` | `String` | Parent subject folder name (e.g., `"Physics"`). |
| `sectionName` | `String` | Section inside the folder (e.g., `"Electrostatics"`). |
| `recurrenceRule` | `String` | `"NONE"`, `"DAILY"`, `"WEEKDAYS"`, `"WEEKLY"`, or `"MONTHLY"`. |
| `recurrenceDayOfMonth` | `Int?` | Day of month (`1..31`) when `recurrenceRule == "MONTHLY"`. |
| `isCompleted` | `Boolean` | Completion state for non-recurring tasks. |
| `completedEpochDaysCsv` | `String` | Comma-separated epoch days tracking per-day completion for recurring tasks independently on each date. |
| `sessionSegmentsCsv` | `String` | Semicolon-separated `"startMin-endMin"` intervals (e.g., `"840-877;887-915"`). When `>1` segment exists, the timeline renders the **Halved Capsule + Dotted Break Loop**. |
| `activeSessionStartMin` | `Int?` | Non-null while the Focus Timer is actively running, storing the start minute of the current live work segment. |
| `sortOrder` | `Int` | Controls the **Sequence Step (`#`)** position on the timeline spine. |

### 2. `subtasks` Table (`SubtaskEntity`)
- Linked to `tasks.id` via a foreign key with `CASCADE` delete (`Relation` modeled by `TaskWithSubtasks`).
- Fields: `id`, `taskId`, `title`, `stageTag`, `isCompleted`, `sortOrder`.

### 3. `folders` Table (`FolderEntity`)
- Stores custom user-created and seeded subject folders.
- Fields: `id`, `name`, `codeTag` (e.g., `"PHY 12"`), `colorKey`, `iconKey`, `sectionsCsv` (pipe-separated section names), `sortOrder`.

---

## ⛓ Timeline Spine & Sequence Ordering Logic

### How Tasks Are Filtered & Sorted on Selected Dates (`PlannerViewModel.kt`)
1. **Recurrence Matching (`TaskEntity.appliesToDate`)**:
   - A task appears on `selectedDate` if its `dateEpochDay == selectedDate.toEpochDay()`, OR if it has an active `RecurrenceRule` (`DAILY`, `WEEKDAYS`, `WEEKLY` matching weekday, or `MONTHLY` matching day-of-month) starting on or before `selectedDate`.
   - Per-date completion is resolved via `task.isCompletedOnDate(epochDay)` so marking a daily habit done today does not mark it done tomorrow.
2. **Separation into Top Bubbles vs. Spine Flow**:
   - Tasks with `TaskMode.TOP_BUBBLE` are routed to `uiState.topBubbles` and rendered in `TopAnytimeBubblesRow`.
   - Tasks with `TaskMode.TIMED_BLOCK` and `TaskMode.TIMELESS_STEP` are routed to `uiState.spineFlowTasks`.
3. **Unified Spine Sorting (`effectiveFlowOrderKey`)**:
   - Every spine task is assigned a unified numeric sort key so **Timed Blocks** and **Timeless Steps** interleave predictably:
     - If a task has a custom step number (`sortOrder in 1..99`), its sort key is `sortOrder * 100` (e.g., Step `#1` $\rightarrow$ `100`, Step `#4` $\rightarrow$ `400`, Step `#6` $\rightarrow$ `600`).
     - If a `TIMED_BLOCK` has no custom step override, its sort key is `startTimeMinutes` (e.g., `07:30` = `450`, `10:00` = `600`).
   - Adjusting `[- #6 +]` inside the Task Popup (or inside the Pencil Editor's *Time & Duration* sub-popup) updates `sortOrder` immediately and re-sorts the task's position in the spine.

### How the Halved Capsule & Dotted Break Loop Works (`TimelineFlowSection.kt`)
- **Unbroken Task (`!hasPauseBreak`)**:
  - Renders a single vertical oval bubble capsule (`RoundedCornerShape(percent = 50)`) whose height scales with duration (`68.sdp` to `84.sdp`) and touches incoming/outgoing `SpineConnectorSegment` lines with **zero vertical gap**.
- **Broken / Paused Task (`hasPauseBreak`)**:
  - Triggered either by pausing & resuming the Focus Timer or by tapping **`Split Break Loop`** inside `TaskDetailPopupModal`.
  - The center spine column stacks three connected elements with `0.dp` gap:
    1. **Top Half-Oval** (`52.sdp` tall, rounded top `22.sdp`, flat cut bottom `4.sdp`) containing the task icon.
    2. **`DottedBreakLoopCanvas`** (`54.sdp` tall): Draws a dashed cubic-Bezier path starting at `(cx, 0f)`, sweeping out to `cx + 16.sdp`, looping over the crest, and returning to `(cx, height)`.
    3. **Bottom Half-Oval** (`52.sdp` tall, flat cut top `4.sdp`, rounded bottom `22.sdp`) containing the same task icon.

---

## 📱 Responsive & Adaptive Multi-Device Architecture

Strukt ships as a **single universal APK** that adapts automatically to both standard phones (`~360dp – 420dp` wide) and large tablets like the **12.1-inch Redmi Pad Pro 5G** (`~800dp × 1280dp`):

1. **Orientation-Driven Layout Rule (`MainActivity.kt`)**:
   - Controlled by `val isLandscapeSplitLayout = maxWidth > maxHeight`:
     - **Portrait Mode (`maxWidth <= maxHeight`)**: Always displays `PortraitHandheldLayout` — the **1-screen vertical layout** with the docked bottom navigation bar (`PermanentStraightBottomBar`) on both phones and tablets held vertically.
     - **Landscape Mode (`maxWidth > maxHeight`)**: Always displays `TabletLandscapeSplitLayout` — the **2-column split-screen workspace** featuring the permanent dark left navigation rail (`NavRailDark`), the persistent Left Inbox Column, and the Right Main Workspace.

2. **Proportional UI & Typography Scale Factor (`Type.kt` & `Theme.kt`)**:
   - `MyApplicationTheme` inspects `shortestWidthDp = minOf(screenWidthDp, screenHeightDp)`:
     - **Standard Phone (`shortestWidthDp < 600`)**: `LocalUiScale = 1.0f` (base phone dimensions).
     - **Tablet (`shortestWidthDp >= 600`, e.g., Redmi Pad Pro 5G at `800dp`)**: `LocalUiScale = 1.24f`.
   - All Material 3 `Typography` styles scale via `scaledTypography(uiScale)`, and custom dimensions/fonts use the `.sdp` and `.ssp` extensions (`Number.sdp`, `Number.ssp`) so icons, top bubbles, spine capsules, checkboxes, and navigation bars scale up by **24%** in lockstep on tablets.

3. **Balanced Container Widths (`widthIn(max = 680.dp)`)**:
   - Main content containers in both Portrait and Landscape are centered within `Modifier.widthIn(max = 680.dp)` so single-column cards maintain balanced proportions and never stretch edge-to-edge across wide tablet screens, while preserving all internal card layouts and vertical lists intact.
   - In Landscape on tablets (`isTabletDevice = minOf(maxWidth, maxHeight) >= 600.dp`), the Left Navigation Rail widens from `72.dp` to `88.dp` and the Persistent Inbox Column widens from `310.dp` to `380.dp`.

---

## 🎨 Design System: Color Palette & Typography Hierarchy

### Soft Neo-Brutalist Color Palette (`Color.kt`)
| Token | Hex | Role |
|---|---|---|
| `PaperCanvas` | `#F7F4EF` | Warm papery off-white canvas background with subtle dot grid (`#DDD7CE`) |
| `PureWhiteSurface` | `#FFFFFF` | Timeline container, unfocused pill buttons, progress bar tracks, and modal surfaces |
| `SoftSheetSurface` | `#EFECE6` | Completed task fills, secondary chips, and subtask row backgrounds |
| `InkBlack` | `#1A1A1A` | Primary headings, crisp `1.5.dp` structural borders, and solid progress bar fills |
| `InkCharcoal` | `#4A4A4A` | Secondary body copy and folder progress labels |
| `InkMuted` | `#71717A` | Timestamps, completed strikethrough text, and subtle metadata |
| `NeoYellow` | `#FFE156` | Sunny Yellow — Overall Readiness card, active nav highlight, Focus Timer CTA |
| `NeoSkyBlue` | `#99D6FF` | Vibrant Sky Blue — Physics folder, Milestones Done card, active Focus hero card |
| `NeoMintGreen` | `#7BF1A8` | Fresh Mint Green — Tasks Completed card, Mark Done / Resume / Continue actions |
| `NeoCoral` | `#FF5E45` | Energetic Coral-Red — Selected date circle, primary `+` button, Delete actions |
| `NeoPeach` | `#FFC48C` | Warm Apricot Peach — Chemistry folder & warm task accents |
| `NeoRosePink` | `#FFADD6` | Bright Pastel Pink — Folders count card & creative task accents |

### Font Weight Hierarchy (`Type.kt`)
Uses the bundled **Outfit** variable font (`res/font/outfit.ttf`) with explicit `FontVariation.weight(...)` axes:
- **Bold (`700`)**: Month header (`"November"`), large summary stat numbers (`"33%"`, `"3 / 5"`, `"4 / 12"`, `"5"`), active Focus card title, and the tabular-numeral (`fontFeatureSettings = "tnum"`) Focus countdown timer (`"60:00"`).
- **SemiBold (`600`)**: Folder card titles, timeline & inbox task titles, section headers, and interactive pill buttons (`"Continue"`, `"Focus Timer"`, `"Mark Done"`, `"+ Folder"`).
- **Medium (`500`)**: Subtask checklist items, status indicators (`"Completed"`, `"Active Folder"`), and calendar day numbers.
- **Regular (`400`)**: Metadata subtitles (`"4 Sections • 1 Tasks"`), timeline clock intervals (`"10:00"`), sequence numbers (`"#6"`), and input placeholders.

---

## 🧭 Screen-by-Screen Guide & User Flows

1. **Timeline Flow (`StruktDestination.FLOW`)**:
   - **Swipeable Week Strip**: Swipe left/right across weeks or tap any day to switch dates. Colored dots under each date preview scheduled tasks.
   - **Top Bubbles Row**: Tap a bubble circle (*Structure Tomorrow*, *Water Plants*, *Call Mum*) to toggle Done/Undone with smooth color dimming; **long-click** (or tap the label) to open the unified **Task Action & Detail Modal**.
   - **Spine Tasks**: Tap or long-click any task row (*Physics: Electric Charges & Fields*, *Yoga Workout*, *Read a Book*) to open the unified **Task Action & Detail Modal**.
   - **Unified Task Action & Detail Modal**:
     - Adjust **Sequence Step (`[- #6 +]`)** and toggle **`Split Break Loop` / `Merge Loop`** side-by-side in a compact 1-row bar.
     - Check off or add **Subtasks** (collapses into a clean `+ Add Subtask` chip when `0` subtasks exist).
     - Launch **`Focus Timer`**, toggle **`Mark Done`**, open **`Edit`** (Pencil), or tap **`Delete`** (which opens a dedicated **Delete Task?** confirmation popup immediately).

2. **Inbox (`StruktDestination.INBOX` / Left Pane in Landscape)**:
   - Displays **DAY FLOW (%)** and **STEPS DONE** summary cards at the top.
   - Type into the **Brain dump** field and tap `+` to capture unscheduled tasks.
   - Tap `+` on any Inbox card to open `ScheduleInboxMiniPopup` and schedule it into today's Timeline as either **Timeless** or **With Time**.

3. **Folders & Sections (`StruktDestination.FOLDERS`)**:
   - Top 2×2 grid displays **OVERALL READINESS**, **MILESTONES DONE**, **TASKS COMPLETED**, and **FOLDERS**.
   - Search folders in real time or tap **`+ Folder`** to create a new subject folder with custom code tag, color, and initial sections.
   - Tap any Folder card (*Physics*, *Chemistry*, *Mathematics*, etc.) to inspect its sections, add new sections (`+ Section`), and toggle or inspect tasks within each section.

4. **Focus Mode (`StruktDestination.FOCUS`)**:
   - Displays the active task's hero card, tabular-numeral countdown timer (`60:00`), and progress bar.
   - **Play / Pause (Break) / Resume**: Starting and pausing the timer automatically records real-time `"Started HH:MM – HH:MM"` and `"Resumed HH:MM – Active"` intervals onto the task, rendering the dotted break loop both in the **Session Timeline** log and on the main **Timeline Spine**.
   - **`Done • Next Task`**: Marks the current task completed and automatically advances Focus Mode to the next incomplete task in the day's flow.

---

## 🚀 Building, Testing & CI/CD

### Prerequisites
- **Android Studio** Ladybug / Koala or newer
- **JDK 17**
- **Android SDK** (`compileSdk 36`, `targetSdk 36`, `minSdk 24`)

### Command-Line Build & Unit Tests
```bash
# Build Debug APK
gradle assembleDebug

# Output APK location:
# app/build/outputs/apk/debug/app-debug.apk

# Run JVM & Robolectric unit tests
gradle :app:testDebugUnitTest
```

---

## 📄 License
This project is for personal use and study. All rights reserved.
