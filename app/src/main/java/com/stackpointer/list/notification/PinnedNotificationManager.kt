package com.stackpointer.list.notification

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.stackpointer.list.MainActivity
import com.stackpointer.list.R
import com.stackpointer.list.domain.model.Item
import com.stackpointer.list.domain.repository.ItemRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Keeps the shade in sync with every item's [Item.isShownInNotificationBar] /
 * [Item.isPinnedToNotification] flags — a continuous reactive projection of Room state, not
 * alarm-driven like [AlarmReceiver]. [start] launches one app-scoped collector over
 * [com.stackpointer.list.domain.repository.ItemRepository.observeNotificationVisibleItems] that
 * reconciles the posted notification set on every emission: cancels ones no longer visible,
 * posts/updates the rest, and maintains one group-summary notification.
 *
 * If both flags are true for an item, it renders once, ongoing (pinned implies shown) — see
 * [reconcile].
 */
@Singleton
class PinnedNotificationManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val itemRepository: ItemRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var collectJob: Job? = null
    private var lastPostedIds: Set<Int> = emptySet()

    fun start() {
        if (collectJob?.isActive == true) return
        collectJob = scope.launch {
            itemRepository.observeNotificationVisibleItems().collect { items -> reconcile(items) }
        }
    }

    /** Every notification, ongoing or not, is cleared on reboot — [BootReceiver] calls this
     * once to repost everything that's still pin/show-eligible, reusing the same [reconcile]
     * the live collector uses rather than a second code path. */
    suspend fun resyncAll() {
        reconcile(itemRepository.observeNotificationVisibleItems().first())
    }

    private fun reconcile(items: List<Item>) {
        val notificationManager = NotificationManagerCompat.from(context)
        val currentIds = items.map { NotificationIds.pinned(it.id) }.toSet()

        (lastPostedIds - currentIds).forEach { staleId -> notificationManager.cancel(staleId) }
        items.forEach { item -> post(notificationManager, item) }

        if (items.isEmpty()) {
            notificationManager.cancel(NotificationIds.PINNED_SUMMARY_ID)
        } else {
            postSummary(notificationManager, items)
        }
        lastPostedIds = currentIds
    }

    private fun post(notificationManager: NotificationManagerCompat, item: Item) {
        if (!hasNotificationPermission()) return

        // Pinned implies shown — an item that's both renders once, as the non-dismissible form.
        val ongoing = item.isPinnedToNotification
        val notificationId = NotificationIds.pinned(item.id)

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

        val builder = NotificationCompat.Builder(context, NotificationChannels.PINNED)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(contentIntent)
            .setOngoing(ongoing)
            .setGroup(GROUP_KEY)
            .setOnlyAlertOnce(true)

        if (item.isChecklist) {
            builder.setCustomContentView(buildChecklistRemoteViews(item, notificationId))
            builder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
        } else {
            builder.setContentTitle(item.title)
            builder.setContentText(item.body?.take(120))
            if (!item.isCompleted) {
                builder.addAction(0, "Complete", NotificationActionReceiver.completeIntent(context, item.id, notificationId))
            }
        }

        if (!ongoing) {
            builder.setAutoCancel(false)
            builder.setDeleteIntent(PinnedNotificationActionReceiver.dismissIntent(context, item.id, notificationId))
        }

        notificationManager.notify(notificationId, builder.build())
    }

    private fun buildChecklistRemoteViews(item: Item, notificationId: Int): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, R.layout.notification_checklist)
        remoteViews.setTextViewText(R.id.notification_checklist_title, item.title)
        remoteViews.removeAllViews(R.id.notification_checklist_rows)

        val sorted = item.subItems.sortedBy { it.sortOrder }
        sorted.take(MAX_CHECKLIST_ROWS).forEach { subItem ->
            val row = RemoteViews(context.packageName, R.layout.notification_checklist_row)
            row.setTextViewText(R.id.notification_checklist_row_text, subItem.text)
            row.setCompoundButtonChecked(R.id.notification_checklist_row_checkbox, subItem.isCompleted)
            row.setInt(
                R.id.notification_checklist_row_text,
                "setPaintFlags",
                if (subItem.isCompleted) Paint.STRIKE_THRU_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG else Paint.ANTI_ALIAS_FLAG,
            )
            row.setOnClickPendingIntent(
                R.id.notification_checklist_row_root,
                PinnedNotificationActionReceiver.toggleSubItemIntent(context, subItem.id),
            )
            remoteViews.addView(R.id.notification_checklist_rows, row)
        }

        val overflow = sorted.size - MAX_CHECKLIST_ROWS
        if (overflow > 0) {
            remoteViews.setViewVisibility(R.id.notification_checklist_more, android.view.View.VISIBLE)
            remoteViews.setTextViewText(R.id.notification_checklist_more, "+$overflow more")
        } else {
            remoteViews.setViewVisibility(R.id.notification_checklist_more, android.view.View.GONE)
        }

        return remoteViews
    }

    private fun postSummary(notificationManager: NotificationManagerCompat, items: List<Item>) {
        if (!hasNotificationPermission()) return

        val summary = NotificationCompat.Builder(context, NotificationChannels.PINNED)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("${items.size} pinned")
            .setStyle(NotificationCompat.InboxStyle().also { style -> items.forEach { style.addLine(it.title) } })
            .setGroup(GROUP_KEY)
            .setGroupSummary(true)
            .setOngoing(items.any { it.isPinnedToNotification })
            .setOnlyAlertOnce(true)
            .build()

        notificationManager.notify(NotificationIds.PINNED_SUMMARY_ID, summary)
    }

    private fun hasNotificationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED

    private companion object {
        const val GROUP_KEY = "com.stackpointer.list.PINNED_GROUP"
        const val MAX_CHECKLIST_ROWS = 8
    }
}
