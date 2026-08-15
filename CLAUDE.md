# Project instructions

Digital List — a notes-and-tasks Android app. `com.stackpointer.list`. (Working name in the
original design handoff was "Keepsake"; the shipping app name is "Digital List".)

## Source of truth

The design lives in `design-handoff/`. Read `README.md`,
`DESIGN_TOKENS.md`, `SCREENS.md` and `DATA_MODEL.md` before writing UI code. The HTML files in
`design/` are references, not code to port — never embed them or copy their markup. Build
`SCREENS.md` **11–30** (the docked capture-bar pattern); screens 01/02/05/07 are superseded and
must not be built; 03/04/06/08/09/10 are current/supplementary.

## Stack

Kotlin, Jetpack Compose, Material 3 including the Expressive APIs, Room, Hilt, Coroutines/Flow,
Navigation Compose. minSdk 31. No other UI or design libraries; no XML layouts, with one narrow,
deliberate exception: `RemoteViews` layouts for the notification-bar feature below (a platform
constraint — `RemoteViews` cannot host Compose content).

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

## New scope beyond the design handoff: notification bar & pin

The handoff does not cover this — it was specced separately with the client. Two independent
`ItemEntity` booleans, distinct from the existing in-app `isPinned` (pin-to-top-of-list):

- `isShownInNotificationBar` — a normal, swipe-dismissible status notification for the item.
- `isPinnedToNotification` — an ongoing, non-dismissible (`setOngoing(true)`) status
  notification. If both flags are true, render once, ongoing (pinned implies shown).

Both post to a dedicated `Pinned` notification channel (`IMPORTANCE_LOW`, no sound), separate
from the alarm/reminder channels (Soft/Medium/Insistent). Checklists render through a small,
tightly-scoped `RemoteViews` layout with one `PendingIntent` per sub-item row, calling the *same*
repository method the in-app checklist UI uses — Room is the single source of truth, never a
separate "sync" path. Swipe-dismissing a "shown" (non-ongoing) notification must flip
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
capture sheet (net-new — not in the original 30 screens) next to the drag handle, disabled until
the draft has been saved once and has a persisted id.

Full technical design (data model, `PinnedNotificationManager`, migration, ID partitioning,
boot-resync) is in the build plan history; ask the client if anything here is ambiguous rather
than inventing behaviour.
