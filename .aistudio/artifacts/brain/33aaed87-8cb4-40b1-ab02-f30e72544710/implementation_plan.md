# Strukt — Learnify × Structured Refinement Plan (Section-by-Section Execution)

A focused visual and ergonomic refinement combining **Learnify's** crisp, shadow-free `1.5.dp` bordered cards, high-contrast warm pastels, and geometric rounded typography with **Structured's** visual timeline.

---

## User Review & Section-by-Section Execution Rule

> [!IMPORTANT]
> **Zero Code Modified Yet — Strict Section-by-Section Execution**:
> Per your instruction, **we will NOT build all phases in one turn**. When you click **Proceed**, I will execute **ONLY Section 1** (Visual Foundation, Font, Normal Lines Without Drop Shadows, Permanent Docked Navigation Bar & Symmetrical Inbox) and stop so you can inspect it in the preview before we touch Section 2.

### Why the PC Preview Looks Different from Your Redmi Pad Pro 5G
- The browser preview frame simulates a **compact phone screen (~360–411dp wide)**. On your **Redmi Pad Pro 5G (12.1" display)**, Android automatically scales the layout to use the tablet's wide screen real estate, and hardware `fullSensor` auto-rotation will rotate the app naturally whenever you turn your tablet horizontally or vertically.

---

## Section 1 — Visual Foundation, Learnify Font, Symmetrical Inbox & Permanent Bottom Bar *(Executed First on "Proceed")*

1. **Normal Crisp Lines & Subtle Paper Dot Background (No Drop Shadows)**:
   - Remove the black offset drop shadows from all cards, buttons, and icons. Use clean, uniform **`1.5.dp` solid dark-ink borders (`#18181B`)** with smooth, symmetrical rounded corners (`20.dp`), matching your **Learnify** reference image.
   - Make the background dot-grid smaller, lighter, and more subtle (`1.dp` soft dots) for a gentle paper feel without visual noise.
2. **Learnify Geometric Font (`Outfit`)**:
   - Install and apply **`Outfit`** (matching the clean, modern geometric headings and numbers in the *Learnify* screenshot) across the entire app, replacing the current font.
3. **Inbox Symmetry**:
   - Give the **`"Brain dump"`** input box and the **`+`** submit button identical height (`50.dp`), matching symmetrical corner curves (`18.dp` radius), and clean `1.5.dp` borders.
4. **Permanent Full-Width Straight Bottom Bar with Larger Icons**:
   - Replace the floating pill bar with a **permanent, full-width docked bottom bar** separated by a clean straight horizontal line (`1.5.dp`), with **larger (`26.dp`–`28.dp`) centered icons** (`Inbox`, `Timeline`, `Folders`, `Focus`) and a solid rounded highlight on the active tab so it never floats over tasks.
5. **Hardware Auto-Rotation (`fullSensor`)**:
   - Switch `AndroidManifest.xml` to pure `fullSensor` so rotating your Redmi Pad Pro 5G (or clicking the emulator's rotate button) rotates the app automatically without needing any on-screen rotate button.

---

## Section 2 — Swipeable Week Calendar, Clean Timeline Spine & Custom Time/Duration *(Executed After Section 1 Review)*

1. **Ultra-Clean Month Header & Swipeable Week Strip**:
   - Top header displays **only the Month name** (e.g., **`November`**) in the new `Outfit` font.
   - **Remove** the `<` `>` arrows, **remove** the `Today` button (so the header never shifts down), and **remove** the rotate button.
   - Make the **7-Day Week Strip horizontally swipeable (`HorizontalPager`)** so you simply **slide left or right** to browse past and future weeks; Today's date is indicated purely by color.
2. **De-Duplicated Timeline Task Rows & Continuous Spine Lines**:
   - **Remove duplicate time text** on the right side of the timeline icon: since `08:00` and `08:15` are already shown on the left of the capsule, the right side will show **only the Task Title** (plus a tiny `↻` icon if recurring).
   - **Continuous & Break Connector Lines**: Always draw the vertical connector line between tasks on the spine—a **solid line** when tasks are back-to-back, and a **clean dotted line** (with no noisy text) when there is unscheduled time or a break between tasks.
3. **Pause/Resume Details Only on Tap**:
   - When you pause and resume a task in Focus Mode, show a clean **dotted break line** on the spine itself, and move the `"Started 14:00 • Paused 14:37 • Continued 14:47"` text **inside the popup window when you tap the task**.
4. **Custom Time & Custom Duration Picker**:
   - Inside the **Time & Duration** popup, add a **Custom Time (`HH : MM`)** selector (any hour and minute) and a **Custom Duration** control (any minute/hour value, not limited to 15m steps or 2 hours).

---

## Section 3 — Learnify-Style Symmetrical Folders & Sections *(Executed After Section 2 Review)*

1. **Symmetrical Folder Cards (No Protruding Left Tab & No Duplicate Labels)**:
   - Restyle Folder cards to match the **Learnify "My courses" cards**: a single symmetrical rounded rectangle (`22.dp` radius, `1.5.dp` border), with the code pill (`PHY 12`) neatly **inside** the top-left of the card, the Folder Title (`Physics`) once, the progress bar (`1/3 tasks`), and a clean action button—removing the overarching top-left folder tab and the duplicate `PHYSICS` / `CHEMISTRY` label at the bottom.
   - Increase vertical and grid spacing (`18.dp`) between folder cards so they breathe cleanly on both phone and tablet screens.

---

## Section 4 — Final Focus Polish & Tablet Density Tuning *(Executed After Section 3 Review)*

1. **Focus Screen & Tablet Multi-Pane Polish**:
   - Apply the Learnify card styling, larger icon hierarchy, and responsive tablet spacing across Focus Mode and the Landscape Split-View for the **Redmi Pad Pro 5G**.

---

## Visual & Structural Overview

```
┌──────────────────────────────────────────────────────────────────────────┐
│  SECTION 1: Outfit Font + 1.5dp Clean Lines (No Drop Shadows)            │
│  • Subtle Paper Dots • Symmetrical "Brain dump" • Permanent Bottom Bar   │
├──────────────────────────────────────────────────────────────────────────┤
│  SECTION 2: Swipeable Week Strip + Clean Spine + Custom Time/Duration    │
│  • "November" Only • Slide Weeks • Left-Only Times • Dotted Break Lines  │
├──────────────────────────────────────────────────────────────────────────┤
│  SECTION 3: Learnify Symmetrical Folder Cards (Single Label + Spaced)    │
├──────────────────────────────────────────────────────────────────────────┤
│  SECTION 4: Focus Popup Details & Redmi Pad Pro 5G Tablet Polish         │
└──────────────────────────────────────────────────────────────────────────┘
```
