package com.stackpointer.list.ui.screens.bin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stackpointer.list.domain.model.SavedView
import com.stackpointer.list.domain.repository.ItemRepository
import com.stackpointer.list.domain.usecase.BucketItems
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
class BinViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
) : ViewModel() {

    private val selectedIds = MutableStateFlow<Set<String>>(emptySet())

    val uiState: StateFlow<BinUiState> = combine(
        itemRepository.observeSavedView(SavedView.RECYCLE_BIN),
        selectedIds,
    ) { items, selected ->
        val now = Instant.now()
        BinUiState(
            isLoading = false,
            rows = items.sortedByDescending { it.deletedAt }
                .map { BinRow(it, BucketItems.recycleBinDaysRemaining(it, now)) },
            selectedIds = selected.intersect(items.map { it.id }.toSet()),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BinUiState())

    fun toggleSelected(id: String) {
        selectedIds.update { if (id in it) it - id else it + id }
    }

    fun selectAll() {
        selectedIds.value = uiState.value.rows.map { it.item.id }.toSet()
    }

    fun clearSelection() {
        selectedIds.value = emptySet()
    }

    fun restoreSelected() {
        val ids = selectedIds.value
        viewModelScope.launch {
            ids.forEach { itemRepository.restore(it) }
            clearSelection()
        }
    }

    fun deleteSelectedForever() {
        val ids = selectedIds.value
        viewModelScope.launch {
            ids.forEach { itemRepository.deleteForever(it) }
            clearSelection()
        }
    }
}
