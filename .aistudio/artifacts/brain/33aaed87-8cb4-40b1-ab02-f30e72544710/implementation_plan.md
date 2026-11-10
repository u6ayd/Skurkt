# Strukt — Hybrid Neo-Brutalist × Structured Visual Day Planner

A battery-efficient Android day and sequence planner for the **Redmi Pad Pro 5G** (landscape & portrait) that hybridizes **Structured's** signature tablet split-screen Inbox, week ribbon, and vertical capsule timeline with **Trackerly's** crisp Neo-Brutalist dot-grid canvas, bold italic serif headers, `2.dp` ink borders, hard offset shadows, and vibrant candy-pastel cards.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> **Visual Hybridization & Phased Build Plan**: Based on your 7 reference screenshots, this revised plan fuses the **Trackerly Neo-Brutalist study aesthetic** (dot-grid background, bold italic serif titles, `2.dp` `#0F172A` borders with hard offset shadows, folder-tab cards, and yellow/sky-blue/mint/pink/peach fills) directly into **Structured's** core workflow (split-pane Inbox on tablet landscape, 7-day header with colored task dots, top timeless bubbles, vertical capsule spine with both timeless steps and timed blocks, and rich task sheet). Approve this plan to begin coding cleanly across the 4 structured phases below.

- **Confirmed Hybrid Visual Language**:
  - **From Screenshots 1–4 (Neo-Brutalist Aesthetic)**: Subtle dot-grid background canvas, **Playfair Display Bold Italic** headers paired with **Plus Jakarta Sans** & **JetBrains Mono** badges, `2.dp` solid dark-ink borders (`#0F172A`), hard `4.dp` offset shadows, folder-tabbed cards for Templates/Routines, and high-contrast candy pastels (Butter Yellow `#FDE047`, Sky Cyan `#7DD3FC`, Mint Green `#6EE7B7`, Rose Pink `#F9A8D4`, Tangerine Peach `#FDBA74`, Coral `#FF9B85`).
  - **From Screenshots 5–7 (Structured Ergonomics & Flow)**:
    - **Tablet Landscape Split View**: Persistent left **Inbox Column** with `+` quick-schedule buttons alongside the main **Week Strip + Daily Flow Sheet** on the right.
    - **Portrait View**: Full-width Week Strip + Daily Flow Sheet with Structured's floating bottom pill navigation bar + oversized Neo-Brutalist `+` FAB.
    - **Unified Timeless + Timed Spine**: Supports both top circular/brutalist "Anytime" bubbles AND inline vertical spine items that work either **without a clock time** (numbered sequence `#1`, `#2` so you never get stuck in paralysis) or **with a clock time** (duration-scaled vertical capsules, active progress fill, and free-time gap indicators).

---

## 1. Overview & Core Concept

