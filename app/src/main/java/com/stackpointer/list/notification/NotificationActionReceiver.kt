package com.stackpointer.list.notification

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.stackpointer.list.domain.repository.ItemRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Handles the Complete and Snooze actions on a reminder notification (screen-24-style
 * "Complete" reused here; "Open" is just the notification's own content intent into
 * MainActivity, so it doesn't need a receiver). */
@AndroidEntryPoint
class NotificationActionReceiver : BroadcastReceiver() {

    @Inject lateinit var itemRepository: ItemRepository
    @Inject lateinit var alarmScheduler: AlarmScheduler

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val itemId = intent.getStringExtra(EXTRA_ITEM_ID) ?: return
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        val pendingResult = goAsync()

        scope.launch {
            try {
                when (intent.action) {
                    ACTION_COMPLETE -> itemRepository.complete(itemId)
                    ACTION_SNOOZE -> alarmScheduler.snooze(itemId, intent.getBooleanExtra(EXTRA_IS_EARLY, false))
                }
                if (notificationId != -1) {
                    NotificationManagerCompat.from(context).cancel(notificationId)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val ACTION_COMPLETE = "com.stackpointer.list.action.NOTIFICATION_COMPLETE"
        private const val ACTION_SNOOZE = "com.stackpointer.list.action.NOTIFICATION_SNOOZE"
        private const val EXTRA_ITEM_ID = "itemId"
        private const val EXTRA_NOTIFICATION_ID = "notificationId"
        private const val EXTRA_IS_EARLY = "isEarly"

        fun completeIntent(context: Context, itemId: String, notificationId: Int): PendingIntent {
            val intent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = ACTION_COMPLETE
                putExtra(EXTRA_ITEM_ID, itemId)
                putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            }
            return PendingIntent.getBroadcast(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        fun snoozeIntent(context: Context, itemId: String, notificationId: Int, isEarly: Boolean): PendingIntent {
            val intent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = ACTION_SNOOZE
                putExtra(EXTRA_ITEM_ID, itemId)
                putExtra(EXTRA_NOTIFICATION_ID, notificationId)
                putExtra(EXTRA_IS_EARLY, isEarly)
            }
            // Distinct request code from completeIntent (bitwise-inverted) so Android doesn't
            // collapse the two PendingIntents for the same notification into one.
            return PendingIntent.getBroadcast(
                context,
                notificationId.inv(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }
    }
}
