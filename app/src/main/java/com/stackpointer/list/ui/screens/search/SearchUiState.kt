package com.stackpointer.list.ui.screens.search

import com.stackpointer.list.domain.model.Item

/** Screen 06's scope chips — "Everything", "Notes", "Tasks", "Archive". */
enum class SearchScope { EVERYTHING, NOTES, TASKS, ARCHIVE }

data class SearchUiState(
    val query: String = "",
    val scope: SearchScope = SearchScope.EVERYTHING,
    val results: List<Item> = emptyList(),
    val recentQueries: List<String> = emptyList(),
)
