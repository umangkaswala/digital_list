package com.stackpointer.list.domain.model

import java.time.DayOfWeek

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
)
