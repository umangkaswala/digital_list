package com.stackpointer.list.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sub_items",
    foreignKeys = [
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("itemId")],
)
data class SubItemEntity(
    @PrimaryKey val id: String,
    val itemId: String,
    val text: String,
    val isCompleted: Boolean,
    val sortOrder: Int,
)
