package com.stackpointer.list.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.stackpointer.list.domain.model.Item
import com.stackpointer.list.domain.model.TriggerType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AlarmScheduler"

// DATA_MODEL.md's default all-day alert time — the Settings row that overrides this doesn't
// exist until M8's DataStore-backed settings screen.
private val DEFAULT_ALL_DAY_ALERT_TIME: LocalTime = LocalTime.of(9, 0)

/**
 * Schedules and cancels the `AlarmManager` alarms behind reminder notifications. Deliberately
 * has no dependency on [com.stackpointer.list.domain.repository.ItemRepository] — taking plain
 * [Item] lists/values as parameters instead — because [com.stackpointer.list.data.repository.ItemRepositoryImpl]
 * calls into this on every save, and depending on the repository interface back would make
 * Hilt's dependency graph circular.
 */
@Singleton
class AlarmScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val alarmManager: AlarmManager
        get() = context.getSystemService(AlarmManager::class.java)

    /** Cancels any existing alarms for [item] and reschedules from its current state — the one
     * entry point [ItemRepositoryImpl] needs, since "does this still need an alarm" and "what
     * time" both potentially changed. */
    fun reschedule(item: Item) {
        cancel(item.id)
        if (!isEligible(item)) return

        val zone = ZoneId.systemDefault()
        val dueAt = item.dueAt ?: return
        val mainTriggerAt = if (item.isAllDay) {
            dueAt.atZone(zone).toLocalDate().atTime(DEFAULT_ALL_DAY_ALERT_TIME).atZone(zone).toInstant()
        } else {
            dueAt
        }

        scheduleAt(mainTriggerAt.toEpochMilli(), NotificationIds.mainAlarm(item.id), item.id, isEarly = false)

        item.earlyAlertMinutes?.let { minutes ->
            val earlyTriggerAt = mainTriggerAt.minusSeconds(minutes * 60L)
            scheduleAt(earlyTriggerAt.toEpochMilli(), NotificationIds.earlyAlert(item.id), item.id, isEarly = true)
        }
    }

    /** Re-fires the same alarm [minutes] from now, independent of the item's actual `dueAt` —
     * snoozing is a "remind me again shortly" nudge, not a real due-date change. */
    fun snooze(itemId: String, isEarly: Boolean, minutes: Int = 10) {
        val requestCode = if (isEarly) NotificationIds.earlyAlert(itemId) else NotificationIds.mainAlarm(itemId)
        val triggerAtMillis = System.currentTimeMillis() + minutes * 60_000L
        scheduleAt(triggerAtMillis, requestCode, itemId, isEarly)
    }

    fun cancel(itemId: String) {
        alarmManager.cancel(pendingIntentFor(NotificationIds.mainAlarm(itemId), itemId, isEarly = false))
        alarmManager.cancel(pendingIntentFor(NotificationIds.earlyAlert(itemId), itemId, isEarly = true))
    }

    /** Called from [com.stackpointer.list.notification.BootReceiver] — exact alarms don't
     * survive a reboot (or a timezone change invalidates their trigger time), so every
     * still-relevant item needs a fresh alarm. */
    fun rescheduleAll(items: List<Item>) {
        items.filter(::isEligible).forEach(::reschedule)
    }

    private fun isEligible(item: Item): Boolean =
        item.triggerType == TriggerType.TIME && item.dueAt != null && !item.isCompleted && item.deletedAt == null

    private fun scheduleAt(triggerAtMillis: Long, requestCode: Int, itemId: String, isEarly: Boolean) {
        val pendingIntent = pendingIntentFor(requestCode, itemId, isEarly)
        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } catch (e: SecurityException) {
            // SCHEDULE_EXACT_ALARM not granted (API 33+, user hasn't allowed it in Settings —
            // there's no Settings row to request it from until M8). Inexact still fires,
            // just not necessarily at the precise minute.
            Log.w(TAG, "Exact alarm not permitted, falling back to inexact for item $itemId", e)
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    private fun pendingIntentFor(requestCode: Int, itemId: String, isEarly: Boolean): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_ALARM_FIRED
            putExtra(AlarmReceiver.EXTRA_ITEM_ID, itemId)
            putExtra(AlarmReceiver.EXTRA_IS_EARLY, isEarly)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
