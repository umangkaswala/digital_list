package com.stackpointer.list.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.stackpointer.list.MainActivity
import com.stackpointer.list.domain.model.AlertType
import com.stackpointer.list.domain.model.Item
import com.stackpointer.list.domain.repository.ItemRepository
import com.stackpointer.list.domain.usecase.RecurrenceNextOccurrence
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

/**
 * Fires when a scheduled alarm goes off (or on tapping Snooze, which re-uses the same intent
 * shape). Builds and posts the reminder notification, and — for a recurring item's main
 * (non-early) alarm — advances `dueAt` to the next occurrence: saving that through
 * [ItemRepository] triggers [AlarmScheduler] to schedule the one after it via the same
 * save-time hook the rest of the app uses, so this receiver doesn't need to call the scheduler
 * directly.
 */
@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var itemRepository: ItemRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val itemId = intent.getStringExtra(EXTRA_ITEM_ID) ?: return
        val isEarly = intent.getBooleanExtra(EXTRA_IS_EARLY, false)
        val pendingResult = goAsync()

        scope.launch {
            try {
                handleAlarm(context, itemId, isEarly)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleAlarm(context: Context, itemId: String, isEarly: Boolean) {
        val item = itemRepository.observeItem(itemId).first() ?: return
        if (item.isCompleted || item.deletedAt != null) return

        postNotification(context, item, isEarly)

        if (!isEarly && item.recurrence != null) {
            val next = RecurrenceNextOccurrence.next(item.recurrence, item.dueAt ?: Instant.now())
            itemRepository.save(item.copy(dueAt = next, updatedAt = Instant.now()))
        }
    }

    private fun postNotification(context: Context, item: Item, isEarly: Boolean) {
        val channel = when (item.alertType) {
            AlertType.SOFT -> NotificationChannels.SOFT
            AlertType.MEDIUM -> NotificationChannels.MEDIUM
            // "Repeats until you respond" (DATA_MODEL.md) needs a re-posting loop this build
            // doesn't implement yet — IMPORTANCE_HIGH still gets a heads-up + sound, just once.
            AlertType.INSISTENT -> NotificationChannels.INSISTENT
        }
        val notificationId = if (isEarly) NotificationIds.earlyAlert(item.id) else NotificationIds.mainAlarm(item.id)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_OPEN_ITEM_ID, item.id)
            putExtra(MainActivity.EXTRA_OPEN_ITEM_IS_NOTE, item.isNote)
        }
        val contentIntent = android.app.PendingIntent.getActivity(
            context,
            notificationId,
            openIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(item.title)
            .setContentText(if (isEarly) "Coming up" else item.body?.take(120))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .addAction(0, "Complete", NotificationActionReceiver.completeIntent(context, item.id, notificationId))
            .addAction(0, "Snooze", NotificationActionReceiver.snoozeIntent(context, item.id, notificationId, isEarly))
            .build()

        NotificationManagerCompat.from(context).apply {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS,
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                notify(notificationId, notification)
            }
        }
    }

    companion object {
        const val ACTION_ALARM_FIRED = "com.stackpointer.list.action.ALARM_FIRED"
        const val EXTRA_ITEM_ID = "itemId"
        const val EXTRA_IS_EARLY = "isEarly"
    }
}
