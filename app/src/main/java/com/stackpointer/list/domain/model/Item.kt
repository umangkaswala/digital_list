package com.stackpointer.list.domain.model

import java.time.Instant
import java.util.UUID

/**
 * One item type covers notes, tasks and checklists. What a user calls the item is derived,
 * not stored as a separate table: a body makes it a note, sub-items make it a checklist, a
 * trigger makes it a reminder. See DATA_MODEL.md.
 */
data class Item(
    val id: String,
    val title: String,
    val body: String?,
    val isCompleted: Boolean,
    val completedAt: Instant?,
    val isStarred: Boolean,
    val isPinned: Boolean,
    val isArchived: Boolean,
    val deletedAt: Instant?,
    val triggerType: TriggerType,
    val dueAt: Instant?,
    val isAllDay: Boolean,
    val earlyAlertMinutes: Int?,
    val alertType: AlertType,
    val recurrence: Recurrence?,
    val placeId: String?,
    val placeTrigger: PlaceTrigger?,
    val placeWindow: PlaceWindow?,
    val subItems: List<SubItem>,
    val collections: List<Collection>,
    val sortOrder: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
    /** A normal, swipe-dismissible status notification — distinct from [isPinned], which is
     * the in-app pin-to-top-of-list. See CLAUDE.md's notification-bar & pin feature. */
    val isShownInNotificationBar: Boolean = false,
    /** An ongoing, non-dismissible status notification. Implies [isShownInNotificationBar]
     * for rendering purposes (render once, ongoing) even though the two flags are independent
     * in storage. */
    val isPinnedToNotification: Boolean = false,
) {
    /** Presence of a body makes this read as a note. */
    val isNote: Boolean get() = !body.isNullOrEmpty()

    /** Presence of sub-items makes this read as a checklist. */
    val isChecklist: Boolean get() = subItems.isNotEmpty()

    val completedSubItemCount: Int get() = subItems.count { it.isCompleted }

    companion object {
        /** A blank draft for the capture sheet — not yet persisted. */
        fun draft(): Item = Item(
            id = UUID.randomUUID().toString(),
            title = "",
            body = null,
            isCompleted = false,
            completedAt = null,
            isStarred = false,
            isPinned = false,
            isArchived = false,
            deletedAt = null,
            triggerType = TriggerType.NONE,
            dueAt = null,
            isAllDay = false,
            earlyAlertMinutes = null,
            alertType = AlertType.MEDIUM,
            recurrence = null,
            placeId = null,
            placeTrigger = null,
            placeWindow = null,
            subItems = emptyList(),
            collections = emptyList(),
            sortOrder = 0,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )
    }
}

enum class TriggerType { NONE, TIME, PLACE }

enum class AlertType { SOFT, MEDIUM, INSISTENT }
