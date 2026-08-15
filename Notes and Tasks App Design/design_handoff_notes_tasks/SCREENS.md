# Screens

30 screens. Numbering matches the labels on the design canvas: **01–10** are the original core flows
(canvas option `1a`), **11–30** the full feature set (option `2a`). Where the two disagree, **11–30
wins** — the client's change was to replace the FAB menu with a docked capture bar.

All copy below is the exact copy in the design. Sample data (names, dates, counts) is illustrative;
reproduce it in previews and screenshot tests, not in the shipped empty database. Reference date in
the design is Thursday 14 August.

Shared elements referenced throughout:

- **Item card** — `surfaceContainerLow`, corner 16, padding 14/16, leading 24 dp checkbox
  (2 dp `onSurfaceVariant` stroke, corner 4) or filled `check_circle` in primary when complete,
  title Title Medium, metadata Label Small `onSurfaceVariant`, optional recurrence line with the
  `repeat` glyph at 14 dp, optional trailing filled `star` at 20 dp in `outline`.
- **Bucket header** — Title Medium emphasized + count in Label Medium `onSurfaceVariant`; error
  colour when the bucket is "Past" or "Overdue".
- **Capture bar** — `surfaceContainerLowest`, 56 tall, corner 28, elevation 2, inset 16, placeholder
  Body Large `onSurfaceVariant`, trailing 40 dp `primaryContainer` circle with `mic`.
- **Floating navigation bar** — 64 tall, corner 32, inset 16, elevation 3, three destinations
  (`sticky_note_2` Home, `task_alt` Tasks, `style` Collections). Only the active destination shows
  its label, inside a `secondaryContainer` pill; inactive items are 48 dp icon targets.

---

## Saved views

### 11 · Home — view tiles + capture bar

**Purpose.** The default screen. Scan what needs attention, jump to a saved view, capture anything.

**Layout.** Docked search bar (`surfaceContainerHigh`, 56, corner 28, leading `search`, trailing
`more_vert`, placeholder "Search notes and tasks"), padding 8/16/12. Then a 3-column grid of six
view tiles, gap 8, each 76 tall, corner 16, padding 12, icon top-left at 22 dp, label and count on
the baseline row. Then the grouped item list. Capture bar 56 dp above the nav pill (bottom 88); nav
pill at bottom 16.

**Tiles.** Today `today` `primaryContainer` "1/5" · Scheduled `schedule` `tertiaryContainer` "5" ·
Starred `star` filled `secondaryContainer` "2" · Place `location_on` `surfaceContainerHigh` "1" ·
No alert `notifications_off` `surfaceContainerHigh` "7" · Completed `done_all`
`surfaceContainerHigh` "308". Counts are live.

**List.** "Past 1" → "Send the lease addendum / Was due Monday · Work" (error checkbox and metadata).
"Today Thu 14 Aug · 3" → "Call mum / 1:30 PM · Personal / repeat Every day" with trailing star;
"Cook dinner / 8:00 PM · Home / repeat Every day".

**Capture bar copy.** "Add a note or task".

### 12 · Today view

**Purpose.** Everything due today, plus what is already done.

**Layout.** Small top app bar (`arrow_back`, spacer, `search`, `more_vert`), then a title block
padding 12/16/20 with "Today" at Headline Medium emphasized and "1 of 5 done" Body Medium on the
same baseline. List, then capture bar at bottom 16 (no nav bar on this screen).

**Buckets.** Past 1 · Soon 3 · Completed 1.
Rows: "Take the vitamins / Yesterday, 11:30 PM / repeat Every day" (past) · "Call mum / 1:30 PM ·
Personal" starred · "Review the export bug / 4:00 PM · Work · 2 subtasks" · "Cook dinner / 8:00 PM ·
Home" · completed "Pay the electricity bill / Today, 8:00 AM" struck through, with a third line
"Completed 8:04 AM" in `onSurfaceVariant` **not** struck through.

**Capture bar copy.** "Add to today" — new items default to today's date.

### 13 · Scheduled view

**Purpose.** Everything with a time trigger, ordered forward in time.

**Layout.** Small top app bar: `arrow_back`, title "Scheduled" (Title Large), `swap_vert` (sort),
`more_vert`. List, capture bar at bottom 16.

