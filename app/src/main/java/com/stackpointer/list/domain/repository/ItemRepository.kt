package com.stackpointer.list.domain.repository

import com.stackpointer.list.domain.model.Item
import com.stackpointer.list.domain.model.SavedView
import kotlinx.coroutines.flow.Flow

/**
 * An undo token returned by mutations that can be reversed from a snackbar. Carries the full
 * prior item state (not just an id) so undo restores the exact previous state — including
 * sortOrder and bucket, which are derived from the item's own fields, not stored separately.
 */
data class UndoToken(val previousState: Item, val action: UndoAction)

enum class UndoAction { COMPLETE, DELETE, MOVE }

interface ItemRepository {
    fun observeSavedView(view: SavedView): Flow<List<Item>>
    fun observeItem(id: String): Flow<Item?>
    fun observeAll(): Flow<List<Item>>

    suspend fun save(item: Item)
    suspend fun complete(id: String): UndoToken
    suspend fun delete(id: String): UndoToken
    suspend fun restore(id: String): UndoToken
    suspend fun purgeDeletedBefore(cutoff: java.time.Instant)

    suspend fun setStarred(id: String, starred: Boolean)
    suspend fun setPinned(id: String, pinned: Boolean)
    suspend fun toggleSubItem(subItemId: String)

    suspend fun undo(token: UndoToken)
}
