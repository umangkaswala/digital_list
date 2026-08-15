package com.stackpointer.list.notification

/**
 * Notification/request-code ID partitioning, shared by the alarm-triggered notifications here
 * and (once M7b lands) the pinned/shown-in-bar notifications — both can be visible for the
 * same item at once, so they must never collide. Two bits are reserved off the top of each
 * item id's hash to keep the spaces disjoint; the remaining bits carry the per-item identity.
 */
object NotificationIds {
    private const val ITEM_ID_MASK = 0x1FFFFFFF // low 29 bits
    private const val EARLY_ALERT_BIT = 1 shl 29
    // Bit 30 is reserved for M7b's pinned/shown-in-notification-bar feature — do not reuse here.

    private fun baseId(itemId: String): Int = itemId.hashCode() and ITEM_ID_MASK

    fun mainAlarm(itemId: String): Int = baseId(itemId)

    fun earlyAlert(itemId: String): Int = baseId(itemId) or EARLY_ALERT_BIT
}
