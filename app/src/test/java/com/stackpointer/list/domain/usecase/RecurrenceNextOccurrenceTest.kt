package com.stackpointer.list.domain.usecase

import com.stackpointer.list.domain.model.Recurrence
import com.stackpointer.list.domain.model.RecurrenceEndType
import com.stackpointer.list.domain.model.RecurrenceFreq
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime

class RecurrenceNextOccurrenceTest {

    private val zone: ZoneId = ZoneId.of("UTC")

    private fun at(iso: String) = ZonedDateTime.parse(iso).toInstant()

    @Test
    fun `daily steps by one day`() {
        val recurrence = Recurrence(id = "r", freq = RecurrenceFreq.DAILY)
        val after = at("2026-08-14T09:00:00Z")

        val next = RecurrenceNextOccurrence.next(recurrence, after, zone)

        assertEquals(at("2026-08-15T09:00:00Z"), next)
    }

    @Test
    fun `daily respects a custom interval`() {
        val recurrence = Recurrence(id = "r", freq = RecurrenceFreq.DAILY, interval = 3)
        val after = at("2026-08-14T09:00:00Z")

        val next = RecurrenceNextOccurrence.next(recurrence, after, zone)

        assertEquals(at("2026-08-17T09:00:00Z"), next)
    }

    @Test
    fun `weekly picks the next selected weekday in the same week`() {
        // Friday 14 Aug 2026, weekdays Mon/Wed/Fri -> next is Fri itself already passed,
        // so next selected day is the following Monday.
        val recurrence = Recurrence(
            id = "r",
            freq = RecurrenceFreq.WEEKLY,
            weekdays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
        )
        val after = at("2026-08-14T09:00:00Z") // Friday

        val next = RecurrenceNextOccurrence.next(recurrence, after, zone)

        assertEquals(at("2026-08-17T09:00:00Z"), next) // Monday 17 Aug
    }

    @Test
    fun `weekly steps mid-week to the next matching weekday`() {
        // Monday, weekdays Mon/Wed/Fri -> next is Wednesday, same week.
        val recurrence = Recurrence(
            id = "r",
            freq = RecurrenceFreq.WEEKLY,
            weekdays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
        )
        val after = at("2026-08-17T09:00:00Z") // Monday

        val next = RecurrenceNextOccurrence.next(recurrence, after, zone)

        assertEquals(at("2026-08-19T09:00:00Z"), next) // Wednesday 19 Aug
    }

    @Test
    fun `weekly with interval 2 skips a week between cycles`() {
        // Last selected day of week 0 (Friday) -> next cycle starts 2 weeks later, on
        // the earliest selected weekday (Monday).
        val recurrence = Recurrence(
            id = "r",
            freq = RecurrenceFreq.WEEKLY,
            weekdays = setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
            interval = 2,
        )
        val after = at("2026-08-14T09:00:00Z") // Friday, last day of its week's cycle

        val next = RecurrenceNextOccurrence.next(recurrence, after, zone)

        // after's week is 10-16 Aug; interval 2 means the next qualifying week is 24-30 Aug.
        assertEquals(at("2026-08-24T09:00:00Z"), next)
    }

    @Test
    fun `monthly steps to the same day next month`() {
        val recurrence = Recurrence(id = "r", freq = RecurrenceFreq.MONTHLY, monthDay = 1)
        val after = at("2026-08-01T09:00:00Z")

        val next = RecurrenceNextOccurrence.next(recurrence, after, zone)

        assertEquals(at("2026-09-01T09:00:00Z"), next)
    }

    @Test
    fun `monthly clamps to the last day of a shorter month`() {
        // "On the 31st" from 31 Jan -> Feb 2026 (not a leap year) only has 28 days.
        val recurrence = Recurrence(id = "r", freq = RecurrenceFreq.MONTHLY, monthDay = 31)
        val after = at("2026-01-31T09:00:00Z")

        val next = RecurrenceNextOccurrence.next(recurrence, after, zone)

        assertEquals(at("2026-02-28T09:00:00Z"), next)
    }

    @Test
    fun `yearly steps to the same date next year`() {
        val recurrence = Recurrence(id = "r", freq = RecurrenceFreq.YEARLY)
        val after = at("2026-08-14T09:00:00Z")

        val next = RecurrenceNextOccurrence.next(recurrence, after, zone)

        assertEquals(at("2027-08-14T09:00:00Z"), next)
    }

    @Test
    fun `yearly clamps 29 Feb to 28 Feb in a non-leap year`() {
        val recurrence = Recurrence(id = "r", freq = RecurrenceFreq.YEARLY)
        val after = at("2024-02-29T09:00:00Z") // 2024 is a leap year

        val next = RecurrenceNextOccurrence.next(recurrence, after, zone)

        assertEquals(at("2025-02-28T09:00:00Z"), next)
    }

    @Test
    fun `returns null once the next occurrence passes the end date`() {
        val recurrence = Recurrence(
            id = "r",
            freq = RecurrenceFreq.DAILY,
            endType = RecurrenceEndType.ON_DATE,
            endDate = at("2026-08-15T09:00:00Z"),
        )
        val after = at("2026-08-15T09:00:00Z") // next occurrence would be 16 Aug, after the end date

        val next = RecurrenceNextOccurrence.next(recurrence, after, zone)

        assertNull(next)
    }

    @Test
    fun `does not stop early when the end date is still ahead`() {
        val recurrence = Recurrence(
            id = "r",
            freq = RecurrenceFreq.DAILY,
            endType = RecurrenceEndType.ON_DATE,
            endDate = at("2026-08-20T09:00:00Z"),
        )
        val after = at("2026-08-15T09:00:00Z")

        val next = RecurrenceNextOccurrence.next(recurrence, after, zone)

        assertEquals(at("2026-08-16T09:00:00Z"), next)
    }
}
