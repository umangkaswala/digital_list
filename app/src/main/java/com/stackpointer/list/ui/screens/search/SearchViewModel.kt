package com.stackpointer.list.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stackpointer.list.domain.model.Item
import com.stackpointer.list.domain.repository.ItemRepository
import com.stackpointer.list.domain.repository.SearchHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val scope = MutableStateFlow(SearchScope.EVERYTHING)

    private val rawResults = query.flatMapLatest { q ->
        if (q.isBlank()) flowOf(emptyList()) else itemRepository.search(q)
    }

    val uiState: StateFlow<SearchUiState> = combine(
        query, scope, rawResults, searchHistoryRepository.recentQueries,
    ) { currentQuery, currentScope, results, recent ->
        SearchUiState(
            query = currentQuery,
            scope = currentScope,
            results = results.filter { matchesScope(it, currentScope) },
            recentQueries = recent,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState())

    fun updateQuery(newQuery: String) {
        query.value = newQuery
    }

    fun selectScope(newScope: SearchScope) {
        scope.value = newScope
    }

    /** Commits the current query into recent history — called on submit (IME search action),
     * not on every keystroke. */
    fun commitQuery() {
        val current = query.value
        if (current.isBlank()) return
        viewModelScope.launch { searchHistoryRepository.addQuery(current) }
    }

    fun selectRecentQuery(recentQuery: String) {
        query.value = recentQuery
        commitQuery()
    }

    fun removeRecentQuery(recentQuery: String) {
        viewModelScope.launch { searchHistoryRepository.removeQuery(recentQuery) }
    }

    fun clearQuery() {
        query.value = ""
    }

    private fun matchesScope(item: Item, scope: SearchScope): Boolean = when (scope) {
        SearchScope.EVERYTHING -> true
        SearchScope.NOTES -> item.isNote
        SearchScope.TASKS -> !item.isNote
        SearchScope.ARCHIVE -> item.isArchived
    }
}
