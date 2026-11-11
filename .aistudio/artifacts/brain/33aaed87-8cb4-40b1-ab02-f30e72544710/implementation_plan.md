# Strukt — Clean Structured-First Visual Planner, Folders & Live Session Tracker

A streamlined, geometric revision of **Strukt** that removes visual clutter and text overload, pivots strongly toward **Structured's** clean visual timeline, replaces static templates with **Folders & Sections**, supports **Daily / Weekly / Monthly recurring tasks** (e.g. *"1st of every month"*), and logs **live Focus timer sessions with pause/resume dotted "Continued" lines** directly onto the timeline.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> **Summary of All Fixes & New Capabilities**: Every point from your feedback has been incorporated into the 4 execution phases below. Click **Proceed** (or attach any additional reference screenshots) to apply these changes in order.

1. **Rotation & Tablet Ergonomics**:
   - Uses `fullSensor` + `resizeableActivity="true"` for physical rotation on your **Redmi Pad Pro 5G**, plus adds a **1-Tap Orientation / Split-View Toggle** icon in the top bar so you can also switch between Horizontal Tablet Split-Screen and Vertical Handheld mode directly inside the browser preview when the emulator's rotation sensor is locked.
2. **Typography & De-Cramping**:
   - **Removed Playfair Display Italic Serif**: Standardized 100% on clean geometric **Plus Jakarta Sans** + **JetBrains Mono**.
   - **Shadow Discipline**: Kept crisp Neo-Brutalist offset shadows **only** on main cards and the timeline capsule icons; removed shadows from small buttons, pills, and text labels.
   - **Icon-Only Geometric Bottom Bar**: Centered, equal-spaced icon-only navigation bar (`Inbox`, `Timeline`, `Folders`, `Focus`).
3. **Inbox & Progressive Sub-Popup Editor**:
   - Input placeholder shortened to **`"Brain dump"`**; removed the redundant `+ Detailed` button.
   - Tapping `+` on an Inbox task opens a **compact Schedule Mini-Popup** (pick *Timeless* or *Timed* and tap *Schedule*).
   - **Task Editor Sub-Popups**: Title placeholder is simply **`"Task"`**. Duration, Color & Icon, Folder & Section, Repeat (including **Monthly on Day X** & **Daily**), and Subtasks each open in their own clean **sub-popup** when tapped instead of cramming everything onto one screen.
4. **Timeline Header & Spine Cleanup**:
   - Header displays **only the Month** (e.g. **`November`**) with clean navigation arrows; removed `"FLOW OS"`, removed the Reset button, and fixed text wrapping when `"Today"` is visible.
   - **Removed the separate upper "Do This Next" window** and removed the `+ Add Bubble` button so the timeline sheet never jumps up/down when switching days.
   - **Minimal Task Rows**: The timeline spine shows only the Time/Step, the Colored Capsule Icon (with its shadow), the Task Title, and the Completion Circle. Tapping a task opens a **dedicated Task & Subtasks Popup** to view/check off subtasks or launch Focus.
5. **Folders & Sections (Replacing Blueprints)**:
   - **Completely removed Day Templates/Blueprints**.
   - Replaced with **Folders & Sections** (e.g. *Physics*, *Mathematics*, *Reading*, *Sports* -> organized into custom *Sections* like *Chapters*, *Practice*, *Labs*), plus an equal-sized 2×2 grid of minimal stat cards (title + number only, zero subtitle clutter) and a **`"Search..."`** bar.
6. **Focus Mode & Live Pause/Resume Timeline Logging**:
   - Starting the timer in Focus Mode records the **real device clock time** (e.g. `14:00`) onto the task in the timeline.
   - Pausing at `14:37` and resuming at `14:47` records the work segments without creating a separate "Break" task—displaying a **dotted vertical line (`⋮ Continued 14:47`)** inside the task's timeline entry.

---

## 1. Phased Implementation Plan

### Phase 1 — Theme Cleanup, Rotation Fix & Inbox Refinement
- Replace `Playfair Display` with geometric `Plus Jakarta Sans` across all typography levels; remove offset shadows from small controls.
- Add an in-app **Landscape / Portrait Split-View Toggle** alongside native sensor rotation so tablet mode can be tested seamlessly in the preview.
- Clean up the **Inbox** panel: placeholder `"Brain dump"`, remove `+ Detailed`, and add the compact **Schedule to Flow Mini-Popup** when tapping `+` on an inbox item.

### Phase 2 — Progressive Sub-Popup Task Editor & Recurrence Engine
- Rebuild the Task Editor so tapping **Duration**, **Color & Icon**, **Folder & Section**, **Repeat** (*One-Time*, *Daily*, *Weekdays*, *Monthly on day 1..31*), or **Subtasks** opens a dedicated, uncluttered popup modal.
- Update the date matching engine so **Monthly** tasks (e.g. *"File Taxes"* on the 1st of every month) and **Daily** tasks (e.g. *"Pray"*, *"Read"*) automatically appear on matching calendar dates.

### Phase 3 — Clean Structured Timeline & Task Detail Modal
- Simplify the top header to **`November`** + week strip (no `FLOW OS`, no Reset icon, no vertical year wrapping).
- Remove the upper `"Do This Next"` window and `+ Add Bubble` button; highlight the current active task inline on the spine.
- Keep timeline rows minimal (Time/Step + Shadowed Capsule Icon + Title + Checkbox + Dotted `"Continued"` session log if paused/resumed). Tapping a row opens the **Task & Subtasks Detail Modal**.

### Phase 4 — Folders & Sections Workspace + Live Focus Session Logger
- Replace Blueprints with the **Folders & Sections** organizer (create/edit folders like *Physics*, *Sports*, *Reading*, add sections inside each folder, and view tasks grouped by section) + equal-height 2×2 minimal metric cards.
- De-cramp **Focus Mode** and wire the **Live Session Tracker** (`startClockMin`, `pauseResumeSegments`) so starting, pausing, and resuming a task timer feeds real start/resume times and a dotted **`Continued`** indicator directly into the timeline.

---

## 2. Technical Architecture & Data Model Updates

```
┌────────────────────────────────────────────────────────────────────────────┐
│             Clean Geometric UI (Jakarta Sans + Icon-Only Nav)              │
│  ┌──────────────────┬───────────────────────┬───────────────────────────┐  │
│  │   Inbox Panel    │  Clean Timeline Spine │    Folders & Sections     │  │
│  │ • "Brain dump"   │ • Month-only Header   │ • Equal 2x2 Minimal Stats │  │
│  │ • Compact "+"    │ • Capsule + Title     │ • Folder -> Section Tree  │  │
│  │   Schedule Popup │ • Dotted "Continued"  │ • Task Detail & Subtasks  │  │
│  └────────┬─────────┴───────────┬───────────┴─────────────┬─────────────┘  │
└───────────┼─────────────────────┼─────────────────────────┼────────────────┘
            │                     ▼                         │
┌───────────┴───────────────────────────────────────────────┴────────────────┐
│           Sub-Popup Modals (Duration / Color & Icon / Folder / Repeat)     │
└─────────────────────────────────────┬──────────────────────────────────────┘
                                      ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                   Room Database (Tasks, Subtasks, Folders)                 │
│  • TaskEntity: + folderName, sectionName, dayOfMonthRecurrence,            │
│                + sessionLog ("14:00-14:37||14:47-15:15")                   │
│  • FolderEntity: id, name, colorKey, iconKey, sectionsCsv                  │
└────────────────────────────────────────────────────────────────────────────┘
```
