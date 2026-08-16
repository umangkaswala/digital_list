package com.stackpointer.list.ui.screens.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stackpointer.list.domain.model.Collection
import com.stackpointer.list.domain.repository.CollectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CollectionsViewModel @Inject constructor(
    private val collectionRepository: CollectionRepository,
) : ViewModel() {

    val uiState: StateFlow<CollectionsUiState> = collectionRepository.observeAllWithCounts()
        .map { CollectionsUiState(isLoading = false, collections = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CollectionsUiState())

    fun createCollection(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            collectionRepository.save(
                Collection(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    iconKey = "label",
                    colorKey = null,
                    isShared = false,
                    sortOrder = uiState.value.collections.size,
                ),
            )
        }
    }

    fun renameCollection(collection: Collection, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch { collectionRepository.save(collection.copy(name = newName)) }
    }

    fun deleteCollection(id: String) {
        viewModelScope.launch { collectionRepository.delete(id) }
    }
}
