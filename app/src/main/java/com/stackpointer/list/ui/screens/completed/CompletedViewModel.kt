package com.stackpointer.list.ui.screens.completed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stackpointer.list.domain.model.Item
import com.stackpointer.list.domain.model.SavedView
import com.stackpointer.list.domain.repository.CollectionRepository
import com.stackpointer.list.domain.repository.ItemRepository
import com.stackpointer.list.domain.usecase.BucketItems
import com.stackpointer.list.domain.usecase.BulkSelectionActions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class CompletedViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    collectionRepository: CollectionRepository,
    private val bulkActions: BulkSelectionActions,
) : ViewModel() {

    private val selectedIds = MutableStateFlow<Set<String>>(emptySet())

    val uiState: StateFlow<CompletedUiState> = combine(
        itemRepository.observeSavedView(SavedView.COMPLETED),
        selectedIds,
        collectionRepository.observeAll(),
    ) { items, selected, collections ->
        CompletedUiState(
            isLoading = false,
            buckets = BucketItems.bucket(SavedView.COMPLETED, items, Instant.now()),
            selectedIds = selected,
            collections = collections,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CompletedUiState())

    /** The trailing "undo" icon on each row (screen 08) — moves the item back to its saved
     * view directly, distinct from the complete/undo snackbar flow the other screens use. */
    fun restoreToIncomplete(item: Item) {
        viewModelScope.launch {
            itemRepository.save(item.copy(isCompleted = false, completedAt = null))
        }
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
