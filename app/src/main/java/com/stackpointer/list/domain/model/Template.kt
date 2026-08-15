package com.stackpointer.list.domain.model

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID

/** A "Try these out" starter (screen 28). Tapping one opens the capture sheet prefilled
 * from [draft]; the trailing `add` button commits it directly. */
data class Template(
    val id: String,
    val title: String,
    val description: String,
    val iconKey: String,
    val draft: TemplateDraft,
)

/**
 * What a template prefills into the capture sheet — deliberately smaller than [Item] (no id,
 * timestamps, or completion state, since those don't exist until the draft is actually used).
 */
data class TemplateDraft(
    val title: String,
    val body: String? = null,
    val triggerType: TriggerType = TriggerType.NONE,
    val recurrenceFreq: RecurrenceFreq? = null,
    val recurrenceWeekdays: Set<DayOfWeek> = emptySet(),
    val recurrenceMonthDay: Int? = null,
    /** For a one-time (non-recurring) trigger, due this many days after the template is used. */
    val dueInDays: Int? = null,
    val dueHour: Int? = null,
    val dueMinute: Int? = null,
    val subItemTexts: List<String> = emptyList(),
) {
    /** Turns this draft into a real, persistable [Item] — used both by the "add" button on a
     * template card (commits directly, screen 28) and could seed the capture sheet the same
     * way if a card is tapped to review first. */
    fun toItem(now: Instant = Instant.now(), zone: ZoneId = ZoneId.systemDefault()): Item {
        val id = UUID.randomUUID().toString()
        val dueAt = when {
            dueInDays != null -> now.atZone(zone).toLocalDate().plusDays(dueInDays.toLong())
                .atTime(LocalTime.of(dueHour ?: 9, dueMinute ?: 0)).atZone(zone).toInstant()
            recurrenceFreq != null -> now.atZone(zone).toLocalDate()
                .atTime(LocalTime.of(dueHour ?: 9, dueMinute ?: 0)).atZone(zone).toInstant()
            else -> null
        }
        val recurrence = recurrenceFreq?.let {
            Recurrence(
                id = UUID.randomUUID().toString(),
                freq = it,
                weekdays = recurrenceWeekdays,
                monthDay = recurrenceMonthDay,
            )
        }
        return Item.draft().copy(
            id = id,
            title = title,
            body = body,
            triggerType = triggerType,
            dueAt = dueAt,
            recurrence = recurrence,
            subItems = subItemTexts.mapIndexed { index, text ->
                SubItem(id = UUID.randomUUID().toString(), itemId = id, text = text, isCompleted = false, sortOrder = index)
            },
            createdAt = now,
            updatedAt = now,
        )
    }
}
