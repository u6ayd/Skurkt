# Strukt ⚡

> **A Commercial-Grade Soft Neo-Brutalist Visual Day & Sequence Planner for Android**  
> Inspired by **Structured** and **Learnify**, engineered in Kotlin & Jetpack Compose for 120Hz handheld phones and 12.1-inch tablets (**Redmi Pad Pro 5G**).

---

## 📖 Table of Contents

1. [Overview & Core Philosophy](#-overview--core-philosophy)
2. [Project Directory & Modular File-by-File Architecture](#-project-directory--modular-file-by-file-architecture)
3. [Database Schema & Non-Destructive Data Model (Room SQLite v6)](#-database-schema--non-destructive-data-model-room-sqlite-v6)
4. [Timeline Spine, Sequence Ordering & Performance Pipeline](#-timeline-spine-sequence-ordering--performance-pipeline)
5. [Persistent Focus Timer Engine & 1Hz Leaf Isolation](#-persistent-focus-timer-engine--1hz-leaf-isolation)
6. [Responsive & Adaptive Multi-Device Architecture](#-responsive--adaptive-multi-device-architecture)
7. [Design System: Color Palette, Typography & 120Hz Micro-Animations](#-design-system-color-palette-typography--120hz-micro-animations)
8. [Screen-by-Screen Guide & User Flows](#-screen-by-screen-guide--user-flows)
9. [Building, Testing & Verification](#-building-testing--verification)

---

## 🎯 Overview & Core Philosophy

**Strukt** combines two distinct ADHD-friendly productivity paradigms into a single cohesive visual interface:

1. **Structured-Style Visual Day Flow**:
   - A continuous vertical timeline spine that unifies **Timed Blocks** (anchored to clock intervals like `10:00 – 11:00`) and **Timeless Workflow Steps** (ordered by sequence rank like `#1`, `#2`, `#3` without forcing artificial clock deadlines).
   - **Top Daily Bubbles** (*Structure Tomorrow*, *Water Plants*, *Call Mum*) sit above the timeline spine for recurring or anytime habits.
   - **Halved Capsules & Dotted Break Loops**: When a task is paused for a break (either live from the Focus Timer or manually via the 1-tap *Split Break Loop* toggle), its vertical oval capsule splits smoothly (`animateContentSize`) into a **top half-oval** and **bottom half-oval** connected by a cached cubic-Bezier roller-coaster dotted loop.

2. **Learnify-Inspired Subject Folders, Sections & Multi-Page Reports**:
   - Tasks are organized into color-coded **Folders** (*Physics*, *Chemistry*, *Mathematics*, *Reading*, *Health & Sport*) and granular **Sections** (e.g., *Electrostatics*, *Optics*, *Organic*, *Calculus*).
   - Bento summary stat cards (`NeoStatCard`) track **Overall Readiness (%)**, **Milestones Done (Subtasks)**, **Tasks Completed**, and **Active Folders**.
   - **Multi-Page PDF & CSV Progress Export**: Generates paginated A4 PDF and CSV progress reports on `Dispatchers.IO` and supports bulk cleanup of completed tasks.

---

## 🗂 Project Directory & Modular File-by-File Architecture

```text
app/src/main/
├── AndroidManifest.xml                         # App config, FileProvider for PDF/CSV export, adaptive launcher icon
├── java/com/example/
│   ├── MainActivity.kt                         # Edge-to-edge setup, StruktWorkspaceHost, Portrait/Landscape split layout
│   ├── data/
│   │   ├── PlannerEntities.kt                  # Room Entities (v6), pre-computed @Ignore properties, forEpochDay() projection
│   │   ├── PlannerDao.kt                       # Reactive Room Flows + non-destructive subtask diffing @Transaction
│   │   ├── PlannerDatabase.kt                  # Singleton Room database (version 6) with initial seed data
│   │   └── PlannerRepository.kt                # Repository mediating CRUD, per-day recurring state, and step reordering
│   └── ui/
│       ├── PlannerViewModel.kt                 # MVVM StateFlow pipeline, Dispatchers.Default schedule computation, FocusTimerSnapshot
│       ├── components/
│       │   ├── NeoComponents.kt                # Core Neo-Brutalist primitives (ShaderBrush DotGrid, NeoCard, NeoStatCard, etc.)
│       │   ├── DateAndWeekHeader.kt            # Bidirectional swipeable 7-day week pager, midnight rollover, Top Bubbles & Filter bar
│       │   ├── TimelineFlowSection.kt          # Cached SpineConnectorSegment, DottedBreakLoopCanvas, HybridSpineTaskRow
│       │   ├── TaskDetailPopupModal.kt         # Unified Task Action & Detail Modal (step stepper, break loop, subtasks, delete)
│       │   ├── WheelTimeAndDurationPicker.kt   # Hardware-accelerated 120Hz wheel time/duration pickers & presets
│       │   ├── TaskEditorAndFocusModal.kt      # Progressive Pencil Task Editor dialog & in-place sub-screens
│       │   ├── FocusAndSettingsModals.kt       # AntiParalysisFocusScreen, 1Hz FocusLiveClockAndProgressLeaf, Settings modal
│       │   ├── InboxAndBlueprintsSection.kt    # Brain-dump NeoInboxPane & ScheduleInboxMiniPopup
│       │   ├── FoldersAndSectionsScreen.kt     # Directional AnimatedContent Folder Grid & Section detail views
│       │   └── CleanupAndExportModal.kt        # Dispatchers.IO multi-page PDF/CSV generator & UniversalBulkSelectBar
│       └── theme/
│           ├── Color.kt                        # Soft Neo-Brutalist pastel palette, ink borders, O(1) color-key lookup
│           ├── Type.kt                         # Bundled Outfit variable font (weights 400..700) & .sdp/.ssp tablet scaling
│           └── Theme.kt                        # Material 3 color scheme + automatic tablet/phone LocalUiScale provider
└── res/
    ├── font/
    │   └── outfit.ttf                          # Bundled geometric sans-serif variable font (weights 400, 500, 600, 700)
    ├── drawable/                               # Custom adaptive launcher foreground & vector assets
    ├── xml/
    │   └── file_paths.xml                      # FileProvider paths for exporting PDF/CSV reports
    └── values/
        ├── strings.xml                         # App name ("Strukt") and UI string resources
        └── themes.xml                          # Edge-to-edge transparent system bar styling
```

### Detailed Module Responsibilities

| File | Key Classes / Composables | Responsibility |
|---|---|---|
| **`MainActivity.kt`** | `MainActivity`, `StruktPlannerApp`, `StruktWorkspaceHost`, `StruktInboxWorkspacePane`, `PortraitHandheldLayout`, `TabletLandscapeSplitLayout`, `DayFlowSheetContent` | Configures transparent edge-to-edge system bars, normalizes navigation state across orientation changes, and hosts `StruktWorkspaceHost` so Portrait and Landscape share a single destination router. |
| **`PlannerEntities.kt`** | `TaskEntity`, `SubtaskEntity`, `FolderEntity`, `TaskWithSubtasks`, `TaskMode`, `RecurrenceRule` | Defines Room SQLite v6 entities, pre-computes `@Ignore` properties (`modeEnum`, `recurrenceEnum`, `originLocalDate`, `effectiveStepNumber`, `parsedSessionSegments`, `hasBreakLoop`, `totalWorkedMinutes`, `totalBreakMinutes`) at construction time, and projects recurring tasks per date via `forEpochDay(epochDay)`. |
| **`PlannerDao.kt`** | `PlannerDao` | Exposes reactive `Flow<List<TaskWithSubtasks>>` and `Flow<List<FolderEntity>>` streams and implements non-destructive subtask upserts in `updateTaskWithSubtasks` (updating existing IDs in place, deleting only removed IDs, and inserting new rows). |
| **`PlannerDatabase.kt`** | `PlannerDatabase` | Configures the Room database (`version = 6`) and seeds realistic initial folders (*Physics*, *Chemistry*, *Mathematics*, *Reading*, *Health & Sport*) and sample tasks on first launch. |
| **`PlannerRepository.kt`** | `PlannerRepository` | Encapsulates per-day completion toggling (`withUpdatedDayCompletion`), per-day session segment recording (`withUpdatedDaySession`), overlap detection, and spine step reordering. |
| **`PlannerViewModel.kt`** | `PlannerViewModel`, `PlannerUiState`, `FocusTimerSnapshot`, `PendingOverlapConflict` | Splits schedule computation into `weekDotColorsFlow` and `computedDayScheduleFlow` on `Dispatchers.Default`, and manages persistent focus timers anchored to `SystemClock.elapsedRealtime()`. |
| **`NeoComponents.kt`** | `DotGridBackground`, `NeoCard`, `NeoStatCard`, `NeoFolderCard`, `NeoPillButton`, `NeoProgressBar`, `NeoSquareCheckbox`, `NeoCircleCheckbox`, `NeoCircleCloseButton`, `NeoDeleteTaskConfirmView`, `NeoSubtaskChecklistSection` | Provides reusable Soft Neo-Brutalist UI primitives with tiled `ShaderBrush` backgrounds, cached dashed dividers, spring-animated checkboxes/progress bars, and `.smoothPressClickable` tactile feedback. |
| **`DateAndWeekHeader.kt`** | `DateAndWeekHeader`, `TopAnytimeBubblesRow`, `FlowStructureFilterBar`, `ClockFormatConfig` | Implements the swipeable 7-day week strip with bidirectional `pagerState` synchronization, `rememberUpdatedState` weekday preservation on week swipe, and automatic midnight / `ON_RESUME` date rollover. |
| **`TimelineFlowSection.kt`** | `SpineConnectorSegment`, `DottedBreakLoopCanvas`, `HybridSpineTaskRow` | Renders the vertical timeline spine using `Modifier.drawWithCache` for zero-allocation path/stroke drawing and `Modifier.animateContentSize` for smooth single-oval $\leftrightarrow$ split-break-loop transitions. |
| **`TaskDetailPopupModal.kt`** | `TaskDetailPopupModal` | Unified modal for inspecting a task, stepping its sequence position (`[- #N +]`), toggling its break loop, checking/adding subtasks, starting Focus Mode, editing, or deleting. |
| **`WheelTimeAndDurationPicker.kt`** | `UnifiedTimeAndDurationPickerSection`, `NeoCompactClockSelector`, `NeoCompactMinutesRow` | Provides snap-flinging 120Hz wheel pickers and quick-select chips for start time, duration, and sequence step numbers. |
| **`TaskEditorAndFocusModal.kt`** | `StructuredTaskEditorDialog` | Implements the progressive Pencil Task Editor with in-place sub-screens for Time/Duration/Step, Color/Icon, Folder/Section, Recurrence (`NONE`, `DAILY`, `WEEKDAYS`, `WEEKLY`, `MONTHLY`, `CUSTOM_DAYS`), and Subtasks. |
| **`FocusAndSettingsModals.kt`** | `AntiParalysisFocusScreen`, `FocusLiveClockAndProgressLeaf`, `OverlapConflictPromptModal`, `StruktSettingsPopupModal` | Isolates the 1Hz countdown tick inside `FocusLiveClockAndProgressLeaf` (`repeatOnLifecycle(Lifecycle.State.STARTED)`) so the surrounding task card and subtask checklist never recompose on timer ticks. |
| **`InboxAndBlueprintsSection.kt`** | `NeoInboxPane`, `ScheduleInboxMiniPopup` | Renders the Brain Dump Inbox with `NeoStatCard` readiness metrics, quick-capture input, multi-select support, and 1-tap day scheduling. |
| **`FoldersAndSectionsScreen.kt`** | `FoldersAndSectionsScreen`, `CreateFolderPopupModal`, `AddSectionPopupModal` | Uses directional `AnimatedContent` and independent `LazyListState` instances (`folderGridListState` vs. `detailListState`) so Folder Grid scroll position is preserved across drill-down round-trips. |
| **`CleanupAndExportModal.kt`** | `CleanupAndExportModal`, `UniversalBulkSelectBar` | Runs multi-page PDF and CSV report generation and `ContentResolver` stream copying on `Dispatchers.IO`, with memoized per-filter task counts. |

---

## 🗄 Database Schema & Non-Destructive Data Model (Room SQLite v6)

Strukt uses **Room SQLite** (`PlannerDatabase`, schema `version = 6`) for 100% offline, device-local persistence.

### 1. `tasks` Table (`TaskEntity`)
| Column | Type | Description |
|---|---|---|
| `id` | `Long` (PK) | Auto-generated unique task ID. |
| `title` | `String` | Task title (wraps up to 2 lines on timeline/cards, full multi-line in Focus Mode). |
| `notes` | `String` | Optional contextual notes. |
| `dateEpochDay` | `Long?` | Origin `LocalDate.toEpochDay()` when scheduled, or `null` when stored in the **Inbox**. |
| `taskMode` | `String` | `"TIMELESS_STEP"` (numbered `#1, #2...`), `"TIMED_BLOCK"` (clock-anchored), or `"TOP_BUBBLE"` (top habit circle). |
| `startTimeMinutes` | `Int?` | Minutes from midnight (`0..1439`, e.g., `600` = `10:00`) for `TIMED_BLOCK` tasks; `null` for timeless/bubbles. |
| `durationMinutes` | `Int` | Planned duration in minutes (determines oval capsule height on the spine and countdown target in Focus Mode). |
| `sortOrder` | `Int` | Controls the **Sequence Step (`#`)** position on the timeline spine. |
| `colorKey` / `iconKey` | `String` | Palette key (`"CORAL"`, `"YELLOW"`, `"SKY"`, `"MINT"`, `"SAGE"`, `"PEACH"`, `"ROSE"`) and icon catalog key. |
| `folderName` / `sectionName` | `String` | Parent subject folder name (e.g., `"Physics"`) and section name (e.g., `"Electrostatics"`). |
| `recurrence` | `String` | `"NONE"`, `"DAILY"`, `"WEEKDAYS"`, `"WEEKLY"`, `"MONTHLY"`, or `"CUSTOM_DAYS"`. |
| `recurrenceDayOfMonth` | `Int` | Day of month (`1..31`) when `recurrence == "MONTHLY"`. |
| `recurrenceIntervalDays` | `Int` | Custom day interval (`>= 1`) when `recurrence == "CUSTOM_DAYS"`. |
| `excludedEpochDaysCsv` | `String` | Comma-separated epoch days where single occurrences of a recurring series were skipped/deleted. |
| `completedEpochDaysCsv` | `String` | Comma-separated epoch days tracking per-day completion for recurring tasks independently on each date. |
| `sessionSegments` | `String` | `"||"`-separated `"startMin-endMin"` intervals for non-recurring tasks. |
| `sessionSegmentsByDayCsv` | `String` | `";;"`-separated `"epochDay:startMin-endMin||startMin-endMin"` entries storing per-day work/break intervals for recurring tasks. |
| `activeSessionStartMin` / `activeSessionEpochDay` | `Int?` / `Long?` | Tracks an actively running Focus Timer session and the specific epoch day it belongs to. |

### 2. Non-Destructive `subtasks` Upsert (`PlannerDao.updateTaskWithSubtasks`)
- Linked to `tasks.id` via a foreign key with `CASCADE` delete.
- Editing a task in `StructuredTaskEditorDialog` never wipes and recreates existing subtasks: `PlannerDao.updateTaskWithSubtasks` diffs incoming subtask IDs against persisted rows, updating existing subtasks in place, deleting only removed subtasks, and inserting only newly added subtasks (`id == 0L`).

---

## ⛓ Timeline Spine, Sequence Ordering & Performance Pipeline

1. **Pre-Computed `@Ignore` Properties (`TaskEntity`)**:
   - Every `TaskEntity` parses `modeEnum`, `recurrenceEnum`, `originLocalDate`, `effectiveStepNumber`, `excludedEpochDays`, `completedEpochDays`, `sessionSegmentsByDay`, `parsedSessionSegments`, `hasBreakLoop`, `totalWorkedMinutes`, and `totalBreakMinutes` **once** at construction time rather than on every Compose frame.
2. **Per-Day Recurring Projection (`forEpochDay`)**:
   - `ComputedDaySchedule` projects tasks onto `selectedDateEpochDay` via `item.forEpochDay(selectedDateEpochDay)` on `Dispatchers.Default`, ensuring recurring tasks have independent completion states and break loops on every calendar date.
3. **Unfiltered Spine Step Reordering**:
   - `PlannerUiState` retains both `unfilteredSpineTasks` and `spineFlowTasks` so stepping a task's position (`[- #N +]`) while a filter (`TIMED` or `STEPS`) is active always reorders against the full day's spine without colliding with hidden tasks.
4. **Zero-Allocation Canvas Rendering**:
   - `DotGridBackground` tiles a cached `22.dp × 22.dp` `ImageBitmap` via `ShaderBrush(ImageShader(..., TileMode.Repeated, TileMode.Repeated))`.
   - `NeoDashedDivider`, `SpineConnectorSegment`, and `DottedBreakLoopCanvas` cache their `Path`, `Stroke`, and `PathEffect` inside `Modifier.drawWithCache`.

---

## ⏱ Persistent Focus Timer Engine & 1Hz Leaf Isolation

- **Monotonic Anchor (`FocusTimerSnapshot`)**:
  - `PlannerViewModel` stores active/paused timers in `focusTimersByTaskDay: Map<Pair<Long, Long>, FocusTimerSnapshot>` keyed by `(taskId, epochDay)` and anchored to `SystemClock.elapsedRealtime()`.
  - Switching tabs (`FLOW`, `INBOX`, `FOLDERS`, `FOCUS`), rotating between Portrait and Landscape, or backgrounding the app never resets or drifts a running focus session.
- **1Hz Leaf Composable (`FocusLiveClockAndProgressLeaf`)**:
  - Only the compact clock and progress leaf composable in `FocusAndSettingsModals.kt` observes the 1-second `repeatOnLifecycle(Lifecycle.State.STARTED)` ticker. The parent `AntiParalysisFocusScreen`, hero card, and subtask checklist experience **zero** recompositions while the timer counts down.

---

## 📱 Responsive & Adaptive Multi-Device Architecture

1. **Unified `StruktWorkspaceHost` & Orientation Guard (`MainActivity.kt`)**:
   - **Portrait Mode (`maxWidth <= maxHeight`)**: Displays `PortraitHandheldLayout` with `PermanentStraightBottomBar`.
   - **Landscape Mode (`maxWidth > maxHeight`)**: Displays `TabletLandscapeSplitLayout` with the dark left navigation rail (`NavRailDark`), persistent Left Inbox Pane (`StruktInboxWorkspacePane`), and Right Main Workspace (`StruktWorkspaceHost`).
   - Rotating into Landscape while viewing `INBOX` automatically normalizes `activeDestination` to `FLOW` so the Inbox is never duplicated across both split panes.
2. **Proportional UI & Typography Scale Factor (`Type.kt` & `Theme.kt`)**:
   - Scales automatically from `LocalUiScale = 1.0f` on phones (`shortestWidthDp < 600`) to `LocalUiScale = 1.24f` on tablets (`shortestWidthDp >= 600`) via `.sdp` and `.ssp` dimension extensions.

---

## 🎨 Design System: Color Palette, Typography & 120Hz Micro-Animations

- **Soft Neo-Brutalist Palette (`Color.kt`)**: Warm papery canvas (`#F7F4EF`), pure white surfaces (`#FFFFFF`), crisp `1.5.dp` `#1A1A1A` ink borders, and high-contrast accents (`NeoYellow`, `NeoSkyBlue`, `NeoMintGreen`, `NeoCoral`, `NeoPeach`, `NeoRosePink`, `NeoSage`).
- **Bundled Variable Typography (`Type.kt`)**: Uses `res/font/outfit.ttf` with explicit `FontVariation.weight(400..700)` axes and tabular numerals (`tnum`) for the Focus Timer.
- **120Hz Spring Micro-Animations**:
  - `Modifier.animateItem` with non-bouncy placement springs (`Spring.DampingRatioNoBouncy`, `Spring.StiffnessMediumLow`) on Timeline Spine, Inbox, and Folder/Section lists.
  - `Modifier.animateContentSize` on `HybridSpineTaskRow` when splitting/merging break loops.
  - Spring-animated checkmark scale/alpha in `NeoSquareCheckbox` & `NeoCircleCheckbox` and width fraction in `NeoProgressBar`.

---

## 🚀 Building, Testing & Verification

```bash
# Build Debug APK
gradle assembleDebug

# Run JVM & Robolectric unit tests
gradle :app:testDebugUnitTest
```

---

## 📄 License
This project is for personal use and study. All rights reserved.
