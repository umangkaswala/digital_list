package com.stackpointer.list.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.stackpointer.list.domain.repository.SearchHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private const val MAX_RECENT = 5

// A control character no real search query would ever contain, used to join the recent-queries
// list into one string preference — Preferences DataStore has no native ordered-list type, and
// a stringSetPreferencesKey doesn't preserve insertion order, which "most recent first" needs.
private const val SEPARATOR = ""

@Singleton
class SearchHistoryRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SearchHistoryRepository {

    override val recentQueries: Flow<List<String>> = dataStore.data.map { prefs ->
        prefs[RECENT_QUERIES]?.split(SEPARATOR)?.filter { it.isNotBlank() } ?: emptyList()
    }

    override suspend fun addQuery(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return
        dataStore.edit { prefs ->
            val current = prefs[RECENT_QUERIES]?.split(SEPARATOR)?.filter { it.isNotBlank() } ?: emptyList()
            val updated = (listOf(trimmed) + current.filterNot { it.equals(trimmed, ignoreCase = true) }).take(MAX_RECENT)
            prefs[RECENT_QUERIES] = updated.joinToString(SEPARATOR)
        }
    }

    override suspend fun removeQuery(query: String) {
        dataStore.edit { prefs ->
            val current = prefs[RECENT_QUERIES]?.split(SEPARATOR)?.filter { it.isNotBlank() } ?: emptyList()
            prefs[RECENT_QUERIES] = current.filterNot { it == query }.joinToString(SEPARATOR)
        }
    }

    private companion object {
        val RECENT_QUERIES = stringPreferencesKey("recent_search_queries")
    }
}
