package com.stackpointer.list.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.stackpointer.list.data.local.entity.RecurrenceEntity

@Dao
interface RecurrenceDao {

    @Upsert
    suspend fun upsert(recurrence: RecurrenceEntity)

    @Query("DELETE FROM recurrences WHERE id = :id")
    suspend fun delete(id: String)
}
