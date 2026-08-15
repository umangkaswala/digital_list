package com.stackpointer.list.domain.usecase

import com.stackpointer.list.domain.model.Recurrence
import com.stackpointer.list.domain.model.RecurrenceEndType
import com.stackpointer.list.domain.model.RecurrenceFreq
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Computes the next occurrence of a [Recurrence] after a given instant.
 *
 * Assumes [after] is itself a valid prior occurrence — that's how the app actually calls this:
 * `dueAt` always holds the current occurrence (per DATA_MODEL.md, completing a recurring item
 * advances `dueAt` to the next one rather than marking it done for good), so "next" only ever
 * needs to step forward from a date that's already on-pattern. This keeps weekly/monthly/yearly
 * math simple — no need to search for the *first* occurrence of a pattern, only the *next* one.
 *
 * Returns null once the recurrence's end condition ([RecurrenceEndType.ON_DATE]) is reached.
 * [RecurrenceEndType.AFTER_COUNT] isn't checked here — nothing in the data model tracks how
 * many occurrences have already happened, so that has to be enforced by the caller.
 */
object RecurrenceNextOccurrence {

    fun next(recurrence: Recurrence, after: Instant, zone: ZoneId = ZoneId.systemDefault()): Instant? {
        val afterZoned = after.atZone(zone)
        val step = recurrence.interval.coerceAtLeast(1)

        val next = when (recurrence.freq) {
            RecurrenceFreq.DAILY -> afterZoned.plusDays(step.toLong())
            RecurrenceFreq.WEEKLY -> nextWeekly(afterZoned, recurrence.weekdays, step)
            RecurrenceFreq.MONTHLY -> nextMonthly(afterZoned, recurrence.monthDay ?: afterZoned.dayOfMonth, step)
            RecurrenceFreq.YEARLY -> afterZoned.plusYears(step.toLong())
            // No unit is specified for CUSTOM beyond "every N" in the design — treated as days
            // until the repeat-picker UI (M5) pins down what it actually offers.
            RecurrenceFreq.CUSTOM -> afterZoned.plusDays(step.toLong())
        }

        val nextInstant = next.toInstant()
        if (recurrence.endType == RecurrenceEndType.ON_DATE &&
            recurrence.endDate != null &&
            nextInstant.isAfter(recurrence.endDate)
        ) {
            return null
        }
        return nextInstant
    }

    private fun nextWeekly(after: ZonedDateTime, weekdays: Set<DayOfWeek>, interval: Int): ZonedDateTime {
        require(weekdays.isNotEmpty()) { "WEEKLY recurrence needs at least one weekday" }
        val sorted = weekdays.sortedBy { it.value }
        val afterDow = after.dayOfWeek.value

        // A later weekday from the set in the same (already-qualifying) week comes next,
        // regardless of interval — interval only governs jumps between weeks, not the
        // selection within one.
        sorted.firstOrNull { it.value > afterDow }?.let { sameWeekMatch ->
            return after.plusDays((sameWeekMatch.value - afterDow).toLong())
        }

        // Otherwise jump to the next qualifying week and take its earliest selected weekday.
        val nextMonday = after.plusDays((8 - afterDow).toLong())
        val targetWeekStart = nextMonday.plusWeeks((interval - 1).toLong())
        return targetWeekStart.plusDays((sorted.first().value - 1).toLong())
    }

    private fun nextMonthly(after: ZonedDateTime, monthDay: Int, interval: Int): ZonedDateTime {
        val target = after.plusMonths(interval.toLong())
        val lastDayOfTargetMonth = target.toLocalDate().lengthOfMonth()
        return target.withDayOfMonth(monthDay.coerceAtMost(lastDayOfTargetMonth))
    }
}
