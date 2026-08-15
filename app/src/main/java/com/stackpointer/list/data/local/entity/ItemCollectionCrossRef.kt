package com.stackpointer.list.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "item_collections",
    primaryKeys = ["itemId", "collectionId"],
    foreignKeys = [
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = CollectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("itemId"), Index("collectionId")],
)
data class ItemCollectionCrossRef(
    val itemId: String,
    val collectionId: String,
)