**Buckets.** Past 1 · Today Thu 14 Aug · 3 · Next 7 days 2 · Later 1.
Recurrence prints on its own line per row: "Every day", "Every week on Mon, Tue, Wed, Thu, Fri",
"Every month on the 1st". Rows: "Take the vitamins / Yesterday, 11:30 PM" · "Call mum / 1:30 PM" ·
"Cook dinner / 8:00 PM" · "Canteen lunch order / Mon, 17 Aug, 12:45 PM" · "Design review with Priya /
Fri, 15 Aug, 10:30 AM · Work" · "Check the payslip / 1 Sept, 9:00 AM".

**Capture bar copy.** "Add a scheduled task".

### 14 · Place view — *deferred*

**Purpose.** Items triggered by arriving at or leaving a place, grouped by place.

**Layout.** Small top app bar `arrow_back` / "Place" / `more_vert`. Permission banner card
(`surfaceContainerHigh`, corner 16, padding 16): `location_off` glyph, Body Medium "Turn on location
so place reminders can alert you when you arrive or leave.", actions right-aligned — text button
"Not now", tonal button "Settings". Then place groups with an icon in the header.

**Groups.** Home 1 → "Water the balcony plants / `login` Arriving · in the evening". Work 2 →
"Return the office key / `logout` Leaving · anytime"; "Print the signed lease / `login` Arriving · in
the morning". Car 1 → "Photograph the parking bay / `logout` Leaving · anytime" with a trailing
image thumbnail.

**Capture bar copy.** "Add a place reminder".

**Deferred.** Build the layout, keep it behind the place-reminders flag.

### 15 · Starred + overflow menu

**Purpose.** Starred items, and the app's global overflow menu shown open.

**Layout.** Small top app bar `arrow_back` / "Starred" / `search` / `more_vert`. Two cards: a task
"Call mum / Today, 1:30 PM · Personal" with filled star, and a note "Kitchen renovation" with a
trailing star and the snippet "Tile samples arrive Thursday. Ask about the matte finish."

**Menu.** Anchored top-right, elevation 2, corner 12, dividers between groups. Items in order:
`sync` Sync now · `check_box` Select · `swap_vert` Sort by · `style` Manage collections ·
`lightbulb` Try these out — divider — `delete` Recycle bin · `settings` Settings.

### 16 · No alert view + templates footer

**Purpose.** Items with no trigger — the pile that would otherwise be forgotten — and a nudge to add
one.

**Layout.** Small top app bar `arrow_back` / "No alert" / `more_vert`. Explanatory line, Body Medium
`onSurfaceVariant`: "Nothing here will alert you. Add a time or a place when you want one to."

Rows carry a trailing assist chip `alarm_add` "Add time": "Find a plumber / Home", "Read the tenancy
handbook / Home". A checklist card "Packing list" shows two sub-items (`check_circle` Passport
completed, `radio_button_unchecked` "Charger, EU adapter") and "1 of 3 done".

**Footer.** Collapsible "Try these out" strip with `expand_less`, showing one template card
(`lightbulb` "Return library books / 21 Aug", trailing `add`). A `keyboard_arrow_up` scroll-to-top
icon button sits above the capture bar. Capture bar: "Add a note or task".

---

## Capture — one sheet, five modes

Screens 17–23 are the same modal bottom sheet, corner 28, elevation 1, keyboard up. Structure from
the top: drag handle; title field (Title Large); optional body/second line; a connected icon-button
group of five modes — `schedule` time, `location_on` place, `checklist` checklist, `image` image,
`label` label — with a confirm FAB (`check`, `primaryContainer`) at the trailing end of that row;
then the mode's own content below.

### 17 · Capture sheet — typing

Title "Book the dentist", supporting line "Add a time, place or list below". No mode selected.
Adding a body turns the item into a note. Confirm FAB commits and dismisses.

### 18 · Time mode

Trigger shown as a removable input chip: "1 Sept, 9:00 AM · monthly" with `cancel`.
Rows below:

- `hourglass_empty` "All day" with a trailing switch
- `event` row with two fields: "Tue, 1 Sept" and "9:00 AM" (open date and time pickers)
- Preset assist chips: "In an hour", "7:00 AM", "3:00 PM", "10:00 PM"
- `notifications_active` "1 day before" with `arrow_drop_down` — opens screen 19
- `repeat` "Every month on the 1st" with `chevron_right` — opens screen 25
- `volume_up` "Alert · Medium" with `chevron_right` — opens the alert-type sheet in screen 26

