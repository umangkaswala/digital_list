package com.stackpointer.list.data.local.relation

import androidx.room.Embedded
import com.stackpointer.list.data.local.entity.CollectionEntity

/** Backs screen 27's "N items · N due" supporting text — computed via correlated subqueries in
 * [com.stackpointer.list.data.local.dao.CollectionDao.observeAllWithCounts] rather than a
 * `@Relation` list, since only the counts are needed, not the items themselves. */
data class CollectionWithCounts(
    @Embedded val collection: CollectionEntity,
    val itemCount: Int,
    val dueCount: Int,
)
