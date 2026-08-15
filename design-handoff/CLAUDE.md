# Project instructions

Keepsake — a notes-and-tasks Android app. `com.stackpointer.list`.

## Source of truth

The design lives in `design-handoff/`. Read `README.md`, `DESIGN_TOKENS.md`,
`SCREENS.md` and `DATA_MODEL.md` before writing UI code. The HTML files in `design/` are references,
not code to port — never embed them or copy their markup.

## Stack

Kotlin, Jetpack Compose, Material 3 including the Expressive APIs, Room, Hilt, Coroutines/Flow,
Navigation Compose. minSdk 31. No other UI or design libraries; no XML layouts.

## Design rules

- Every colour comes from `MaterialTheme.colorScheme`, every text style from
  `MaterialTheme.typography`, every corner from `MaterialTheme.shapes`. No literal hex, no literal
  `sp`, no ad-hoc `RoundedCornerShape` in screen code.
- Motion comes from `MotionScheme.expressive()`. No hand-written durations or easing curves.
- Elevation is tonal. Only the FAB, floating toolbar, navigation bar, menus, sheets and the capture
  bar cast a shadow.
- 48 dp minimum touch target. Content descriptions on every icon-only control.
- Status is never carried by colour alone.
- Match the design exactly. If a spec is missing, take the value from the HTML; if it is still
  unclear, ask rather than invent.

## Code rules

- One ViewModel per screen, a single immutable `UiState` exposed as `StateFlow`, events as methods.
- Composables take data and lambdas; no ViewModel below screen level.
- Bucketing, counts and recurrence maths live in the domain layer with unit tests.
- Repositories are interfaces. Nothing in `ui` or `domain` may reference Room types.
- Deferred features (place reminders, voice, images, sync, widget) sit behind flags in one
  `Features` object. Do not delete their data-model fields.
- Add a `@Preview` for every component and screen, using the seed data.