- **What It Does**: Eliminates "what do I do next?" paralysis by letting you brain-dump tasks into the Inbox, slot them into a day's sequence **with or without clock times**, and step through them one by one using the pinned **Up Next** card and distraction-free **Focus Mode**.
- **Target Audience / Persona**: Built specifically for smooth, rotatable use on your **Redmi Pad Pro 5G** (12.1" tablet in horizontal split-screen or vertical handheld posture) with 100% local Room SQLite storage for zero background battery drain.

---

## 2. User Experience & Visual Design

### Visual Identity & Hybrid Theme
- **Canvas & Atmosphere**:
  - `--background`: Crisp Paper (`#FAF9F5`) with a custom subtle **Canvas Dot-Grid Pattern** (`#CBD5E1` dots spaced every `20.dp`, drawn efficiently in a single pass).
  - `--border-ink`: Deep Slate Ink (`#0F172A`), `2.dp` stroke weight on all cards, pills, checkboxes, and timeline nodes, paired with a solid `3.dp`–`4.dp` bottom-right offset shadow.
  - **Candy Neo-Brutalist Palette**:
    - **Butter Gold**: `#FDE047`
    - **Sky Cyan**: `#7DD3FC`
    - **Mint Emerald**: `#6EE7B7`
    - **Rose Pink**: `#F9A8D4`
    - **Tangerine Peach**: `#FDBA74`
    - **Coral Salmon**: `#FF9B85`
    - **Lavender Pop**: `#C4B5FD`
- **Typography (Bundled Locally via `res/font`)**:
  - **Display & Section Titles**: `Playfair Display` (Bold Italic) — mirrors the *"Trackerly"*, *"Academic Master Tracker"*, and *"Syllabus Courses"* headings while formatting Structured's `"7. October 2026"` header.
  - **UI & Task Titles**: `Plus Jakarta Sans` (SemiBold / Bold) for crisp readability.
  - **Time Stamps, Duration Pills & Sequence Badges**: `JetBrains Mono` (Medium / Bold) for `08:30`, `45m`, `STEP #01`.

### Hybrid Screen Layouts (Horizontal & Vertical)
1. **Tablet Landscape Mode (Redmi Pad Pro 5G Horizontal — Inspired by Screenshot 5 + Screenshot 1)**:
   - **Left Column (32% width) — Neo-Brutalist Inbox & Quick Stats**:
     - Top `Inbox` Neo-Brutalist pill badge + mini readiness/completion summary cards (`DONE TODAY`, `UP NEXT`).
     - Scrollable list of unscheduled Inbox cards (`2.dp` ink border, category icon, duration badge, task title, and a tactile `+` button that immediately sends the task into the selected day's flow).
   - **Right Main Area (68% width) — Week Ribbon & Structured Flow Sheet**:
     - **Header**: Bold Italic Serif date title (`10. November 2026 >`), `Today` jump pill, and `Templates` / `Focus` quick pills.
     - **7-Day Week Strip**: `Mon` through `Sun` with Neo-Brutalist active day badge and miniature colored task dots underneath each date.
     - **Main Flow Sheet** (`2.dp` ink-bordered white card container):
       - **Pinned Anti-Paralysis "Up Next" Bar**: Highlights the active/next task with a one-tap **`Start Focus`** button and subtask counter.
       - **Top "Anytime / Quick Routine" Bubbles**: Horizontal row of circular icon nodes with `2.dp` ink borders (like `Structure Tomorrow`, `Water Plants`, `Call Mum` in Screenshot 5 & 6).
       - **Vertical Capsule Spine (Timeless + Timed)**:
         - Left rail shows either the **Clock Time** (`08:30 – 09:15`) or **Timeless Sequence Tag** (`NEXT #1`, `NEXT #2`).
         - Center spine renders **Colored Circular or Stretched Vertical Capsule Nodes** (`2.dp` ink border + category icon inside) connected by a `2.dp` vertical ink line, plus *"🕒 Free time"* gap callouts between timed slots.
         - Right content area displays the task title, duration/remaining time subtitle, inline expandable **Subtask Checklists** (`☑ 1/3` with Neo-Brutalist square checkboxes from Screenshot 3), reorder up/down controls, and completion toggle.
2. **Portrait Mode (Vertical Handheld — Inspired by Screenshot 6)**:
   - Full-width Date Header + 7-Day Week Strip + Main Flow Sheet.
   - Floating Neo-Brutalist **Bottom Pill Bar** (`Inbox`, `Timeline`, `Templates`, `Focus`) + circular/brutalist `+` FAB on the bottom right.
3. **Structured-Style Task Creation & Edit Sheet (Inspired by Screenshot 7)**:
   - Full-bleed colored top header in the task's chosen Neo-Brutalist color with a large `2.dp` bordered icon badge and title input.
   - **Structure Mode Selector**: Switch effortlessly between **Timeless Flow Step** (ordered sequence without clock pressure), **Timed Schedule Block** (with start time & duration pills `15m`, `30m`, `45m`, `1h`, `1.5h`), or **Top Day Bubble**.
   - **Subtasks & Milestones Editor**: Add step-by-step checklist items directly inside the task.
   - **Recurrence & Folder Templates**: Set daily/weekday recurrence or save/apply **Folder-Tabbed Day Templates** (styled like the `PHY 12` / `CHEM 12` folder cards in Screenshot 2).

---

## 3. Phased Implementation Strategy (Clean, Modular & Battery-Efficient)

To keep the codebase rock-solid, maintainable, and optimized for the Redmi Pad Pro 5G, we will build the app in **4 structured phases**:

- **Phase 1 — Foundation, Typography & Neo-Brutalist Design System**:
  - Configure app metadata, adaptive launcher icon, and full sensor rotation (`fullSensor`) for seamless tablet landscape/portrait switching.
  - Install local Google Fonts (`Playfair Display`, `Plus Jakarta Sans`, `JetBrains Mono`) and build reusable Neo-Brutalist primitives (`NeoCard`, `NeoFolderCard`, `NeoPillButton`, `NeoCheckbox`, and the single-pass `DotGridBackground`).
- **Phase 2 — Zero-Battery Local Room Database & Repository Layer**:
  - Implement Room entities (`TaskEntity`, `SubtaskEntity`, `DayTemplateEntity`), DAOs returning reactive `Flow` streams, and `PlannerRepository` with rich starter data (sample timed & timeless tasks, Inbox items, and Folder-Tabbed Day Templates) so the app feels alive immediately.
- **Phase 3 — Hybrid Structured Timeline, Pinned "Up Next" & Responsive Tablet Split-View**:
  - Build `PlannerViewModel` and the adaptive layout engine: Side-by-side **Inbox + Week Strip + Visual Capsule Spine** in Landscape, and **Floating Pill Bar + Sheet** in Portrait.
  - Implement the unified **Timeless + Timed Capsule Spine**, top Anytime bubbles, inline subtask checklists, sequence reordering, and free-time gap detection.
- **Phase 4 — Task Editor Sheet, Anti-Paralysis Focus Mode & Folder Templates**:
  - Build the Screenshot-7-inspired **Task Creation/Editor Modal** (color/icon picker, Timeless vs. Timed toggle, duration pills, subtask builder).
  - Build the full-screen **Anti-Paralysis Focus View** (timestamp-based low-power timer, active subtask checklist, *"Complete & Advance to Next"* action) and the **Folder-Tabbed Templates & Progress Funnel View** (inspired by Screenshots 1–4).
  - Run `compile_applet` to verify a clean, warning-free build.

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

```
┌────────────────────────────────────────────────────────────────────────────┐
│                   DotGridBackground + NeoBrutalistTheme                    │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │       Adaptive Layout (Landscape Split-Screen vs Portrait Sheet)     │  │
│  ├──────────────────────┬─────────────────────────┬─────────────────────┤  │
│  │  Left Pane (Tablet)  │   Center Visual Spine   │   Overlays/Modals   │  │
│  │  • Brain-Dump Inbox  │   • Week Ribbon + Dots  │   • Task Editor     │  │
│  │  • Quick `+` to Day  │   • Pinned "Up Next"    │   • Focus Mode      │  │
│  │  • Readiness Stats   │   • Bubbles & Capsules  │   • Folder Tmpls    │  │
│  └──────────┬───────────┴────────────┬────────────┴──────────┬──────────┘  │
└─────────────┼────────────────────────┼───────────────────────┼─────────────┘
              │                        ▼                       │
┌─────────────┴────────────────────────────────────────────────┴─────────────┐
│                     PlannerViewModel (Single StateFlow)                    │
│  • Combines selectedDate, dayFlow, inboxFlow, templatesFlow, focusState    │
└──────────────────────────────────────┬─────────────────────────────────────┘
                                       ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                  Room Local SQLite Database (Zero Battery)                 │
│   TaskEntity (Inbox / Timeless / Timed / Bubble)  ◄──►  SubtaskEntity      │
│   DayTemplateEntity (Folder-Tabbed Blueprints)                             │
└────────────────────────────────────────────────────────────────────────────┘
```
