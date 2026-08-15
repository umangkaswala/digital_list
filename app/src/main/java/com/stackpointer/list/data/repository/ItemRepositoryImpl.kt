package com.stackpointer.list.data.repository

import com.stackpointer.list.data.local.dao.ItemDao
import com.stackpointer.list.data.local.dao.RecurrenceDao
import com.stackpointer.list.data.local.entity.ItemCollectionCrossRef
import com.stackpointer.list.data.local.mapper.toDomain
import com.stackpointer.list.data.local.mapper.toEntity
import com.stackpointer.list.domain.model.Item
import com.stackpointer.list.domain.model.SavedView
import com.stackpointer.list.domain.repository.ItemRepository
import com.stackpointer.list.domain.repository.UndoAction
import com.stackpointer.list.domain.repository.UndoToken
import com.stackpointer.list.domain.usecase.RecurrenceNextOccurrence
import com.stackpointer.list.notification.AlarmScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemRepositoryImpl @Inject constructor(
    private val itemDao: ItemDao,
    private val recurrenceDao: RecurrenceDao,
    private val alarmScheduler: AlarmScheduler,
) : ItemRepository {

    override fun observeSavedView(view: SavedView): Flow<List<Item>> {
        val flow = when (view) {
            SavedView.TODAY -> {
                val zone = ZoneId.systemDefault()
                val todayStart = todayStartMillis(zone)
                val todayEnd = todayStart + DAY_MILLIS
                itemDao.observeToday(todayStart, todayEnd)
            }
            SavedView.SCHEDULED -> itemDao.observeScheduled()
            SavedView.STARRED -> itemDao.observeStarred()
            SavedView.PLACE -> itemDao.observePlace()
            SavedView.NO_ALERT -> itemDao.observeNoAlert()
            SavedView.COMPLETED -> itemDao.observeCompleted()
            SavedView.RECYCLE_BIN -> itemDao.observeRecycleBin()
        }
        return flow.map { list -> list.map { it.toDomain() } }
    }

    override fun observeItem(id: String): Flow<Item?> =
        itemDao.observeItem(id).map { it?.toDomain() }

    override fun observeAll(): Flow<List<Item>> =
        itemDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun save(item: Item) {
        item.recurrence?.let { recurrenceDao.upsert(it.toEntity()) }
        itemDao.upsertItem(item.toEntity())

        itemDao.clearSubItems(item.id)
        if (item.subItems.isNotEmpty()) {
            itemDao.insertSubItems(item.subItems.map { it.toEntity() })
        }

        itemDao.clearItemCollections(item.id)
        if (item.collections.isNotEmpty()) {
            itemDao.insertItemCollections(item.collections.map { ItemCollectionCrossRef(item.id, it.id) })
        }

        alarmScheduler.reschedule(item)
    }

    override suspend fun complete(id: String): UndoToken {
        val previous = requireNotNull(itemDao.getItem(id)) { "No item with id $id" }.toDomain()
        val now = Instant.now()

        if (previous.recurrence != null) {
            // A recurring item advances to its next occurrence rather than being marked done
            // for good — see DATA_MODEL.md's "Completing a recurring item" note.
            val next = RecurrenceNextOccurrence.next(previous.recurrence, previous.dueAt ?: now)
            val updated = previous.copy(dueAt = next, updatedAt = now)
            itemDao.upsertItem(updated.toEntity())
            alarmScheduler.reschedule(updated)
        } else {
            itemDao.upsertItem(previous.copy(isCompleted = true, completedAt = now, updatedAt = now).toEntity())
            alarmScheduler.cancel(id)
        }
        return UndoToken(previous, UndoAction.COMPLETE)
    }

    override suspend fun delete(id: String): UndoToken {
        val previous = requireNotNull(itemDao.getItem(id)) { "No item with id $id" }.toDomain()
        itemDao.setDeletedAt(id, Instant.now().toEpochMilli(), Instant.now().toEpochMilli())
        alarmScheduler.cancel(id)
        return UndoToken(previous, UndoAction.DELETE)
    }

    override suspend fun restore(id: String): UndoToken {
        val previous = requireNotNull(itemDao.getItem(id)) { "No item with id $id" }.toDomain()
        itemDao.setDeletedAt(id, null, Instant.now().toEpochMilli())
        alarmScheduler.reschedule(previous.copy(deletedAt = null))
        return UndoToken(previous, UndoAction.DELETE)
    }

    override suspend fun purgeDeletedBefore(cutoff: Instant) {
        itemDao.purgeDeletedBefore(cutoff.toEpochMilli())
    }

    override suspend fun setStarred(id: String, starred: Boolean) {
        itemDao.setStarred(id, starred, Instant.now().toEpochMilli())
    }

    override suspend fun setPinned(id: String, pinned: Boolean) {
        itemDao.setPinned(id, pinned, Instant.now().toEpochMilli())
    }

    override suspend fun toggleSubItem(subItemId: String) {
        itemDao.toggleSubItem(subItemId)
    }

    override suspend fun undo(token: UndoToken) {
        // The token already carries the full prior item state — including sortOrder and
        // whatever bucket it implies — so reverting is just writing it back.
        itemDao.upsertItem(token.previousState.toEntity())
        alarmScheduler.reschedule(token.previousState)
    }

    private fun todayStartMillis(zone: ZoneId): Long =
        ZonedDateTime.now(zone).toLocalDate().atStartOfDay(zone).toInstant().toEpochMilli()

    private companion object {
        const val DAY_MILLIS = 24L * 60 * 60 * 1000
    }
}
