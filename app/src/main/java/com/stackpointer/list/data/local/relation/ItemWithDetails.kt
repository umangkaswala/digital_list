package com.stackpointer.list.data.local.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.stackpointer.list.data.local.entity.CollectionEntity
import com.stackpointer.list.data.local.entity.ItemCollectionCrossRef
import com.stackpointer.list.data.local.entity.ItemEntity
import com.stackpointer.list.data.local.entity.RecurrenceEntity
import com.stackpointer.list.data.local.entity.SubItemEntity

data class ItemWithDetails(
    @Embedded val item: ItemEntity,
    @Relation(parentColumn = "id", entityColumn = "itemId")
    val subItems: List<SubItemEntity>,
    @Relation(parentColumn = "recurrenceId", entityColumn = "id")
    val recurrence: RecurrenceEntity?,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ItemCollectionCrossRef::class,
            parentColumn = "itemId",
            entityColumn = "collectionId",
        ),
    )
    val collections: List<CollectionEntity>,
)
