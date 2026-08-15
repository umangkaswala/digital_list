package com.stackpointer.list.domain.usecase

import com.stackpointer.list.domain.model.AlertType
import com.stackpointer.list.domain.model.BucketLabel
import com.stackpointer.list.domain.model.Item
import com.stackpointer.list.domain.model.SavedView
import com.stackpointer.list.domain.model.TriggerType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class BucketItemsTest {

    private val zone: ZoneId = ZoneId.of("UTC")
    private val now = ZonedDateTime.parse("2026-08-14T12:00:00Z").toInstant() // Friday, midday

    private fun item(
        title: String,
        dueAt: Instant? = null,
        isCompleted: Boolean = false,
        completedAt: Instant? = null,
        deletedAt: Instant? = null,
    ) = Item.draft().copy(
        title = title,
        triggerType = if (dueAt != null) TriggerType.TIME else TriggerType.NONE,
        dueAt = dueAt,
        isCompleted = isCompleted,
        completedAt = completedAt,
        deletedAt = deletedAt,
        alertType = AlertType.MEDIUM,
    )

    private fun at(iso: String) = ZonedDateTime.parse(iso).toInstant()

    @Test
    fun `today splits past, soon, and completed`() {
        val overdue = item("Overdue", dueAt = at("2026-08-13T09:00:00Z"))
        val laterToday = item("Later today", dueAt = at("2026-08-14T18:00:00Z"))
        val doneToday = item("Done today", isCompleted = true, completedAt = at("2026-08-14T08:00:00Z"))

        val buckets = BucketItems.bucket(SavedView.TODAY, listOf(overdue, laterToday, doneToday), now, zone)

        assertEquals(
            mapOf(
                BucketLabel.PAST to listOf("Overdue"),
                BucketLabel.SOON to listOf("Later today"),
                BucketLabel.COMPLETED to listOf("Done today"),
            ),
            buckets.associate { it.label to it.items.map(Item::title) },
        )
    }

    @Test
    fun `today omits empty buckets`() {
        val laterToday = item("Later today", dueAt = at("2026-08-14T18:00:00Z"))

        val buckets = BucketItems.bucket(SavedView.TODAY, listOf(laterToday), now, zone)

        assertEquals(listOf(BucketLabel.SOON), buckets.map { it.label })
    }

    @Test
    fun `scheduled splits past, today, next 7 days, and later`() {
        val past = item("Past", dueAt = at("2026-08-10T09:00:00Z"))
        val today = item("Today", dueAt = at("2026-08-14T20:00:00Z"))
        val nextWeek = item("Next week", dueAt = at("2026-08-19T09:00:00Z"))
        val later = item("Later", dueAt = at("2026-09-20T09:00:00Z"))

        val buckets = BucketItems.bucket(SavedView.SCHEDULED, listOf(past, today, nextWeek, later), now, zone)

        assertEquals(
            mapOf(
                BucketLabel.PAST to listOf("Past"),
                BucketLabel.TODAY to listOf("Today"),
                BucketLabel.NEXT_7_DAYS to listOf("Next week"),
                BucketLabel.LATER to listOf("Later"),
            ),
            buckets.associate { it.label to it.items.map(Item::title) },
        )
    }

    @Test
    fun `completed splits today, earlier this week, and older`() {
        // now is Friday 14 Aug 2026 -> Monday of this week is 10 Aug.
        val today = item("Today", isCompleted = true, completedAt = at("2026-08-14T08:00:00Z"))
        val earlierThisWeek = item("Earlier this week", isCompleted = true, completedAt = at("2026-08-11T08:00:00Z"))
        val older = item("Older", isCompleted = true, completedAt = at("2026-08-01T08:00:00Z"))

        val buckets = BucketItems.bucket(SavedView.COMPLETED, listOf(today, earlierThisWeek, older), now, zone)

        assertEquals(
            mapOf(
                BucketLabel.TODAY to listOf("Today"),
                BucketLabel.EARLIER_THIS_WEEK to listOf("Earlier this week"),
                BucketLabel.OLDER to listOf("Older"),
            ),
            buckets.associate { it.label to it.items.map(Item::title) },
        )
    }

    @Test
    fun `starred is not bucketed`() {
        val starred = item("Starred").copy(isStarred = true)

        val buckets = BucketItems.bucket(SavedView.STARRED, listOf(starred), now, zone)

        assertEquals(listOf(BucketLabel.UNBUCKETED), buckets.map { it.label })
    }

    @Test
    fun `recycle bin days remaining counts down from 30`() {
        val justDeleted = item("Just deleted", deletedAt = now)
        val deleted10DaysAgo = item("10 days ago", deletedAt = now.minusSeconds(10L * 86400))
        val deleted40DaysAgo = item("40 days ago", deletedAt = now.minusSeconds(40L * 86400))

        assertEquals(30, BucketItems.recycleBinDaysRemaining(justDeleted, now))
        assertEquals(20, BucketItems.recycleBinDaysRemaining(deleted10DaysAgo, now))
        assertEquals(0, BucketItems.recycleBinDaysRemaining(deleted40DaysAgo, now)) // clamped, not negative
    }
}
