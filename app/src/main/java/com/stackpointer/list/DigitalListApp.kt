package com.stackpointer.list

import android.app.Application
import com.stackpointer.list.notification.NotificationChannels
import com.stackpointer.list.notification.PinnedNotificationManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class DigitalListApp : Application() {

    @Inject lateinit var pinnedNotificationManager: PinnedNotificationManager

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.createAll(this)
        pinnedNotificationManager.start()
    }
}