### 19 · Early alert menu

Menu over the sheet, six options: "No early alert", "10 minutes before", "15 minutes before",
"1 hour before", "1 day before" (selected — primary text with a trailing `check`), "Custom…".

### 20 · Place mode — *deferred*

Trigger chip "Leaving: Car · anytime" with `cancel`. A `location_on` row shows the chosen place
("Car") and the trigger ("When I leave"). Preset places as filter chips: Home, Work, Current
location, `search` "Pick a place", plus a `settings` icon button opening place management. Below,
a map preview area captioned "Map preview · 200 m radius". Then `volume_up` "Alert · Medium".

### 21 · Place trigger dialog — *deferred*

M3 dialog, corner 28. Header `home` "Home". Radio group one: "When I arrive here" / "When I leave
here". Radio group two: "Anytime", "In the morning 4:00 AM – 12:00 PM", "In the afternoon
12:00 PM – 6:00 PM", "In the evening 5:00 PM – 12:00 AM", "At night 11:00 PM – 5:00 AM". Text
buttons "Cancel" and "Done" (Label Large emphasized).

### 22 · Checklist + image mode

Title "Weekend chores", a removable image thumbnail (`close` badge) — *image part deferred* — and
"3 items". Sub-item rows are 48 dp with a checkbox and a trailing `drag_indicator`: "Wash clothes
and sheets", "Vacuum the floors", "Take out the rubbish". Footer row `add` "Add an item".

### 23 · Collection mode

Title "Return the office key", trigger chip "Leaving: Work", collection chip "Work". A multi-select
list with a leading checkbox, a collection icon, the name and an item count: `work` Work 14 (checked)
· `person` Personal 21 · `home` Home 9 · `group` "Flat move / Shared with 2 people" 6. Footer row
`add` "New collection" creates one inline.

---

## Item detail and pickers

### 24 · Task detail

**Purpose.** Read one item and act on it.

**Layout.** Small top app bar: `arrow_back`, spacer, `star` toggle (filled when starred),
`more_vert`. Title "Call mum" at Headline Medium emphasized. Attribute rows, each a 48 dp row with a
leading 24 dp glyph, tappable to edit:

`event` Today, 1:30 PM · `repeat` Every day · `notifications_active` 10 minutes before ·
`volume_up` Medium · `label` Personal.

Footer line Body Medium `onSurfaceVariant`: "Last modified 14 Aug 2026".

**Floating toolbar.** Elevation 3, corner full, four labelled actions: `check` Complete, `edit` Edit,
`share` Share, `delete` Delete.

### 25 · Repeat picker

Modal sheet titled "Repeat". Radio list: "Never", "Every day", "Every week", "Every month on the
1st", "Every year on 14 Aug", "Custom interval…". Choosing "Every week" reveals a weekday toggle
group M T W T F S S directly beneath it. Then a row `event_available` "Ends" / "Never" with
`chevron_right`. Footer: text button "Cancel" and a split button "Save" + `expand_less`.

### 26 · Alert type + Sort by

Two compact radio sheets, shown side by side on the canvas.

**Sort by** — "Due date" (selected), "Recently edited", "Title, A to Z", "Manual order".

**Alert type** — each option carries supporting text: "Soft / Silent notification only",
"Medium / Sound once, then quiet", "Insistent / Repeats until you respond". Footer: text button
"Set as default", filled button "Done".

---

## Organizing and system screens

### 27 · Manage collections

Small top app bar `arrow_back` / "Manage collections" / `more_vert`. Rows: leading collection icon,
name, "N items · N due" as supporting text, trailing `drag_indicator` and `more_vert`.

`work` Work 14 items · 3 due · `person` Personal 21 items · 1 due · `home` Home 9 items ·
`group` "Flat move / Shared with Priya and Sam". Footer row `add` "New collection".

Note under the list, Body Medium `onSurfaceVariant`: "Shared collections can be left but not
deleted. Items you remove from a collection stay in your list." Shared rows have no Delete in their
overflow.

**PLACES** section (*deferred*): `home` "Home / 14 Bridge Street" and `work` "Work / Set a location",
each with a trailing `edit`.

