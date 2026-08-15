package com.stackpointer.list.ui.screens.completed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stackpointer.list.domain.model.Item
import com.stackpointer.list.domain.model.SavedView
import com.stackpointer.list.domain.repository.ItemRepository
import com.stackpointer.list.domain.usecase.BucketItems
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class CompletedViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
) : ViewModel() {

    val uiState: StateFlow<CompletedUiState> = itemRepository.observeSavedView(SavedView.COMPLETED)
        .map { items ->
            CompletedUiState(
                isLoading = false,
                buckets = BucketItems.bucket(SavedView.COMPLETED, items, Instant.now()),
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CompletedUiState())

    /** The trailing "undo" icon on each row (screen 08) — moves the item back to its saved
     * view directly, distinct from the complete/undo snackbar flow the other screens use. */
    fun restoreToIncomplete(item: Item) {
        viewModelScope.launch {
            itemRepository.save(item.copy(isCompleted = false, completedAt = null))
        }
    }
}
