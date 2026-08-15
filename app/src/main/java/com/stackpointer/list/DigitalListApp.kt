package com.stackpointer.list

import android.app.Application
import com.stackpointer.list.notification.NotificationChannels
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DigitalListApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationChannels.createAll(this)
    }
}
