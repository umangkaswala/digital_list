package com.stackpointer.list

import android.app.Application
import com.stackpointer.list.domain.repository.ItemRepository
import com.stackpointer.list.notification.NotificationChannels
import com.stackpointer.list.notification.PinnedNotificationManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

private const val RECYCLE_BIN_RETENTION_DAYS = 30L

@HiltAndroidApp
class DigitalListApp : Application() {

    @Inject lateinit var pinnedNotificationManager: PinnedNotificationManager
    @Inject lateinit var itemRepository: ItemRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.createAll(this)
        pinnedNotificationManager.start()

        // Screen 29: "Items here are deleted for good after 30 days" — swept once per launch
        // rather than scheduled, since missing a day or two while the app is unused is harmless.
        scope.launch {
            itemRepository.purgeDeletedBefore(Instant.now().minus(Duration.ofDays(RECYCLE_BIN_RETENTION_DAYS)))
        }
    }
}
