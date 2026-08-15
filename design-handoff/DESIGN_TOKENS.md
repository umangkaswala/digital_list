# Design tokens

Seed colour **#29A87C**. The light scheme below is the fallback brand theme. When dynamic colour is
available (all supported devices, minSdk 31) every role comes from the system palette and nothing
else in the app changes.

Regenerate the full role set from the seed with the Material Theme Builder if a role is missing
here; the values below are the ones the design actually uses and must match.

## Colour — light

| Role | Hex | Where |
| --- | --- | --- |
| primary | `#006C4C` | text buttons, section labels, active switch track |
| onPrimary | `#FFFFFF` | |
| primaryContainer | `#89F8C7` | tonal buttons, confirm FAB, mic button, Today tile |
| onPrimaryContainer | `#002114` | |
| secondary | `#4C6358` | |
| secondaryContainer | `#CEE9D9` | nav bar active indicator, selected chips, Starred tile |
| onSecondaryContainer | `#092017` | |
| tertiary | `#3D6373` | |
| tertiaryContainer | `#C1E8FB` | due-date chips, Scheduled tile |
| onTertiaryContainer | `#001F29` | |
| error | `#BA1A1A` | overdue text and rules, destructive actions |
| errorContainer | `#FFDAD6` | |
| onErrorContainer | `#410002` | |
| surface | `#F5FBF6` | every screen background |
| surfaceContainerLowest | `#FFFFFF` | capture bar, sheets, menus |
| surfaceContainerLow | `#EFF5F0` | item cards, settings groups |
| surfaceContainer | `#E9EFEA` | |
| surfaceContainerHigh | `#E4EAE5` | docked search bar, banner cards, inactive view tiles |
| surfaceContainerHighest | `#DEE4DF` | |
| onSurface | `#171D19` | titles, primary text (13.9:1 on surface) |
| onSurfaceVariant | `#3F4942` | supporting text, inactive icons (8.3:1) |
| outline | `#6F7A73` | outlined controls, tertiary text |
| outlineVariant | `#BFC9C1` | dividers only |

## Colour — dark

Observed in screen 10; derive the remaining roles from the same seed.

| Role | Hex |
| --- | --- |
| primary | `#6CDBAC` |
| onPrimary | `#003824` |
| primaryContainer | `#005236` |
| onPrimaryContainer | `#89F8C7` |
| surfaceContainer | `#1B211D` |
| onSurface | `#DDE5DE` |
| onSurfaceVariant | `#BFC9C1` |
| outline | `#89938B` |

## Type — Roboto Flex

Five roles × three sizes, each with an emphasized twin at a heavier weight and identical metrics.
Only the styles the app uses are listed.

| Style | Size / line | Weight | Tracking | Use |
| --- | --- | --- | --- | --- |
| Display Small | 36 / 44 | 400 | 0 | large empty-state numerals |
| Headline Large | 32 / 40 | 400 | 0 | large top app bar, expanded |
| Headline Medium *emphasized* | 28 / 36 | 500 | 0 | large top app bar titles, empty-state headline |
| Title Large | 22 / 28 | 400 | 0 | small top app bar titles, sheet titles |
| Title Medium | 16 / 24 | 500 | +0.15 | item titles, section headers |
| Title Medium *emphasized* | 16 / 24 | 700 | +0.15 | active day header, bucket headers |
| Body Large | 16 / 24 | 400 | +0.5 | note body, settings rows, search field |
| Body Medium | 14 / 20 | 400 | +0.25 | snippets, supporting text |
| Label Large | 14 / 20 | 500 | +0.1 | buttons, chips |
| Label Large *emphasized* | 14 / 20 | 700 | +0.1 | primary action in a dialog |
| Label Medium | 12 / 16 | 500 | +0.5 | nav bar labels, metadata, counts |
| Label Small | 12 / 16 | 400 | +0.4 | timestamps, tile captions |

Rule from the design: never two emphasized styles in one view.

