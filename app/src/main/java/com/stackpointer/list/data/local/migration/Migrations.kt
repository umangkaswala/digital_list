package com.stackpointer.list.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** v1 -> v2: adds the two M7b notification-bar flags to `items`. Both default `0` so every
 * existing row keeps its current (non-pinned, non-shown) notification state. */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE items ADD COLUMN isShownInNotificationBar INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE items ADD COLUMN isPinnedToNotification INTEGER NOT NULL DEFAULT 0")
    }
}
