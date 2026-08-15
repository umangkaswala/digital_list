package com.stackpointer.list.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Deferred (see `Features.placeReminders`) — modelled now so the schema doesn't change later. */
@Entity(tableName = "places")
data class PlaceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val iconKey: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Int,
    val address: String?,
)
