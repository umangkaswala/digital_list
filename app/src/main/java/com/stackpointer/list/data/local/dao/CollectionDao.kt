package com.stackpointer.list.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.stackpointer.list.data.local.entity.CollectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {

    @Query("SELECT * FROM collections ORDER BY sortOrder")
    fun observeAll(): Flow<List<CollectionEntity>>

    @Upsert
    suspend fun upsert(collection: CollectionEntity)

    @Query("DELETE FROM collections WHERE id = :id")
    suspend fun delete(id: String)
}
