package com.stackpointer.list.ui.screens.bin

import com.stackpointer.list.domain.model.Item

/** One recycle-bin row plus its precomputed "N days left" — see
 * [com.stackpointer.list.domain.usecase.BucketItems.recycleBinDaysRemaining]. */
data class BinRow(val item: Item, val daysLeft: Int)

data class BinUiState(
    val isLoading: Boolean = true,
    val rows: List<BinRow> = emptyList(),
    val selectedIds: Set<String> = emptySet(),
) {
    val isSelectionMode: Boolean get() = selectedIds.isNotEmpty()

    /** Grouped by days-left, most-recently-deleted group first — screen 29's "28 DAYS LEFT" /
     * "14 DAYS LEFT" headers. */
    val grouped: List<Pair<Int, List<BinRow>>>
        get() = rows.groupBy { it.daysLeft }.toList().sortedByDescending { it.first }
}
