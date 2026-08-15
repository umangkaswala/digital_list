package com.stackpointer.list.domain

import com.stackpointer.list.domain.model.AlertType
import com.stackpointer.list.domain.model.Collection
import com.stackpointer.list.domain.model.Item
import com.stackpointer.list.domain.model.Recurrence
import com.stackpointer.list.domain.model.RecurrenceFreq
import com.stackpointer.list.domain.model.SubItem
import com.stackpointer.list.domain.model.TriggerType
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

/**
 * Sample content mirroring SCREENS.md's illustrative copy (reference date Thursday 14 Aug
 * 2026), for `@Preview`s and tests — not seeded into the shipped database (see SCREENS.md's
 * own framing: sample names/dates/counts are illustrative, reproduced in previews only).
 */
object SeedData {

    private fun at(iso: String): Instant = ZonedDateTime.parse(iso).toInstant()

    val work = Collection(id = "col-work", name = "Work", iconKey = "work", colorKey = null, isShared = false, sortOrder = 0)
    val personal = Collection(id = "col-personal", name = "Personal", iconKey = "person", colorKey = null, isShared = false, sortOrder = 1)
    val home = Collection(id = "col-home", name = "Home", iconKey = "home", colorKey = null, isShared = false, sortOrder = 2)

    val collections = listOf(work, personal, home)

    private val everyDay = Recurrence(id = "rec-daily", freq = RecurrenceFreq.DAILY)

    val overdueTask = Item.draft().copy(
        id = "item-lease",
        title = "Send the lease addendum",
        triggerType = TriggerType.TIME,
        dueAt = at("2026-08-10T09:00:00Z"),
        alertType = AlertType.MEDIUM,
        collections = listOf(work),
        createdAt = at("2026-08-01T09:00:00Z"),
        updatedAt = at("2026-08-01T09:00:00Z"),
    )

    val callMum = Item.draft().copy(
        id = "item-call-mum",
        title = "Call mum",
        triggerType = TriggerType.TIME,
        dueAt = at("2026-08-14T13:30:00Z"),
        isStarred = true,
        alertType = AlertType.MEDIUM,
        recurrence = everyDay,
        collections = listOf(personal),
        createdAt = at("2026-08-01T09:00:00Z"),
        updatedAt = at("2026-08-01T09:00:00Z"),
    )

    val cookDinner = Item.draft().copy(
        id = "item-cook-dinner",
        title = "Cook dinner",
        triggerType = TriggerType.TIME,
        dueAt = at("2026-08-14T20:00:00Z"),
        alertType = AlertType.SOFT,
        recurrence = everyDay,
        collections = listOf(home),
        createdAt = at("2026-08-01T09:00:00Z"),
        updatedAt = at("2026-08-01T09:00:00Z"),
    )

    val payElectricityBill = Item.draft().copy(
        id = "item-electricity",
        title = "Pay the electricity bill",
        triggerType = TriggerType.TIME,
        dueAt = at("2026-08-14T08:00:00Z"),
        isCompleted = true,
        completedAt = at("2026-08-14T08:04:00Z"),
        alertType = AlertType.MEDIUM,
        collections = listOf(home),
        createdAt = at("2026-08-01T09:00:00Z"),
        updatedAt = at("2026-08-14T08:04:00Z"),
    )

    val kitchenRenovationNote = Item.draft().copy(
        id = "item-kitchen-note",
        title = "Kitchen renovation",
        body = "Tile samples arrive Thursday. Ask about the matte finish.",
        collections = listOf(home),
        createdAt = at("2026-08-03T09:12:00Z"),
        updatedAt = at("2026-08-12T14:00:00Z"),
    )

    val packingListChecklist = Item.draft().copy(
        id = "item-packing-list",
        title = "Packing list",
        subItems = listOf(
            SubItem(id = "sub-1", itemId = "item-packing-list", text = "Passport", isCompleted = true, sortOrder = 0),
            SubItem(id = "sub-2", itemId = "item-packing-list", text = "Charger, EU adapter", isCompleted = false, sortOrder = 1),
            SubItem(id = "sub-3", itemId = "item-packing-list", text = "Running shoes", isCompleted = false, sortOrder = 2),
        ),
        collections = listOf(home),
        createdAt = at("2026-08-05T09:00:00Z"),
        updatedAt = at("2026-08-05T09:00:00Z"),
    )

    val items = listOf(
        overdueTask,
        callMum,
        cookDinner,
        payElectricityBill,
        kitchenRenovationNote,
        packingListChecklist,
    )
}
