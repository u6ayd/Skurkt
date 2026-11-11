# Strukt ⚡

> **A Neo-Brutalist Visual Day & Sequence Planner for Android**  
> Inspired by **Structured** and **Learnify**, designed for phones and optimized for the **Redmi Pad Pro 5G** tablet.

---

## ✨ Features

- **Hybrid Timeline Spine**:
  - Seamless vertical continuous timeline with zero gaps.
  - **Timed Blocks** with clock-based scheduling (`08:00 – 08:45`).
  - **Timeless Workflow Steps** numbered by sequence position (`#1`, `#2`, `#4`, `#6`, etc.).
  - **All-Day / Top Bubbles**: Floating top capsules for recurring or open-ended habits (e.g. *Structure Tomorrow*, *Water Plants*, *Call Mum*) with quick action menus on tap and long-press.
  - **Split Capsules & Dotted Break Loops**: Tasks interrupted by a break smoothly divide into top and bottom half-oval capsules joined by a roller-coaster dotted loop.

- **Learnify-Inspired Folders & Sections**:
  - Categorize workflows into custom folders (e.g. *Physics*, *Mathematics*, *Marketing*, *Computer Science*) and sections (*Core*, *Homework*, *Labs*).
  - Clean card layouts with progress bars, task counts, and color badges.

- **Inbox & Brain Dump**:
  - Quick-capture pane for unscheduled thoughts and ideas.
  - 1-tap scheduling modal to promote inbox items directly to the daily timeline or assign them as Timeless steps.

- **Focus Mode**:
  - Distraction-free countdown timer with smooth session tracking.
  - Supports pause/resume session recording directly onto the timeline spine.

- **Tablet Split-View (Redmi Pad Pro 5G)**:
  - Responsive two-column / three-pane layout in landscape.
  - Permanent dark navigation rail with high-contrast active badges.
  - Inbox side-pane alongside the interactive timeline workspace.

- **Aesthetic & Design System**:
  - Clean Neo-Brutalist design language with crisp `1.5.dp` solid borders, custom Outfit typography, and subtle paper dot-grid background.
  - 100% device-local persistence using Room SQLite with zero background battery drain.

---

## 🛠 Tech Stack

- **Platform**: Android (minSdk 26, targetSdk 35)
- **Language**: Kotlin 2.0+
- **UI Toolkit**: Jetpack Compose + Material Design 3 (M3)
- **Architecture**: MVVM (Model-View-ViewModel) + Kotlin Coroutines & StateFlow
- **Local Database**: Room SQLite (Entity, DAO, Repository)
- **Asynchronous / Reactive Flow**: Kotlin Flow + Lifecycle Compose
- **Build System**: Gradle 8.9 (Kotlin DSL)

---

## 🚀 Building & Running

### Prerequisites
- Android Studio Ladybug / Koala or later
- JDK 17 (Temurin recommended)
- Gradle 8.9

### Local Build (Command Line)
```bash
# Clone the repository
git clone https://github.com/<your-username>/<repo-name>.git
cd <repo-name>

# Build Debug APK
gradle assembleDebug

# Output APK path:
# app/build/outputs/apk/debug/app-debug.apk
```

### GitHub Actions CI
A GitHub Actions workflow is provided at `.github/workflows/build.yml`. On every push to `main` or tag creation (`v*`), it builds the debug APK and publishes it as a downloadable release artifact (`Skurkt-Debug-APK`).

---

## 📱 Navigation & Controls Summary

| Gesture / Control | Action |
|---|---|
| **Tap timeline task** | Opens task details, subtask checklist, focus timer, and sequence `#` adjuster |
| **Tap `+` / `-` on Step `#`** | Adjusts timeless sequence placement (e.g. moves `#4` to `#6`) |
| **Tap `Split with Break Loop`** | Divides task into top & bottom half-ovals connected by a dotted break loop |
| **Tap Top Bubble** | Opens bubble action modal (Mark Done / Edit / Delete) |
| **Long-Press Top Bubble** | Quick-access context options |
| **Swipe Calendar Week Strip** | Moves between past and upcoming weeks smoothly |
| **Bottom Navigation Bar** | Switch between Inbox, Timeline Flow, Folders, and Focus screens |

---

## 📄 License
This project is for personal use and study. All rights reserved.
