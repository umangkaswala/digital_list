package com.stackpointer.list.ui.screens.scheduled

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stackpointer.list.domain.model.Item
import com.stackpointer.list.domain.model.SavedView
import com.stackpointer.list.domain.repository.CollectionRepository
import com.stackpointer.list.domain.repository.ItemRepository
import com.stackpointer.list.domain.usecase.BucketItems
import com.stackpointer.list.domain.usecase.BulkSelectionActions
import com.stackpointer.list.ui.screens.capture.SortOrder
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
class ScheduledViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    collectionRepository: CollectionRepository,
    private val bulkActions: BulkSelectionActions,
) : ViewModel() {

    private val _events = MutableSharedFlow<UndoEvent>()
    val events: SharedFlow<UndoEvent> = _events

    private val sortOrder = MutableStateFlow(SortOrder.DUE_DATE)
    private val selectedIds = MutableStateFlow<Set<String>>(emptySet())

    val uiState: StateFlow<ScheduledUiState> = combine(
        itemRepository.observeSavedView(SavedView.SCHEDULED),
        sortOrder,
        selectedIds,
        collectionRepository.observeAll(),
    ) { items, sort, selected, collections ->
        ScheduledUiState(
            isLoading = false,
            sortOrder = sort,
            // Due-date order already falls out of the bucketing itself — bucketing preserves
            // the input list's relative order within each bucket, so sorting first is enough
            // for all four options, not just the three non-default ones.
            buckets = BucketItems.bucket(SavedView.SCHEDULED, items.sortedWith(comparator(sort)), Instant.now()),
            selectedIds = selected,
            collections = collections,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScheduledUiState())

    fun completeItem(id: String) {
        viewModelScope.launch {
            val token = itemRepository.complete(id)
            _events.emit(UndoEvent("Task completed", token))
        }
    }

    fun undo(event: UndoEvent) {
        viewModelScope.launch { itemRepository.undo(event.token) }
    }

    fun setSortOrder(order: SortOrder) {
        sortOrder.value = order
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

    private fun comparator(order: SortOrder): Comparator<Item> = when (order) {
        SortOrder.DUE_DATE -> compareBy { it.dueAt ?: Instant.MAX }
        SortOrder.RECENTLY_EDITED -> compareByDescending { it.updatedAt }
        SortOrder.TITLE_A_TO_Z -> compareBy { it.title.lowercase() }
        SortOrder.MANUAL -> compareBy { it.sortOrder }
    }
}
