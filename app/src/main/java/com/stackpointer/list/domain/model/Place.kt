package com.stackpointer.list.domain.model

/** Deferred (see `Features.placeReminders`) — modelled now so the schema doesn't change later. */
data class Place(
    val id: String,
    val name: String,
    val iconKey: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Int = 200,
    val address: String?,
)

enum class PlaceTrigger { ARRIVE, LEAVE }

enum class PlaceWindow { ANYTIME, MORNING, AFTERNOON, EVENING, NIGHT }