### 28 · Try these out

Small top app bar `arrow_back` / "Try these out". Intro Body Medium: "Tap one to open it in the
capture sheet, already filled in."

Template cards, each with a leading `lightbulb`, a title, a description of the trigger it prefills,
and a trailing `add` button:

- "Walk with family / Every week on Sat"
- "Check the payslip / Every month on the 25th"
- "Parking photo / When you leave the car" *(deferred with place)*
- "Commuting essentials / Headphones, wallet / Every day at 7:00 AM, or when you leave home"
- "Weekend chores" with three unchecked sub-items: Wash clothes and sheets, Vacuum the floors, Take out the rubbish
- "Return library books / 21 Aug"

Tapping a card opens the capture sheet prefilled; the trailing `add` commits it directly.

### 29 · Recycle bin — selection mode

Selection top app bar: `close`, "1 selected", text button "Select all". Below it the screen title
"Recycle bin" and the retention notice "Items here are deleted for good after 30 days."

Grouped by days remaining, headers "28 DAYS LEFT" and "14 DAYS LEFT" in Label Medium uppercase.
Rows show the leading selection circle, the title, and "Type · deleted date": "Old lease — 2023 /
Note · deleted 12 Aug" (selected, with a trailing `description` glyph), "Confirm the courier pickup /
Task · deleted 29 Jul", "Old packing list / Checklist · deleted 29 Jul".

Bottom toolbar with two labelled actions: `restore_from_trash` Restore, `delete_forever` Delete now.

Purge on app start: anything older than 30 days is removed for good.

### 30 · Settings

Small top app bar `arrow_back` / "Settings". Grouped cards on `surfaceContainerLow`, corner 16, with
1 dp `outlineVariant` dividers between rows. Section labels in primary, uppercase, Label Medium.
Row title Body Large; current value or explanation as supporting text in Body Medium
`onSurfaceVariant`. Switches 52 × 32.

- *(ungrouped)* "Account and sync / a.mehta@gmail.com · synced 2 minutes ago" — *deferred, disable*
- "Sync on mobile data / Off — syncs on Wi-Fi only" — *deferred, disable*
- **PRESETS** — "Show preset times" switch · "Show preset places" switch *(place row deferred)*
- **ALERTS** — "Default alert type / Medium" · "Alert time for all-day items / On the day at 9:00 AM" ·
  "Dismiss on all devices / Dismissing an alert here dismisses it everywhere." switch *(deferred)* ·
  "Shared collection alerts / Tell me when someone adds or completes an item." switch *(deferred)*
- **GENERAL** — "Recycle bin / 3 items"

Add a theme row (System / Light / Dark) here; the design assumes system.

---

## Core flows (01–10)

These predate the capture-bar decision. Screens 01, 02, 05 and 07 show the older FAB-menu pattern —
build 11–30 instead. The rest are still current and are the source of truth for their screens.

### 01 · Home — everything together *(superseded by 11)*

Docked search bar with a leading `menu` and a trailing avatar "A". Filter chips: "All" (selected,
with `check`), "Notes", "Tasks", "Labels" + `expand_more`. Sections "PINNED" (with a `push_pin`
glyph) and "TODAY". Cards mix notes and tasks: "Kitchen renovation" with a two-line snippet, the
collection "Home" and "Edited 2h ago"; "Send the lease addendum" with an `event_busy` "Overdue · Mon"
chip in error and "Work"; "Book the dentist" with a `notifications` "5:00 PM" chip and "Personal";
"Standup notes — 14 Aug" with a snippet, "Work", "9:12 AM". Medium FAB `add` bottom right.

### 02 · FAB menu expanded *(superseded by the capture bar)*

Scrim at 32%. Three labelled actions stagger out above the FAB on spatial fast, 20 ms apart:
`notifications` Reminder, `checklist` Task list, `edit_note` Note. The FAB icon morphs add → close.

### 03 · Tasks — grouped by when

Large top app bar, title "Tasks", actions `swap_vert` and `more_vert`, leading `menu`. Day headers
are Title Medium emphasized with a count: "Overdue 1", "Today Thu 14 Aug · 3", "Tomorrow Fri 15 Aug ·
2". Rows are 56 dp with a checkbox and a trailing `drag_indicator` for manual reorder:
"Send the lease addendum / Was due Monday · Work", "Book the dentist / 5:00 PM · Personal",
"Review the export bug / 2 subtasks · Work", "Water the balcony plants / Repeats daily · Home",
"Design review with Priya / 10:30 AM · Work". A collapsed footer row `done_all` "Completed · 12"
with `expand_more`. FAB `add_task`.

