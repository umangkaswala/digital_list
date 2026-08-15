package com.stackpointer.list.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "collections")
data class CollectionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val iconKey: String,
    val colorKey: String?,
    val isShared: Boolean,
    val sortOrder: Int,
)
