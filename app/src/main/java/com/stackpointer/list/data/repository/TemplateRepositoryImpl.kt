package com.stackpointer.list.data.repository

import com.stackpointer.list.data.local.dao.TemplateDao
import com.stackpointer.list.data.local.mapper.toDomain
import com.stackpointer.list.data.local.mapper.toEntity
import com.stackpointer.list.domain.model.RecurrenceFreq
import com.stackpointer.list.domain.model.Template
import com.stackpointer.list.domain.model.TemplateDraft
import com.stackpointer.list.domain.model.TriggerType
import com.stackpointer.list.domain.repository.TemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemplateRepositoryImpl @Inject constructor(
    private val templateDao: TemplateDao,
) : TemplateRepository {

    override fun observeAll(): Flow<List<Template>> =
        templateDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun seedIfEmpty() {
        if (templateDao.count() > 0) return
        templateDao.insertAll(starterTemplates().map { it.toEntity() })
    }

    // The six starter templates from screen 28. "Parking photo" prefills a place trigger,
    // which is deferred — its draft has no trigger set yet.
    private fun starterTemplates(): List<Template> = listOf(
        Template(
            id = UUID.randomUUID().toString(),
            title = "Walk with family",
            description = "Every week on Sat",
            iconKey = "lightbulb",
            draft = TemplateDraft(
                title = "Walk with family",
                triggerType = TriggerType.TIME,
                recurrenceFreq = RecurrenceFreq.WEEKLY,
                recurrenceWeekdays = setOf(DayOfWeek.SATURDAY),
            ),
        ),
        Template(
            id = UUID.randomUUID().toString(),
            title = "Check the payslip",
            description = "Every month on the 25th",
            iconKey = "lightbulb",
            draft = TemplateDraft(
                title = "Check the payslip",
                triggerType = TriggerType.TIME,
                recurrenceFreq = RecurrenceFreq.MONTHLY,
                recurrenceMonthDay = 25,
            ),
        ),
        Template(
            id = UUID.randomUUID().toString(),
            title = "Parking photo",
            description = "When you leave the car",
            iconKey = "lightbulb",
            draft = TemplateDraft(title = "Parking photo"),
        ),
        Template(
            id = UUID.randomUUID().toString(),
            title = "Commuting essentials",
            description = "Headphones, wallet / Every day at 7:00 AM, or when you leave home",
            iconKey = "lightbulb",
            draft = TemplateDraft(
                title = "Commuting essentials",
                body = "Headphones, wallet",
                triggerType = TriggerType.TIME,
                recurrenceFreq = RecurrenceFreq.DAILY,
                dueHour = 7,
                dueMinute = 0,
            ),
        ),
        Template(
            id = UUID.randomUUID().toString(),
            title = "Weekend chores",
            description = "Wash clothes and sheets, Vacuum the floors, Take out the rubbish",
            iconKey = "lightbulb",
            draft = TemplateDraft(
                title = "Weekend chores",
                subItemTexts = listOf("Wash clothes and sheets", "Vacuum the floors", "Take out the rubbish"),
            ),
        ),
        Template(
            id = UUID.randomUUID().toString(),
            title = "Return library books",
            description = "21 Aug",
            iconKey = "lightbulb",
            draft = TemplateDraft(
                title = "Return library books",
                triggerType = TriggerType.TIME,
                dueInDays = 7,
            ),
        ),
    )
}
