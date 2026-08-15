package com.stackpointer.list.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stackpointer.list.domain.model.Item
import com.stackpointer.list.domain.model.SavedView
import com.stackpointer.list.domain.repository.ItemRepository
import com.stackpointer.list.domain.usecase.BucketItems
import com.stackpointer.list.ui.screens.common.UndoEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
) : ViewModel() {

    private val _events = MutableSharedFlow<UndoEvent>()
    val events: SharedFlow<UndoEvent> = _events

    val uiState: StateFlow<HomeUiState> = combine(
        listOf(
            itemRepository.observeSavedView(SavedView.TODAY),
            itemRepository.observeSavedView(SavedView.SCHEDULED),
            itemRepository.observeSavedView(SavedView.STARRED),
            itemRepository.observeSavedView(SavedView.PLACE),
            itemRepository.observeSavedView(SavedView.NO_ALERT),
            itemRepository.observeSavedView(SavedView.COMPLETED),
        ),
    ) { results: Array<List<Item>> ->
        val (today, scheduled, starred, place, noAlert, completed) = results
        HomeUiState(
            isLoading = false,
            todayDone = today.count { it.isCompleted },
            todayTotal = today.size,
            scheduledCount = scheduled.size,
            starredCount = starred.size,
            placeCount = place.size,
            noAlertCount = noAlert.size,
            completedCount = completed.size,
            buckets = BucketItems.bucket(SavedView.TODAY, today, Instant.now()),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun completeItem(id: String) {
        viewModelScope.launch {
            val token = itemRepository.complete(id)
            _events.emit(UndoEvent("Task completed", token))
        }
    }

    fun undo(event: UndoEvent) {
        viewModelScope.launch { itemRepository.undo(event.token) }
    }
}

private operator fun <T> Array<T>.component6(): T = this[5]
