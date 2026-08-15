package com.stackpointer.list.data.local.mapper

import com.stackpointer.list.data.local.entity.CollectionEntity
import com.stackpointer.list.data.local.entity.ItemEntity
import com.stackpointer.list.data.local.entity.RecurrenceEntity
import com.stackpointer.list.data.local.entity.SubItemEntity
import com.stackpointer.list.data.local.entity.TemplateEntity
import com.stackpointer.list.data.local.relation.ItemWithDetails
import com.stackpointer.list.domain.model.Collection
import com.stackpointer.list.domain.model.Item
import com.stackpointer.list.domain.model.Recurrence
import com.stackpointer.list.domain.model.SubItem
import com.stackpointer.list.domain.model.Template
import com.stackpointer.list.domain.model.TemplateDraft
import java.time.DayOfWeek

fun ItemWithDetails.toDomain(): Item = Item(
    id = item.id,
    title = item.title,
    body = item.body,
    isCompleted = item.isCompleted,
    completedAt = item.completedAt,
    isStarred = item.isStarred,
    isPinned = item.isPinned,
    isArchived = item.isArchived,
    deletedAt = item.deletedAt,
    triggerType = item.triggerType,
    dueAt = item.dueAt,
    isAllDay = item.isAllDay,
    earlyAlertMinutes = item.earlyAlertMinutes,
    alertType = item.alertType,
    recurrence = recurrence?.toDomain(),
    placeId = item.placeId,
    placeTrigger = item.placeTrigger,
    placeWindow = item.placeWindow,
    subItems = subItems.sortedBy { it.sortOrder }.map { it.toDomain() },
    collections = collections.map { it.toDomain() },
    sortOrder = item.sortOrder,
    createdAt = item.createdAt,
    updatedAt = item.updatedAt,
)

fun Item.toEntity(): ItemEntity = ItemEntity(
    id = id,
    title = title,
    body = body,
    isCompleted = isCompleted,
    completedAt = completedAt,
    isStarred = isStarred,
    isPinned = isPinned,
    isArchived = isArchived,
    deletedAt = deletedAt,
    triggerType = triggerType,
    dueAt = dueAt,
    isAllDay = isAllDay,
    earlyAlertMinutes = earlyAlertMinutes,
    alertType = alertType,
    recurrenceId = recurrence?.id,
    placeId = placeId,
    placeTrigger = placeTrigger,
    placeWindow = placeWindow,
    sortOrder = sortOrder,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun SubItemEntity.toDomain(): SubItem = SubItem(
    id = id,
    itemId = itemId,
    text = text,
    isCompleted = isCompleted,
    sortOrder = sortOrder,
)

fun SubItem.toEntity(): SubItemEntity = SubItemEntity(
    id = id,
    itemId = itemId,
    text = text,
    isCompleted = isCompleted,
    sortOrder = sortOrder,
)

fun CollectionEntity.toDomain(): Collection = Collection(
    id = id,
    name = name,
    iconKey = iconKey,
    colorKey = colorKey,
    isShared = isShared,
    sortOrder = sortOrder,
)

fun Collection.toEntity(): CollectionEntity = CollectionEntity(
    id = id,
    name = name,
    iconKey = iconKey,
    colorKey = colorKey,
    isShared = isShared,
    sortOrder = sortOrder,
)

fun RecurrenceEntity.toDomain(): Recurrence = Recurrence(
    id = id,
    freq = freq,
    interval = interval,
    weekdays = weekdaysMask.toWeekdaySet(),
    monthDay = monthDay,
    endType = endType,
    endDate = endDate,
    endCount = endCount,
)

fun Recurrence.toEntity(): RecurrenceEntity = RecurrenceEntity(
    id = id,
    freq = freq,
    interval = interval,
    weekdaysMask = weekdays.toBitmask(),
    monthDay = monthDay,
    endType = endType,
    endDate = endDate,
    endCount = endCount,
)

fun TemplateEntity.toDomain(): Template = Template(
    id = id,
    title = title,
    description = description,
    iconKey = iconKey,
    draft = TemplateDraft(
        title = draftTitle,
        body = draftBody,
        triggerType = draftTriggerType,
        recurrenceFreq = draftRecurrenceFreq,
        recurrenceWeekdays = draftRecurrenceWeekdaysMask.toWeekdaySet(),
        recurrenceMonthDay = draftRecurrenceMonthDay,
        dueInDays = draftDueInDays,
        dueHour = draftDueHour,
        dueMinute = draftDueMinute,
        subItemTexts = if (draftSubItems.isEmpty()) emptyList() else draftSubItems.split("\n"),
    ),
)

fun Template.toEntity(): TemplateEntity = TemplateEntity(
    id = id,
    title = title,
    description = description,
    iconKey = iconKey,
    draftTitle = draft.title,
    draftBody = draft.body,
    draftTriggerType = draft.triggerType,
    draftRecurrenceFreq = draft.recurrenceFreq,
    draftRecurrenceWeekdaysMask = draft.recurrenceWeekdays.toBitmask(),
    draftRecurrenceMonthDay = draft.recurrenceMonthDay,
    draftDueInDays = draft.dueInDays,
    draftDueHour = draft.dueHour,
    draftDueMinute = draft.dueMinute,
    draftSubItems = draft.subItemTexts.joinToString("\n"),
)

private fun Int.toWeekdaySet(): Set<DayOfWeek> =
    DayOfWeek.entries.filter { (this shr (it.value - 1)) and 1 == 1 }.toSet()

private fun Set<DayOfWeek>.toBitmask(): Int =
    fold(0) { mask, day -> mask or (1 shl (day.value - 1)) }
