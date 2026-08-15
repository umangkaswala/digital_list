# Data model

One item type covers notes, tasks and checklists. What a user calls the item is derived, not stored
as a separate table: a body makes it a note, sub-items make it a checklist, a trigger makes it a
reminder. Keep it that way — the capture sheet depends on an item being able to become any of these
mid-typing.

## Entities (Room)

### `items`

| Column | Type | Notes |
| --- | --- | --- |
| `id` | String (UUID) | PK |
| `title` | String | required |
| `body` | String? | presence makes it read as a note |
| `isCompleted` | Boolean | |
| `completedAt` | Instant? | shown as "Completed 8:04 AM" |
| `isStarred` | Boolean | |
| `isPinned` | Boolean | |
| `isArchived` | Boolean | |
| `deletedAt` | Instant? | non-null = in recycle bin; purge after 30 days |
| `triggerType` | enum | `NONE`, `TIME`, `PLACE` |
| `dueAt` | Instant? | |
| `isAllDay` | Boolean | all-day items alert at the configured time |
| `earlyAlertMinutes` | Int? | null = no early alert |
| `alertType` | enum | `SOFT`, `MEDIUM`, `INSISTENT` |
| `recurrenceId` | String? | FK → `recurrences` |
| `placeId` | String? | FK → `places`, deferred |
| `placeTrigger` | enum? | `ARRIVE`, `LEAVE`, deferred |
| `placeWindow` | enum? | `ANYTIME`, `MORNING`, `AFTERNOON`, `EVENING`, `NIGHT`, deferred |
| `sortOrder` | Int | manual ordering |
| `createdAt` / `updatedAt` | Instant | `updatedAt` drives "Last modified" and "Edited 2h ago" |

### `sub_items`

`id`, `itemId` (FK, cascade delete), `text`, `isCompleted`, `sortOrder`.
Drives "1 of 3 done" and the drag-reorderable checklist rows.

### `collections`

`id`, `name`, `iconKey` (`work`, `person`, `home`, `flight`, `group`…), `colorKey?`,
`isShared` (deferred — shared collections can be left but not deleted), `sortOrder`.

### `item_collections`

Join table: `itemId`, `collectionId`, composite PK. An item can be in several collections.

### `recurrences`

| Column | Notes |
| --- | --- |
| `id` | PK |
| `freq` | `DAILY`, `WEEKLY`, `MONTHLY`, `YEARLY`, `CUSTOM` |
| `interval` | every N units, default 1 |
| `weekdays` | bitmask or comma list, for `WEEKLY` |
| `monthDay` | for `MONTHLY` ("on the 1st") |
| `endType` | `NEVER`, `ON_DATE`, `AFTER_COUNT` |
| `endDate` / `endCount` | |

Store the rule, not the expansion. Derive the next occurrence on read. Completing a recurring item
advances `dueAt` to the next occurrence and writes a completion record rather than marking the item
done permanently.

### `templates`

`id`, `title`, `description`, `iconKey`, and a serialized draft (trigger, recurrence, sub-items) used
to prefill the capture sheet. Seed the six templates from screen 28 on first run.

### `places` — deferred

`id`, `name`, `iconKey`, `latitude`, `longitude`, `radiusMeters` (default 200), `address`.

### Settings (DataStore, not Room)

`defaultAlertType`, `allDayAlertTime` (default 09:00), `showPresetTimes`, `showPresetPlaces`,
`theme`, and placeholders for the deferred sync switches.

## Saved views

Each view is a query plus a bucketing rule. Counts on the Home tiles come from the same queries, so
implement them once and observe them as Flows.

| View | Query | Buckets |
| --- | --- | --- |
| Today | `dueAt` within today, or overdue | Past, Soon, Completed |
| Scheduled | `triggerType = TIME`, not completed | Past, Today, Next 7 days, Later |
| Starred | `isStarred`, not deleted | none |
| Place | `triggerType = PLACE` | by place |
| No alert | `triggerType = NONE`, not completed | none |
| Completed | `isCompleted` | Today, Earlier this week, older |
| Recycle bin | `deletedAt != null` | by days remaining (30 − age) |

The Today tile shows "done/total" ("1/5"); the others show a single count. Completed shows the
all-time count ("308").

## Reminder scheduling

- `AlarmManager` with `setExactAndAllowWhileIdle` for the item's `dueAt` and, separately, for the
  early alert. minSdk 31 means `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` must be declared and the
  `canScheduleExactAlarms()` path handled with a prompt.
- Request `POST_NOTIFICATIONS` at the first reminder creation, not at launch.
- Three notification channels matching the alert types: Soft (no sound), Medium (sound once),
  Insistent (repeats until acted on — re-post on a short interval and cancel on action).
- Notification actions: Complete, Snooze, Open.
- Reschedule everything on `BOOT_COMPLETED` and after a timezone change.
- All-day items alert at the `allDayAlertTime` setting.
- Recurring items schedule only the next occurrence; the alarm receiver schedules the one after it.

## Undo

Complete, delete, restore and move all go through a repository method that returns an undo token,
surfaced as a snackbar. Undo must restore the exact prior state including `sortOrder` and bucket.

## Testing

- Unit tests for recurrence next-occurrence maths (including month-end and weekday sets), bucketing,
  and the 30-day purge.
- Room migration tests from the first schema onward; export schemas from the start.
- Compose UI tests for the capture sheet mode switching and for completing an item with undo.
