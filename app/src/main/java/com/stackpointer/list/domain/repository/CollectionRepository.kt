package com.stackpointer.list.domain.repository

import com.stackpointer.list.domain.model.Collection
import com.stackpointer.list.domain.model.CollectionSummary
import kotlinx.coroutines.flow.Flow

interface CollectionRepository {
    fun observeAll(): Flow<List<Collection>>
    fun observeAllWithCounts(): Flow<List<CollectionSummary>>
    suspend fun save(collection: Collection)
    suspend fun delete(id: String)
    suspend fun addItem(itemId: String, collectionId: String)
    suspend fun removeItem(itemId: String, collectionId: String)
}
