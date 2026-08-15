package com.stackpointer.list.notification

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.stackpointer.list.domain.repository.ItemRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The two interactive edges of a pinned/shown notification that a tap on the content or a
 * "Complete" action (reused from [NotificationActionReceiver]) doesn't cover:
 *  - tapping a checklist row's [RemoteViews][android.widget.RemoteViews] toggles that sub-item
 *    through the same [ItemRepository.toggleSubItem] the in-app checklist UI uses, so Room stays
 *    the single source of truth — [PinnedNotificationManager]'s reactive collector re-posts the
 *    updated RemoteViews on its own, this receiver doesn't touch the notification directly.
 *  - swiping away a "shown" (non-ongoing) notification must flip `isShownInNotificationBar`
 *    back to `false`, or the next unrelated Room emission would silently repost it.
 */
@AndroidEntryPoint
class PinnedNotificationActionReceiver : BroadcastReceiver() {

    @Inject lateinit var itemRepository: ItemRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        scope.launch {
            try {
                when (intent.action) {
                    ACTION_TOGGLE_SUBITEM -> {
                        val subItemId = intent.getStringExtra(EXTRA_SUBITEM_ID) ?: return@launch
                        itemRepository.toggleSubItem(subItemId)
                    }
                    ACTION_DISMISS -> {
                        val itemId = intent.getStringExtra(EXTRA_ITEM_ID) ?: return@launch
                        itemRepository.setShownInNotificationBar(itemId, false)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val ACTION_TOGGLE_SUBITEM = "com.stackpointer.list.action.PINNED_TOGGLE_SUBITEM"
        private const val ACTION_DISMISS = "com.stackpointer.list.action.PINNED_DISMISS"
        private const val EXTRA_ITEM_ID = "itemId"
        private const val EXTRA_SUBITEM_ID = "subItemId"

        fun toggleSubItemIntent(context: Context, subItemId: String): PendingIntent {
            val intent = Intent(context, PinnedNotificationActionReceiver::class.java).apply {
                action = ACTION_TOGGLE_SUBITEM
                putExtra(EXTRA_SUBITEM_ID, subItemId)
            }
            return PendingIntent.getBroadcast(
                context,
                subItemId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        fun dismissIntent(context: Context, itemId: String, notificationId: Int): PendingIntent {
            val intent = Intent(context, PinnedNotificationActionReceiver::class.java).apply {
                action = ACTION_DISMISS
                putExtra(EXTRA_ITEM_ID, itemId)
            }
            return PendingIntent.getBroadcast(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }
    }
}