Section labels in Settings and list headings ("PRESETS", "3 RESULTS", "28 DAYS LEFT") are Label
Medium, uppercase, in primary, with 12 dp top padding.

## Shape

| Token | Radius | Use |
| --- | --- | --- |
| extraSmall | 4 dp | checkbox, caret affordances |
| small | 8 dp | assist and filter chips |
| medium | 12 dp | menus, snackbar |
| large | 16 dp | item cards, view tiles, FAB (medium) |
| extraLarge | 28 dp | search bar, bottom sheets, dialogs, editor canvas, capture bar, nav pill (32) |
| full | 999 dp | buttons, nav indicator, avatars, toggle icon buttons |

Fully rounded corners use the `full` token, not 50% of the component, so shape stays stable as a
component resizes.

**Shape morphing.** Checking a task morphs the check target circle → squircle. Pressing a button
squares its corners one step and springs back on release. Use the M3 shape-morph APIs where present.

## Elevation

Elevation is tonal first. Cards sit on `surfaceContainerLow` with **no shadow**.

| Level | Shadow | Used by |
| --- | --- | --- |
| 0 | none | screen background, cards |
| 1 | `0 1px 3px 1px rgba(0,0,0,.10), 0 1px 2px rgba(0,0,0,.20)` | bottom sheets |
| 2 | `0 2px 6px 2px rgba(0,0,0,.10), 0 1px 2px rgba(0,0,0,.20)` | menus, capture bar |
| 3 | `0 4px 8px 3px rgba(0,0,0,.10), 0 1px 3px rgba(0,0,0,.20)` | FAB, floating toolbar, nav bar |

## Motion

Theme `MotionScheme.expressive()`. Springs replace duration + easing. Spatial springs may overshoot;
effects springs never do.

| Spring | Stiffness | Damping | Applies to |
| --- | --- | --- | --- |
| spatial fast | 800 | 0.6 | FAB menu stagger, check morph |
| spatial default | 380 | 0.8 | sheet entry, container transform, nav indicator, list reorder |
| spatial slow | 200 | 0.8 | large layout changes |
| effects fast | 3800 | 1.0 | ripple |
| effects default | 1600 | 1.0 | chip selection, checkbox fill, strike-through |
| effects slow | 800 | 1.0 | scrim fade |

Named transitions:

- **Opening an item** — container transform from the tapped card, spatial default; shadow and radius interpolate.
- **Capture sheet / FAB menu** — items stagger 20 ms apart on spatial fast; the FAB icon morphs add → close.
- **Completing a task** — check morph spatial fast, strike-through effects default, neighbours spring closed.
- **Tab switch** — shared x-axis between the three nav destinations; the indicator leads the content.

## Layout

Compact window class, 412 × 892 dp reference. 4 dp grid.

| Value | dp |
| --- | --- |
| Body margin | 16 |
| Gutter between cards | 8 |
| Item card padding | 14 vertical, 16 horizontal |
| Item card internal gap | 12 (leading control to text), 3 (title to metadata) |
| Section header top padding | 8, with 4 below |
| Top app bar (small) | 56 |
| Large top app bar collapsed / expanded | 56 / 152 |
| Docked search bar | 56, corner full/28 |
| Capture bar | 56, corner 28, inset 16, sits 88 from the bottom when a nav bar is present, 16 when not |
| Floating navigation bar | 64 tall, corner 32, inset 16, elevation 3 |
| FAB (medium) | 56, corner 16, margin 16 |
| Icon button target | 48 × 48, glyph 24 |
| Checkbox | 24 with a 48 dp target, 2 dp stroke, corner 4 |
| Switch | 52 × 32, thumb 24 |
| View tile | 76 tall, 3-up grid, gap 8, corner 16, padding 12 |
| Sub-item / settings row | 48 minimum |
| Dividers | 1 dp `outlineVariant`, inset to the text column |

## Contrast rules

Body text 8.3:1 or better. Labels on containers 7:1 or better. Disabled states use 38% onSurface.
Status is never carried by colour alone.
