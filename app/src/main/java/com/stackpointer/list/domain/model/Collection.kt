package com.stackpointer.list.domain.model

/** `iconKey` names a Material Symbol (`work`, `person`, `home`, `flight`, `group`...). */
data class Collection(
    val id: String,
    val name: String,
    val iconKey: String,
    val colorKey: String?,
    /** Shared collections can be left but not deleted — deferred, modelled only. */
    val isShared: Boolean,
    val sortOrder: Int,
)
