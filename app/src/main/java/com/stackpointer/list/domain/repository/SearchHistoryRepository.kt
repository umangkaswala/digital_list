package com.stackpointer.list.domain.repository

import kotlinx.coroutines.flow.Flow

/** Screen 06's "RECENT" rows — most recent first, deduplicated, capped. */
interface SearchHistoryRepository {
    val recentQueries: Flow<List<String>>
    suspend fun addQuery(query: String)
    suspend fun removeQuery(query: String)
}
