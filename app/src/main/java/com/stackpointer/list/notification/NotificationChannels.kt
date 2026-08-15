package com.stackpointer.list.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

/** The three alert-type channels from DATA_MODEL.md, plus the M7b `Pinned` channel used by
 * [PinnedNotificationManager]. Created once from [com.stackpointer.list.DigitalListApp.onCreate]
 * — channel creation is idempotent per id, so there's no harm calling this more than once. */
object NotificationChannels {
    const val SOFT = "alert_soft"
    const val MEDIUM = "alert_medium"
    const val INSISTENT = "alert_insistent"
    const val PINNED = "pinned"

    fun createAll(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannels(
            listOf(
                NotificationChannel(SOFT, "Soft alerts", NotificationManager.IMPORTANCE_LOW).apply {
                    description = "Silent notification only"
                    setSound(null, null)
                },
                NotificationChannel(MEDIUM, "Medium alerts", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Sound once, then quiet"
                },
                NotificationChannel(INSISTENT, "Insistent alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Repeats until you respond"
                },
                NotificationChannel(PINNED, "Pinned notes and tasks", NotificationManager.IMPORTANCE_LOW).apply {
                    description = "Items shown or pinned in the notification bar"
                    setSound(null, null)
                },
            ),
        )
    }
}
