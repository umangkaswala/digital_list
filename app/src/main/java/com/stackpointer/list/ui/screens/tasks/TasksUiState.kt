package com.stackpointer.list.ui.screens.tasks

import com.stackpointer.list.domain.model.Collection
import com.stackpointer.list.domain.model.Item

/** Screen 03's grouping — "when" buckets distinct from the other saved views' PAST/SOON/etc.
 * labels, since Tasks mixes every trigger type (time, place, none) into one list. */
enum class TaskWhenGroup { OVERDUE, TODAY, TOMORROW, LATER, NO_DATE }

data class TaskGroup(val label: TaskWhenGroup, val items: List<Item>)

data class TasksUiState(
    val isLoading: Boolean = true,
    val groups: List<TaskGroup> = emptyList(),
    val completedCount: Int = 0,
    val selectedIds: Set<String> = emptySet(),
    val collections: List<Collection> = emptyList(),
) {
    val isEmpty: Boolean get() = !isLoading && groups.isEmpty()
    val isSelectionMode: Boolean get() = selectedIds.isNotEmpty()
}
