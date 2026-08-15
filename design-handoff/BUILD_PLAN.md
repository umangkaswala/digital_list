# Build plan

Suggested order. Each milestone ends with something runnable on a device.

**M0 — Project skeleton**
New Compose project, `com.stackpointer.list`, minSdk 31, version catalog, Hilt, Navigation Compose,
Room with schema export. Empty single Activity that renders the theme.

**M1 — Theme**
`Color.kt`, `Type.kt` (Roboto Flex, the full scale incl. emphasized styles), `Shape.kt`, `Motion.kt`
with `MotionScheme.expressive()`, dynamic colour with the green fallback, light and dark. A preview
screen that renders every token so it can be compared against `design/` side by side. Do this before
any screen — everything downstream depends on it.

**M2 — Data layer**
Entities, DAOs, repositories behind interfaces, seed data for previews, the saved-view queries and
their counts as Flows, recurrence maths with unit tests.

**M3 — Shared components**
`ItemRow`, `SectionHeader`, `ViewTile`, `CaptureBar`, floating navigation bar, empty state, snackbar
with undo, selection top bar. Build these as previewable composables against the seed data.

**M4 — Home and the saved views**
Screens 11, 12, 13, 15, 16, and the Completed view (08). Navigation between them. Live counts.

**M5 — Capture sheet**
Screens 17, 18, 19, 22 (checklist part), 23, plus the repeat picker (25) and the alert-type and
sort sheets (26). This is the heart of the app; budget accordingly.

**M6 — Detail and editor**
Screens 24 and 04, including the container transform and autosave.

**M7 — Reminders**
Alarm scheduling, the three notification channels, notification actions, boot rescheduling,
permission flows.

**M8 — Organizing and system**
Collections (07 list layout, 27 management), templates (28), search (06), recycle bin (29),
settings (30).

**M9 — Polish**
Shape morphing on completion, staggered entries, pull-to-refresh loading indicator, dark scheme
pass, TalkBack pass, 200% font-scale pass, empty states everywhere.

**Later (flagged off in the first build)**
Place reminders (14, 20, 21 and the place parts of 23, 27, 30), voice capture, image attachments,
account sync, home-screen widget.
