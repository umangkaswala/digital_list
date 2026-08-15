package com.stackpointer.list.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.stackpointer.list.data.local.migration.MIGRATION_1_2
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TEST_DB = "migration-test"

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        DigitalListDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    fun migrate1To2_addsNotificationColumns_defaultingToFalse() {
        helper.createDatabase(TEST_DB, 1).apply {
            execSQL(
                """
                INSERT INTO items (
                    id, title, body, isCompleted, completedAt, isStarred, isPinned, isArchived,
                    deletedAt, triggerType, dueAt, isAllDay, earlyAlertMinutes, alertType,
                    recurrenceId, placeId, placeTrigger, placeWindow, sortOrder, createdAt, updatedAt
                ) VALUES (
                    '1', 'Pre-migration item', NULL, 0, NULL, 0, 0, 0,
                    NULL, 'NONE', NULL, 0, NULL, 'MEDIUM',
                    NULL, NULL, NULL, NULL, 0, 0, 0
                )
                """.trimIndent(),
            )
            close()
        }

        val migratedDb = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)
        migratedDb.query("SELECT isShownInNotificationBar, isPinnedToNotification FROM items WHERE id = '1'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
            assertEquals(0, cursor.getInt(1))
        }
    }
}
