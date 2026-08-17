package com.stackpointer.list.ui.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stackpointer.list.domain.model.Item
import com.stackpointer.list.domain.repository.CollectionRepository
import com.stackpointer.list.domain.repository.ItemRepository
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
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    collectionRepository: CollectionRepository,
    private val bulkActions: BulkSelectionActions,
) : ViewModel() {

    private val _events = MutableSharedFlow<UndoEvent>()
    val events: SharedFlow<UndoEvent> = _events

    private val selectedIds = MutableStateFlow<Set<String>>(emptySet())

    val uiState: StateFlow<TasksUiState> = combine(
        itemRepository.observeAll(),
        selectedIds,
        collectionRepository.observeAll(),
    ) { allItems, selected, collections ->
        val tasks = allItems.filter { !it.isNote && it.deletedAt == null }
        val incomplete = tasks.filter { !it.isCompleted }
        TasksUiState(
            isLoading = false,
            groups = group(incomplete, Instant.now()),
            completedCount = tasks.count { it.isCompleted },
            selectedIds = selected,
            collections = collections,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TasksUiState())

    private fun group(items: List<Item>, now: Instant): List<TaskGroup> {
        val zone = ZoneId.systemDefault()
        val today = now.atZone(zone).toLocalDate()
        val buckets = items.groupBy { item ->
            val due = item.dueAt?.atZone(zone)?.toLocalDate()
            when {
                due == null -> TaskWhenGroup.NO_DATE
                due.isBefore(today) -> TaskWhenGroup.OVERDUE
                due.isEqual(today) -> TaskWhenGroup.TODAY
                due.isEqual(today.plusDays(1)) -> TaskWhenGroup.TOMORROW
                else -> TaskWhenGroup.LATER
            }
        }
        return TaskWhenGroup.entries.mapNotNull { label ->
            buckets[label]?.takeIf { it.isNotEmpty() }?.let { TaskGroup(label, it) }
        }
    }

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
