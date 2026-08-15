# Handoff: Keepsake — notes & tasks (Android)

Package for a developer using Claude Code. Everything needed to build the app is in this folder.

Read the files in this order:

1. `README.md` (this file) — what to build, in what stack, with what architecture
2. `DESIGN_TOKENS.md` — colour, type, shape, elevation and motion values, ready to paste into a Compose theme
3. `SCREENS.md` — all 30 screens, one entry each: purpose, layout, components, exact copy
4. `DATA_MODEL.md` — entities, relations, queries, reminder scheduling
5. `BUILD_PLAN.md` — suggested milestone order
6. `CLAUDE.md` — drop this at the root of the new repo; it is the persistent instruction file for Claude Code
7. `design/` — the original HTML design files (reference only, see below)
8. `screenshots/` — rendered images of the design canvas

## Overview

Keepsake is a personal notes-and-tasks app for Android. One capture surface takes anything the user
types — a thought, a task, a checklist — and the same item can be given a time trigger, a place
trigger, a recurrence rule, sub-items, a collection and a star. Items are read back through saved
views (Today, Scheduled, Starred, Place, No alert, Completed) rather than through folders.

The design is Material 3 Expressive throughout: a green-seeded dynamic colour scheme, the M3 type
scale with emphasized styles, the six-step shape scale with shape morphing on state change,
tonal-first elevation, spring-based motion, and the expressive component set (floating navigation
bar, FAB menu, docked search bar, floating toolbar, split button, button groups, loading indicator).

## About the design files

The files in `design/` are **design references authored in HTML** — prototypes that show intended
look, layout and behaviour. They are not production code and must not be ported, transpiled or
embedded in a WebView.

The task is to **recreate these designs natively in a new Android project** using the stack below.
Where the HTML and this document disagree, this document wins; where this document is silent, take
the measurement from the HTML.

## Fidelity

**High fidelity.** Colours, type, spacing, corner radii, elevation and copy in `SCREENS.md` and
`DESIGN_TOKENS.md` are final. Reproduce them exactly. Where a Material 3 component already provides
the specified appearance, use the component and its theme values rather than hard-coding a
dimension — the numbers given are the M3 defaults for that component unless flagged otherwise.

The device frame, the canvas background and the annotation captions in the HTML are presentation
scaffolding for the design review. They are not part of the app.

## Stack and project setup

Decided with the client; do not substitute without asking.

| Decision | Value |
| --- | --- |
| Language | Kotlin |
| UI | Jetpack Compose |
| Design library | `androidx.compose.material3` including the Material 3 Expressive APIs |
| Package / application id | `com.stackpointer.list` |
| minSdk | 31 |
| targetSdk / compileSdk | latest stable |
| Persistence | Room, local only, no account |
| Async | Coroutines + Flow |
| DI | Hilt |
| Navigation | Navigation Compose, single Activity |
| Build | Gradle Kotlin DSL with a version catalog |

Notes on the Expressive APIs:

- They are alpha. Pin explicit versions in the version catalog, allow the opt-in annotation
  (`@OptIn(ExperimentalMaterial3ExpressiveApi::class)`) at the call sites, and do not scatter alpha
  types through the domain layer.
- If a specific expressive component is missing from the version you resolve, build the closest
  equivalent from stable Material 3 and leave a `// TODO(expressive):` comment naming the intended
  component. Do not silently change the design.
- Theme with `MotionScheme.expressive()`. Do not hand-write durations or easing curves where a
  motion scheme token exists.

## Scope

**In scope for the first build**

- Notes, tasks, checklists — one item model, one capture sheet
- Saved views with live counts: Today, Scheduled, Starred, No alert, Completed
- Home with view tiles, capture bar and the grouped item list
- Time triggers, all-day items, early alerts, per-item alert type
- Recurrence (daily, weekly with weekday selection, monthly, yearly, custom interval, end condition)
- Collections and templates ("Try these out")
- Search, selection mode, recycle bin with 30-day retention
- Settings
- Light and dark schemes, dynamic colour on the system palette

**Out of scope for the first build** — design them into the model, but do not implement

- Place / geofence reminders (screens 14, 20, 21, and the Place tile and place mode of the capture
  sheet). Keep the data model fields and render the UI as disabled or hidden behind a flag.
- Voice capture (the mic button in the capture bar). Show the button, no-op or hide it behind the
  same flag.
- Images attached to notes (image mode in the capture sheet).
- Home-screen widget.
- Account sync. The Settings screen shows account, sync and shared-collection rows; keep them in the
  layout but disable them and mark them "Not available yet", or hide behind the flag.

Put these behind one `Features` object of compile-time booleans so they can be switched on later
without a refactor. The data layer must be written behind repository interfaces so that a sync
backend can be added later without touching the UI or the view models.

## Architecture

Single-module app to start, with clean package boundaries. Split into Gradle modules only if the
build gets slow.

