package com.stackpointer.list.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.stackpointer.list.domain.model.RecurrenceFreq
import com.stackpointer.list.domain.model.TriggerType

/**
 * The draft fields are flattened onto this table rather than serialized as one blob — the
 * draft shape is small and bounded (see [com.stackpointer.list.domain.model.TemplateDraft]),
 * so plain columns are simpler than introducing a JSON dependency for six seed rows.
 * [draftSubItems] joins its texts with a newline (sub-item text is single-line UI input, so
 * this can't collide with real content).
 */
@Entity(tableName = "templates")
data class TemplateEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val iconKey: String,
    val draftTitle: String,
    val draftBody: String?,
    val draftTriggerType: TriggerType,
    val draftRecurrenceFreq: RecurrenceFreq?,
    val draftRecurrenceWeekdaysMask: Int,
    val draftRecurrenceMonthDay: Int?,
    val draftDueInDays: Int?,
    val draftDueHour: Int?,
    val draftDueMinute: Int?,
    val draftSubItems: String,
)
