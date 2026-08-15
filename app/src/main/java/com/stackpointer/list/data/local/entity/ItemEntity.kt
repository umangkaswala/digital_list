package com.stackpointer.list.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.stackpointer.list.domain.model.AlertType
import com.stackpointer.list.domain.model.PlaceTrigger
import com.stackpointer.list.domain.model.PlaceWindow
import com.stackpointer.list.domain.model.TriggerType
import java.time.Instant

@Entity(
    tableName = "items",
    foreignKeys = [
        ForeignKey(
            entity = RecurrenceEntity::class,
            parentColumns = ["id"],
            childColumns = ["recurrenceId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = PlaceEntity::class,
            parentColumns = ["id"],
            childColumns = ["placeId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("recurrenceId"), Index("placeId")],
)
data class ItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val body: String?,
    val isCompleted: Boolean,
    val completedAt: Instant?,
    val isStarred: Boolean,
    val isPinned: Boolean,
    val isArchived: Boolean,
    val deletedAt: Instant?,
    val triggerType: TriggerType,
    val dueAt: Instant?,
    val isAllDay: Boolean,
    val earlyAlertMinutes: Int?,
    val alertType: AlertType,
    val recurrenceId: String?,
    val placeId: String?,
    val placeTrigger: PlaceTrigger?,
    val placeWindow: PlaceWindow?,
    val sortOrder: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
    val isShownInNotificationBar: Boolean = false,
    val isPinnedToNotification: Boolean = false,
)
