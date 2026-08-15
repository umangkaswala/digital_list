package com.stackpointer.list.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.stackpointer.list.data.local.entity.TemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateDao {

    @Query("SELECT * FROM templates")
    fun observeAll(): Flow<List<TemplateEntity>>

    @Query("SELECT COUNT(*) FROM templates")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(templates: List<TemplateEntity>)
}
