package com.stackpointer.list.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.stackpointer.list.data.local.entity.ItemCollectionCrossRef
import com.stackpointer.list.data.local.entity.ItemEntity
import com.stackpointer.list.data.local.entity.SubItemEntity
import com.stackpointer.list.data.local.relation.ItemWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Transaction
    @Query("SELECT * FROM items WHERE id = :id")
    fun observeItem(id: String): Flow<ItemWithDetails?>

    @Transaction
    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getItem(id: String): ItemWithDetails?

    @Transaction
    @Query("SELECT * FROM items")
    fun observeAll(): Flow<List<ItemWithDetails>>

    /** "dueAt within today, or overdue" plus anything completed today, so the Completed
     * bucket has something to show — see DATA_MODEL.md's Today row. */
    @Transaction
    @Query(
        """
        SELECT * FROM items WHERE deletedAt IS NULL AND triggerType = 'TIME' AND (
            (isCompleted = 0 AND dueAt < :todayEnd) OR
            (isCompleted = 1 AND completedAt >= :todayStart AND completedAt < :todayEnd)
        )
        """,
    )
    fun observeToday(todayStart: Long, todayEnd: Long): Flow<List<ItemWithDetails>>

    @Transaction
    @Query("SELECT * FROM items WHERE deletedAt IS NULL AND triggerType = 'TIME' AND isCompleted = 0")
    fun observeScheduled(): Flow<List<ItemWithDetails>>

    @Transaction
    @Query("SELECT * FROM items WHERE deletedAt IS NULL AND isStarred = 1")
    fun observeStarred(): Flow<List<ItemWithDetails>>

    @Transaction
    @Query("SELECT * FROM items WHERE deletedAt IS NULL AND triggerType = 'PLACE'")
    fun observePlace(): Flow<List<ItemWithDetails>>

    @Transaction
    @Query("SELECT * FROM items WHERE deletedAt IS NULL AND triggerType = 'NONE' AND isCompleted = 0")
    fun observeNoAlert(): Flow<List<ItemWithDetails>>

    @Transaction
    @Query("SELECT * FROM items WHERE deletedAt IS NULL AND isCompleted = 1")
    fun observeCompleted(): Flow<List<ItemWithDetails>>

    @Transaction
    @Query("SELECT * FROM items WHERE deletedAt IS NOT NULL")
    fun observeRecycleBin(): Flow<List<ItemWithDetails>>

    @Transaction
    @Query("SELECT * FROM items WHERE deletedAt IS NULL AND (isShownInNotificationBar = 1 OR isPinnedToNotification = 1)")
    fun observeNotificationVisibleItems(): Flow<List<ItemWithDetails>>

    @Upsert
    suspend fun upsertItem(item: ItemEntity)

    @Query("DELETE FROM sub_items WHERE itemId = :itemId")
    suspend fun clearSubItems(itemId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubItems(subItems: List<SubItemEntity>)

    @Query("DELETE FROM item_collections WHERE itemId = :itemId")
    suspend fun clearItemCollections(itemId: String)

    @Query("DELETE FROM item_collections WHERE itemId = :itemId AND collectionId = :collectionId")
    suspend fun removeItemCollection(itemId: String, collectionId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItemCollections(refs: List<ItemCollectionCrossRef>)

    @Query("UPDATE sub_items SET isCompleted = NOT isCompleted WHERE id = :subItemId")
    suspend fun toggleSubItem(subItemId: String)

    @Query("UPDATE items SET isStarred = :starred, updatedAt = :now WHERE id = :id")
    suspend fun setStarred(id: String, starred: Boolean, now: Long)

    @Query("UPDATE items SET isPinned = :pinned, updatedAt = :now WHERE id = :id")
    suspend fun setPinned(id: String, pinned: Boolean, now: Long)

    @Query("UPDATE items SET isShownInNotificationBar = :shown, updatedAt = :now WHERE id = :id")
    suspend fun setShownInNotificationBar(id: String, shown: Boolean, now: Long)

    @Query("UPDATE items SET isPinnedToNotification = :pinned, updatedAt = :now WHERE id = :id")
    suspend fun setPinnedToNotification(id: String, pinned: Boolean, now: Long)

    @Query("UPDATE items SET deletedAt = :deletedAt, updatedAt = :now WHERE id = :id")
    suspend fun setDeletedAt(id: String, deletedAt: Long?, now: Long)

    @Delete
    suspend fun deleteItem(item: ItemEntity)

    @Query("SELECT * FROM items WHERE deletedAt IS NOT NULL AND deletedAt < :cutoff")
    suspend fun getDeletedBefore(cutoff: Long): List<ItemEntity>

    @Query("DELETE FROM items WHERE deletedAt IS NOT NULL AND deletedAt < :cutoff")
    suspend fun purgeDeletedBefore(cutoff: Long)
}