```
com.stackpointer.list
├── data
│   ├── local        Room database, entities, DAOs, type converters
│   ├── repository   ItemRepository, CollectionRepository, TemplateRepository, SettingsRepository
│   └── prefs        DataStore for settings
├── domain
│   ├── model        Item, Trigger, Recurrence, Collection, Template, SavedView
│   └── usecase      Bucketing, recurrence expansion, next-occurrence, view counts
├── notification     AlarmScheduler, BootReceiver, AlarmReceiver, notification channels
├── ui
│   ├── theme        Color.kt, Type.kt, Shape.kt, Motion.kt, KeepsakeTheme
│   ├── components   ItemRow, SectionHeader, ViewTile, CaptureBar, CaptureSheet, ...
│   └── screens      home, today, scheduled, starred, noalert, completed, detail,
│                    editor, search, collections, templates, bin, settings
└── MainActivity.kt
```

- One `ViewModel` per screen, exposing a single immutable `UiState` via `StateFlow`, with events as
  method calls. No state hoisting above the screen composable other than navigation.
- Composables receive plain data and lambdas; no `ViewModel` references below screen level.
- Repositories return `Flow`; all list bucketing and count derivation happens in the domain layer so
  it can be unit tested without Compose.

## Interactions and behaviour

Motion values are in `DESIGN_TOKENS.md`. Behaviour that applies across screens:

- **Capture.** The capture bar is docked above the navigation bar on every list screen. Tapping it
  opens the capture sheet (screens 17–23) as a modal bottom sheet, keyboard already up. The sheet
  has five modes selected by a connected icon-button group: time, place, checklist, image, label.
  A body added to a title turns the item into a note. The confirm FAB commits.
- **Prefill by context.** The capture bar placeholder and the prefilled trigger depend on the view:
  "Add a note or task" on Home, "Add to today" in Today (due today), "Add a scheduled task" in
  Scheduled, "Add a place reminder" in Place.
- **Completing an item.** Check target morphs circle → squircle on the spatial-fast spring, the
  strike-through fades in on effects-default, the row leaves its bucket and lands in Completed with
  the neighbours springing to close the gap. A snackbar offers Undo.
- **Opening an item.** Container transform from the tapped card into the detail or editor screen,
  spatial default; radius and shadow interpolate.
- **Editing.** The note editor autosaves; there is no Save button, only a "Saved just now" line.
- **Selection mode.** Long-press any row enters selection mode: the top app bar becomes a selection
  bar with a close button, the count, and the bulk actions for that screen.
- **Overdue.** Anything past its trigger reads in error colour *and* in words ("Was due Monday",
  "Overdue · Mon"). Never colour alone.
- **Empty states.** Every list has one; screen 09 is the pattern — shape motif, headline small,
  supporting text, one tonal action.
- **Pull to refresh** shows the expressive loading indicator (screen 10). With local-only storage it
  simply re-reads; keep it, it is where sync will attach.
- **Back.** Sheets and menus dismiss on back and on scrim tap. Detail screens pop.
- **Accessibility.** 48 dp minimum touch target on every icon button, checkbox and row. Content
  descriptions on all icon-only controls. Layouts grow vertically at 200% font scale; snippets clamp
  by line count, never by fixed height. Test with TalkBack on the Home, capture sheet and detail
  screens at minimum.

## State

Per-screen `UiState` shapes are implied by `SCREENS.md`. The state that crosses screens:

- Current saved view and its filter
- Selection mode: set of selected item ids, plus the actions valid for the current screen
- Capture sheet: open/closed, mode, draft item (title, body, trigger, recurrence, sub-items,
  collections, alert type, early alert), and the view that opened it
- Snackbar queue with undo payloads (complete, delete, restore, move)
- Settings: default alert type, all-day alert time, preset visibility, theme
- Sync placeholder state (idle / refreshing) so the loading indicator has something to bind to

## Assets

- **Icons.** Material Symbols Rounded. Every glyph name used by the design is listed per screen in
  `SCREENS.md`. Use `androidx.compose.material.icons` where the glyph exists; otherwise add the
  Symbols font or the individual vector drawables. Filled variants are called out where used
  (`star`, `check_circle`).
- **Fonts.** Roboto Flex for all text, with Roboto as the fallback. The design's monospace captions
  are annotation only and do not appear in the app.
- **Images.** None. The image-mode thumbnail in screen 22 is a placeholder for user content.
- **App icon and branding.** Not designed. Ask the client before inventing one.

## Files in this package

| Path | What it is |
| --- | --- |
| `design/Notes & Tasks - Material 3 Expressive.dc.html` | The full design canvas, all 30 screens plus the design-system documentation. Open in a browser. |
| `design/android-frame.jsx` | The device frame used to present the screens. Presentation only. |
| `design/support.js` | Runtime for the HTML design files. Presentation only. |
| `screenshots/` | Rendered images of the canvas for quick visual reference. |
