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
    private const val PINNED_BIT = 1 shl 30

    /** Fixed id for the pinned/shown group-summary notification — outside both the
     * `[0, 0x3FFFFFFF]` main+early range and the `[0x40000000, 0x5FFFFFFF]` pinned range above,
     * so it can never collide with a real item's id. */
    const val PINNED_SUMMARY_ID = 0x60000000

    private fun baseId(itemId: String): Int = itemId.hashCode() and ITEM_ID_MASK

    fun mainAlarm(itemId: String): Int = baseId(itemId)

    fun earlyAlert(itemId: String): Int = baseId(itemId) or EARLY_ALERT_BIT

    fun pinned(itemId: String): Int = baseId(itemId) or PINNED_BIT
}
