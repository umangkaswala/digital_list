package com.stackpointer.list.ui.screens.common

import com.stackpointer.list.domain.model.Item
import com.stackpointer.list.domain.model.Recurrence
import com.stackpointer.list.domain.model.RecurrenceFreq
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/** Turns raw [Item] fields into the copy patterns SCREENS.md shows — "1:30 PM",
 * "Was due Monday", "Every week on Mon, Tue, Wed, Thu, Fri". Kept out of ItemRow itself so
 * that component stays a plain data-in renderer, per CLAUDE.md's "composables take data and
 * lambdas" rule. */
object ItemFormatting {

    private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
    private val monthDayFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.US)
    private val weekdayMonthDayFormatter = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.US)

    fun timeOnly(instant: Instant, zone: ZoneId = ZoneId.systemDefault()): String =
        instant.atZone(zone).format(timeFormatter)

    fun dueText(dueAt: Instant, now: Instant, zone: ZoneId = ZoneId.systemDefault()): String {
        val due = dueAt.atZone(zone)
        val today = now.atZone(zone).toLocalDate()
        val time = due.format(timeFormatter)
        return when (due.toLocalDate()) {
            today -> time
            today.minusDays(1) -> "Yesterday, $time"
            today.plusDays(1) -> "Tomorrow, $time"
            else -> "${due.format(weekdayMonthDayFormatter)}, $time"
        }
    }

    fun overdueText(dueAt: Instant, now: Instant, zone: ZoneId = ZoneId.systemDefault()): String {
        val due = dueAt.atZone(zone)
        val daysAgo = java.time.Duration.between(due.toLocalDate().atStartOfDay(zone), now.atZone(zone).toLocalDate().atStartOfDay(zone)).toDays()
        return if (daysAgo in 1..6) {
            "Was due ${due.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.US)}"
        } else {
            "Was due ${due.format(monthDayFormatter)}"
        }
    }

    fun completedText(completedAt: Instant, zone: ZoneId = ZoneId.systemDefault()): String =
        "Completed ${completedAt.atZone(zone).format(timeFormatter)}"

    fun checklistProgress(item: Item): String? {
        if (item.subItems.isEmpty()) return null
        return "${item.completedSubItemCount} of ${item.subItems.size} done"
    }

    fun collectionNames(item: Item): String? =
        item.collections.takeIf { it.isNotEmpty() }?.joinToString(", ") { it.name }

    /** Combines the due-time text with the item's collection(s) — "1:30 PM · Personal". */
    fun metadata(item: Item, now: Instant, zone: ZoneId = ZoneId.systemDefault()): String? {
        val parts = buildList {
            item.dueAt?.let { add(dueText(it, now, zone)) }
            collectionNames(item)?.let { add(it) }
        }
        return parts.takeIf { it.isNotEmpty() }?.joinToString(" · ")
    }

    fun recurrenceText(recurrence: Recurrence): String = when (recurrence.freq) {
        RecurrenceFreq.DAILY -> if (recurrence.interval <= 1) "Every day" else "Every ${recurrence.interval} days"
        RecurrenceFreq.WEEKLY -> {
            val days = recurrence.weekdays.sortedBy { it.value }
                .joinToString(", ") { it.getDisplayName(TextStyle.SHORT, Locale.US) }
            if (recurrence.interval <= 1) "Every week on $days" else "Every ${recurrence.interval} weeks on $days"
        }
        RecurrenceFreq.MONTHLY -> "Every month on the ${ordinal(recurrence.monthDay ?: 1)}"
        RecurrenceFreq.YEARLY -> "Every year"
        RecurrenceFreq.CUSTOM -> "Every ${recurrence.interval} days"
    }

    fun ordinal(day: Int): String {
        if (day in 11..13) return "${day}th"
        return when (day % 10) {
            1 -> "${day}st"
            2 -> "${day}nd"
            3 -> "${day}rd"
            else -> "${day}th"
        }
    }
}
