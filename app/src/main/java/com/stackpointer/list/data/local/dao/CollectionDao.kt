package com.stackpointer.list.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.stackpointer.list.data.local.entity.CollectionEntity
import com.stackpointer.list.data.local.relation.CollectionWithCounts
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {

    @Query("SELECT * FROM collections ORDER BY sortOrder")
    fun observeAll(): Flow<List<CollectionEntity>>

    @Query(
        """
        SELECT c.*,
            (SELECT COUNT(*) FROM item_collections ic JOIN items i ON i.id = ic.itemId
                WHERE ic.collectionId = c.id AND i.deletedAt IS NULL) AS itemCount,
            (SELECT COUNT(*) FROM item_collections ic JOIN items i ON i.id = ic.itemId
                WHERE ic.collectionId = c.id AND i.deletedAt IS NULL AND i.triggerType = 'TIME' AND i.isCompleted = 0) AS dueCount
        FROM collections c ORDER BY c.sortOrder
        """,
    )
    fun observeAllWithCounts(): Flow<List<CollectionWithCounts>>

    @Upsert
    suspend fun upsert(collection: CollectionEntity)

    @Query("DELETE FROM collections WHERE id = :id")
    suspend fun delete(id: String)
}
