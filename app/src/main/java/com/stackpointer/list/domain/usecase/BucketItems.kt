package com.stackpointer.list.domain.usecase

import com.stackpointer.list.domain.model.Bucket
import com.stackpointer.list.domain.model.BucketLabel
import com.stackpointer.list.domain.model.Item
import com.stackpointer.list.domain.model.SavedView
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

/**
 * Splits an already view-filtered item list into the labelled buckets DATA_MODEL.md specifies
 * per saved view. View membership (which items belong to "Scheduled" vs "Starred" etc.) is a
 * repository-level query concern, not this use case's — [items] is assumed to already be the
 * right set for [view]; this only decides which bucket each one falls into.
 *
 * Recycle bin is deliberately not handled here: its grouping is by an exact "N days left"
 * count, not a small set of fixed labels, so it doesn't fit the [Bucket] shape — see
 * [recycleBinDaysRemaining] instead.
 */
object BucketItems {

    fun bucket(view: SavedView, items: List<Item>, now: Instant, zone: ZoneId = ZoneId.systemDefault()): List<Bucket> =
        when (view) {
            SavedView.TODAY -> bucketToday(items, now, zone)
            SavedView.SCHEDULED -> bucketScheduled(items, now, zone)
            SavedView.COMPLETED -> bucketCompleted(items, now, zone)
            SavedView.STARRED, SavedView.NO_ALERT, SavedView.PLACE, SavedView.RECYCLE_BIN ->
                listOfNotNull(bucketOrNull(BucketLabel.UNBUCKETED, items))
        }

    /** 30 minus the item's age since deletion, per DATA_MODEL.md's recycle-bin retention. */
    fun recycleBinDaysRemaining(item: Item, now: Instant, retentionDays: Int = 30): Int {
        val deletedAt = item.deletedAt ?: return retentionDays
        val ageDays = Duration.between(deletedAt, now).toDays().toInt()
        return (retentionDays - ageDays).coerceAtLeast(0)
    }

    private fun bucketToday(items: List<Item>, now: Instant, zone: ZoneId): List<Bucket> {
        val todayStart = startOfDay(now, zone)
        val (completed, active) = items.partition { it.isCompleted }
        val (past, soon) = active.partition { (it.dueAt ?: now).isBefore(todayStart) }
        return listOfNotNull(
            bucketOrNull(BucketLabel.PAST, past),
            bucketOrNull(BucketLabel.SOON, soon),
            bucketOrNull(BucketLabel.COMPLETED, completed),
        )
    }

    private fun bucketScheduled(items: List<Item>, now: Instant, zone: ZoneId): List<Bucket> {
        val todayStart = startOfDay(now, zone)
        val todayEnd = todayStart.plus(Duration.ofDays(1))
        val next7End = todayStart.plus(Duration.ofDays(8))

        val (past, afterPast) = items.partition { (it.dueAt ?: now).isBefore(todayStart) }
        val (today, afterToday) = afterPast.partition { (it.dueAt ?: now).isBefore(todayEnd) }
        val (next7, later) = afterToday.partition { (it.dueAt ?: now).isBefore(next7End) }

        return listOfNotNull(
            bucketOrNull(BucketLabel.PAST, past),
            bucketOrNull(BucketLabel.TODAY, today),
            bucketOrNull(BucketLabel.NEXT_7_DAYS, next7),
            bucketOrNull(BucketLabel.LATER, later),
        )
    }

    private fun bucketCompleted(items: List<Item>, now: Instant, zone: ZoneId): List<Bucket> {
        val todayStart = startOfDay(now, zone)
        val mondayOfThisWeek = todayStart.atZone(zone).let { it.minusDays((it.dayOfWeek.value - 1).toLong()) }.toInstant()

        val (today, afterToday) = items.partition { (it.completedAt ?: now).let { c -> !c.isBefore(todayStart) } }
        val (thisWeek, older) = afterToday.partition { (it.completedAt ?: now).let { c -> !c.isBefore(mondayOfThisWeek) } }

        return listOfNotNull(
            bucketOrNull(BucketLabel.TODAY, today),
            bucketOrNull(BucketLabel.EARLIER_THIS_WEEK, thisWeek),
            bucketOrNull(BucketLabel.OLDER, older),
        )
    }

    private fun startOfDay(instant: Instant, zone: ZoneId): Instant =
        instant.atZone(zone).toLocalDate().atStartOfDay(zone).toInstant()

    private fun bucketOrNull(label: BucketLabel, items: List<Item>): Bucket? =
        items.takeIf { it.isNotEmpty() }?.let { Bucket(label, it) }
}
