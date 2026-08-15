package com.stackpointer.list.data.repository

import com.stackpointer.list.data.local.dao.CollectionDao
import com.stackpointer.list.data.local.dao.ItemDao
import com.stackpointer.list.data.local.entity.ItemCollectionCrossRef
import com.stackpointer.list.data.local.mapper.toDomain
import com.stackpointer.list.data.local.mapper.toEntity
import com.stackpointer.list.domain.model.Collection
import com.stackpointer.list.domain.repository.CollectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CollectionRepositoryImpl @Inject constructor(
    private val collectionDao: CollectionDao,
    private val itemDao: ItemDao,
) : CollectionRepository {

    override fun observeAll(): Flow<List<Collection>> =
        collectionDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun save(collection: Collection) {
        collectionDao.upsert(collection.toEntity())
    }

    override suspend fun delete(id: String) {
        collectionDao.delete(id)
    }

    override suspend fun addItem(itemId: String, collectionId: String) {
        itemDao.insertItemCollections(listOf(ItemCollectionCrossRef(itemId, collectionId)))
    }

    override suspend fun removeItem(itemId: String, collectionId: String) {
        // Items you remove from a collection stay in your list — only the cross-ref goes.
        itemDao.removeItemCollection(itemId, collectionId)
    }
}