### 04 · Note editor

Top app bar: `arrow_back`, spacer, `push_pin`, `notifications_none`, `more_vert`. Title field
Headline Medium emphasized: "Standup notes — 14 Aug". A chip row: `label` "Work" and an outlined
`add` "Label". Body in Body Large, three paragraphs, with "Friday 10:30" highlighted inline in
`secondaryContainer`. Below the body, a muted row "Add a checklist item". Status line "Saved just
now" — autosave, no Save button. Floating toolbar above the keyboard, elevation 3:
`format_bold`, `format_italic`, `checklist`, `image`, `mic`, `palette`.

Entry is a container transform from the tapped card.

### 05 · New task — bottom sheet *(superseded by 17–23)*

Modal sheet, corner 28, titled "New task" with a "Task" chip. Field "Review the export bug".
**WHEN** section with three assist chips: `calendar_today` Today, `schedule` 2:00 PM, `repeat` Never.
**PRIORITY** button group: None, Low, `flag` Medium (selected), High — the pressed member widens and
its neighbours give way. Row `label` "Work" with `chevron_right`. Row `notifications_active`
"Remind me" with a switch. Footer: text button "Cancel", split button "Add task" + `expand_less`.

### 06 · Search — full screen view

Search view: `arrow_back`, the query "lease", `close`, `mic`. Scope chips: "Everything" (selected),
"Notes", "Tasks", "Archive". Header "3 RESULTS". Results carry a leading type glyph and highlight
the matched term inline in `secondaryContainer`: `task_alt` "Send the **lease** addendum / Overdue ·
Work"; `sticky_note_2` "Flat move — admin / …countersigned **lease** goes to the agent, deposit is
protected within 30 days… / Note · Home · 3 Aug"; `archive` "Old **lease** — 2023 / Archived · 12
Jan". Below, "RECENT" with two `history` rows, each removable with `close`: "standup", "tile samples".

### 07 · Collections — organizing *(list layout still current)*

Large top app bar "Collections", leading `menu`, action `search`. Two-up grid of tonal collection
cards: `work` "Work / 14 items · 3 due", `person` "Personal / 21 items · 1 due", `home` "Home /
9 items", `flight` "Travel / 5 items". Outlined button `add` "New collection". Then a plain list of
system views: `notifications` Reminders 4, `done_all` Completed 12, `archive` Archive 38,
`delete` Trash 2.

### 08 · Completed items

Top app bar `arrow_back` / "Completed" / text button "Clear all". Headers "TODAY" and "EARLIER THIS
WEEK". Rows: filled `check_circle` in primary, title struck through at 38% onSurface, "Done 8:04 AM ·
Home" supporting text, trailing `undo` icon button. Items: "Pay the electricity bill / Done 8:04 AM ·
Home", "Reply to the landlord / Done 9:41 AM · Work", "Order the tile samples / Done Tue · Home",
"Confirm the courier pickup / Done Mon · Work". Snackbar: "Task moved back to Today" with "Undo".

### 09 · Empty state

The pattern for every empty list. Centred: an expressive shape motif, headline "Nothing here yet"
(Headline Medium emphasized), supporting text "Notes, checklists and reminders you create will live
on this screen. Start with a thought and sort it out later.", and one tonal button `edit_note`
"Write a note". Nothing else.

### 10 · Dark scheme + sync

Dark roles throughout. Selection mode top bar: `close`, "2 selected", actions `push_pin`, `label`,
`archive`, `more_vert`. The expressive loading indicator (a 40 dp morphing shape) sits centred under
the bar during pull-to-refresh. Selected cards use `primaryContainer` with a 2 dp primary border and
a filled `check_circle`; unselected cards use `surfaceContainer`. Rows: "Kitchen renovation",
"Standup notes — 14 Aug" (both selected), "Book the dentist / 5:00 PM · Personal", "Water the
balcony plants / Repeats daily · Home", "Packing list / Charger, EU adapter · Running shoes · 1 of 3
done".
