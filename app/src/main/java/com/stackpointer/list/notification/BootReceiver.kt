package com.stackpointer.list.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.stackpointer.list.domain.model.SavedView
import com.stackpointer.list.domain.repository.ItemRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Exact alarms don't survive a reboot, and a timezone change invalidates whatever trigger
 * times were already scheduled — DATA_MODEL.md calls for rescheduling everything after both.
 * The "Scheduled" saved view's own query (`triggerType = TIME`, not completed) is exactly the
 * set of items that need alarms, so it's reused here rather than adding a second one. */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var itemRepository: ItemRepository
    @Inject lateinit var alarmScheduler: AlarmScheduler
    @Inject lateinit var pinnedNotificationManager: PinnedNotificationManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && intent.action != Intent.ACTION_TIMEZONE_CHANGED) return
        val pendingResult = goAsync()

        scope.launch {
            try {
                val items = itemRepository.observeSavedView(SavedView.SCHEDULED).first()
                alarmScheduler.rescheduleAll(items)
                // Every notification, ongoing or not, is cleared on reboot too — repost
                // whatever's still pin/show-eligible.
                pinnedNotificationManager.resyncAll()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
