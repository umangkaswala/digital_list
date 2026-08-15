# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Digital List — a notes-and-tasks Android app. `com.stackpointer.list`. (Working name in the
original design handoff was "Keepsake"; the shipping app name is "Digital List".)

## Commands

Single Gradle module (`:app`). Always run from the repo root.

```
./gradlew assembleDebug                 # build the debug APK
./gradlew installDebug                  # build + install on a connected device/emulator
./gradlew :app:compileDebugKotlin       # fast compile-only check
./gradlew test                          # all JVM unit tests (app/src/test)
./gradlew testDebugUnitTest --tests "com.stackpointer.list.SomeTest"   # a single test class
./gradlew connectedAndroidTest          # instrumented tests (app/src/androidTest) — needs a running device/emulator
./gradlew lint                          # Android Lint (no ktlint/detekt configured)
```

Windows-specific gotchas seen on this project:
- If any `./gradlew` invocation fails with `java.io.IOException: Unable to establish loopback
  connection`, the JDK's daemon IPC pipe is choking on a long/spaced `TEMP`/`TMP` path (e.g. a
  profile directory with a space in the username). Point `TEMP`/`TMP` (and
  `GRADLE_OPTS=-Djava.io.tmpdir=...`) at a short path like `C:\gtmp` before retrying.
- If `java`/`gradlew` isn't resolvable, set `JAVA_HOME` explicitly — Android Studio bundles a
  JDK at `<Android Studio install dir>\jbr` (e.g. `C:\Program Files\Android\Android Studio\jbr`).
- `local.properties`' `sdk.dir` must use forward slashes even on Windows, or AGP's SDK
  location check throws `IOException: Invalid file path`.
- AGP 9 has Kotlin support built in — do not add the `org.jetbrains.kotlin.android` plugin
  back; only `org.jetbrains.kotlin.plugin.compose` (the Compose compiler plugin) is needed
  alongside `com.android.application`.

Room schema JSONs are exported to `app/schemas/` (`room { schemaDirectory(...) }` in
`app/build.gradle.kts`) — commit them; migration tests read them.

## Architecture

**Source of truth for UI/UX is `design-handoff/`** — read `README.md`, `DESIGN_TOKENS.md`,
`SCREENS.md` and `DATA_MODEL.md` there before writing UI or data-layer code. The HTML files
under `design-handoff/design/` are references only (never port or embed their markup). Build
`SCREENS.md` **11–30** (the docked capture-bar pattern) — screens 01/02/05/07 are superseded
and must not be built; 03/04/06/08/09/10 are current/supplementary.

Stack: Kotlin, Jetpack Compose, Material 3 including the Expressive APIs (alpha — pin explicit
versions, opt in per call site), Room, Hilt, Coroutines/Flow, Navigation Compose, single
Activity. minSdk 31. No other UI or design libraries; no XML layouts, with one narrow,
deliberate exception: `RemoteViews` for the notification-bar feature below (`RemoteViews`
cannot host Compose content).

Package layout under `com.stackpointer.list` (single module, split further only if the build
gets slow):

```
data/
  local        Room database, entities, DAOs, type converters, migrations
  repository   *RepositoryImpl classes — the only place allowed to reference Room types
  prefs        DataStore for settings
domain/
  model        Item, Trigger, Recurrence, Collection, Template, SavedView
  usecase      bucketing, recurrence expansion, next-occurrence, view counts — unit tested
notification/  AlarmScheduler, BootReceiver, AlarmReceiver, notification channels,
               PinnedNotificationManager (see below)
ui/
  theme        Color/Type/Shape/Motion/Theme.kt (built — DigitalListTheme)
  components   shared composables: ItemRow, SectionHeader, ViewTile, CaptureBar, ...
  screens      home, today, scheduled, starred, noalert, completed, detail, editor,
               search, collections, templates, bin, settings
```

Only `ui/theme` exists so far; the rest is the target layout for upcoming milestones. Repos
are interfaces — nothing in `ui` or `domain` may reference Room types. One `ViewModel` per
screen exposing a single immutable `UiState` via `StateFlow`, events as method calls;
composables take plain data + lambdas, no `ViewModel` reference below screen level. Deferred
features (place reminders, voice capture, images, account sync, home-screen widget) are built
into the data model and UI but gated behind one `Features` object of compile-time booleans —
don't delete their fields, don't implement their behaviour yet.

### Design rules

- Every colour from `MaterialTheme.colorScheme`, every text style from
  `MaterialTheme.typography`, every corner from `MaterialTheme.shapes`. No literal hex, no
  literal `sp`, no ad-hoc `RoundedCornerShape` in screen code.
- Motion from `MaterialTheme.motionScheme` (`MotionScheme.expressive()`, set once in
  `DigitalListTheme`). No hand-written durations or easing curves.
- Elevation is tonal. Only the FAB, floating toolbar, navigation bar, menus, sheets and the
  capture bar cast a shadow.
- 48dp minimum touch target; content descriptions on every icon-only control. Status is never
  carried by colour alone.
- Match the design exactly. If a spec is missing, take the value from the HTML; if still
  unclear, ask rather than invent — and if a specific Expressive component is missing from the
  resolved Material3 version, build the closest stable-M3 equivalent with a
  `// TODO(expressive):` comment naming the intended component, rather than silently changing
  the design.

### New scope beyond the design handoff: notification bar & pin

The handoff does not cover this — it was specced separately with the client. Two independent
`ItemEntity` booleans, distinct from the existing in-app `isPinned` (pin-to-top-of-list):

- `isShownInNotificationBar` — a normal, swipe-dismissible status notification for the item.
- `isPinnedToNotification` — an ongoing, non-dismissible (`setOngoing(true)`) status
  notification. If both flags are true, render once, ongoing (pinned implies shown).

Both post to a dedicated `Pinned` notification channel (`IMPORTANCE_LOW`, no sound), separate
from the alarm/reminder channels (Soft/Medium/Insistent). Checklists render through a small,
tightly-scoped `RemoteViews` layout with one `PendingIntent` per sub-item row, calling the
*same* repository method the in-app checklist UI uses — Room is the single source of truth,
never a separate "sync" path. Swipe-dismissing a "shown" (non-ongoing) notification must flip
`isShownInNotificationBar` back to `false` via a `setDeleteIntent` receiver, or the next
unrelated Room emission would silently repost it. Every pinned/shown item gets its own
notification, grouped under one summary notification. Notification IDs for this feature must
never collide with alarm-triggered notification IDs for the same item (both can be visible for
one item at once) — partition the ID space, e.g. by reserving a high bit.

Toggle UI: labelled overflow-menu rows (not bare icon buttons), using Material Symbols
`keep`/`keep_off` for "pin to notification" and `notifications`/`notifications_none` for "show
in notification bar" — deliberately not `push_pin`, which the design already uses for the
unrelated in-app pin-to-top feature. Placement: detail screen (24) and editor (04) overflow
menus, the selection-mode bulk-action bar's overflow, and a new `more_vert` overflow on the
capture sheet (net-new — not in the original 30 screens) next to the drag handle, disabled
until the draft has been saved once and has a persisted id.
