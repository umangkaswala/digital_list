package com.stackpointer.list.ui.screens.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stackpointer.list.domain.model.SavedView
import com.stackpointer.list.domain.repository.CollectionRepository
import com.stackpointer.list.domain.repository.ItemRepository
import com.stackpointer.list.domain.usecase.BucketItems
import com.stackpointer.list.domain.usecase.BulkSelectionActions
import com.stackpointer.list.ui.screens.common.UndoEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    collectionRepository: CollectionRepository,
    private val bulkActions: BulkSelectionActions,
) : ViewModel() {

    private val _events = MutableSharedFlow<UndoEvent>()
    val events: SharedFlow<UndoEvent> = _events

    private val selectedIds = MutableStateFlow<Set<String>>(emptySet())

    val uiState: StateFlow<TodayUiState> = combine(
        itemRepository.observeSavedView(SavedView.TODAY),
        selectedIds,
        collectionRepository.observeAll(),
    ) { items, selected, collections ->
        TodayUiState(
            isLoading = false,
            doneCount = items.count { it.isCompleted },
            totalCount = items.size,
            buckets = BucketItems.bucket(SavedView.TODAY, items, Instant.now()),
            selectedIds = selected,
            collections = collections,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodayUiState())

    fun completeItem(id: String) {
        viewModelScope.launch {
            val token = itemRepository.complete(id)
            _events.emit(UndoEvent("Task completed", token))
        }
    }

    fun undo(event: UndoEvent) {
        viewModelScope.launch { itemRepository.undo(event.token) }
    }

    fun toggleSelected(id: String) {
        selectedIds.update { if (id in it) it - id else it + id }
    }

    fun clearSelection() {
        selectedIds.value = emptySet()
    }

    fun bulkPin() {
        viewModelScope.launch { bulkActions.pin(selectedIds.value); clearSelection() }
    }

    fun bulkAddToCollection(collectionId: String) {
        viewModelScope.launch { bulkActions.addToCollection(selectedIds.value, collectionId); clearSelection() }
    }

    fun bulkArchive() {
        viewModelScope.launch { bulkActions.archive(selectedIds.value); clearSelection() }
    }

    fun bulkDelete() {
        viewModelScope.launch { bulkActions.delete(selectedIds.value); clearSelection() }
    }
}
