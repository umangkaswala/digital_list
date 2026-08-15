package com.stackpointer.list.domain.model

import java.time.DayOfWeek
import java.time.Instant

/**
 * The rule, not the expansion — the next occurrence is derived on read (see
 * [com.stackpointer.list.domain.usecase.RecurrenceNextOccurrence]). Completing a recurring
 * item advances the item's `dueAt` to the next occurrence rather than marking it done for good.
 */
data class Recurrence(
    val id: String,
    val freq: RecurrenceFreq,
    val interval: Int = 1,
    /** Only meaningful for [RecurrenceFreq.WEEKLY]. */
    val weekdays: Set<DayOfWeek> = emptySet(),
    /** Only meaningful for [RecurrenceFreq.MONTHLY] — "on the 1st". */
    val monthDay: Int? = null,
    val endType: RecurrenceEndType = RecurrenceEndType.NEVER,
    val endDate: Instant? = null,
    val endCount: Int? = null,
)

enum class RecurrenceFreq { DAILY, WEEKLY, MONTHLY, YEARLY, CUSTOM }

enum class RecurrenceEndType { NEVER, ON_DATE, AFTER_COUNT }
