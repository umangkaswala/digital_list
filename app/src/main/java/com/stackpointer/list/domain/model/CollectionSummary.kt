package com.stackpointer.list.domain.model

/** [Collection] plus screen 27's "N items · N due" counts. */
data class CollectionSummary(
    val collection: Collection,
    val itemCount: Int,
    val dueCount: Int,
)
