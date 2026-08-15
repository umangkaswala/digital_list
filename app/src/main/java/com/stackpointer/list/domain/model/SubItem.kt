package com.stackpointer.list.domain.model

/** A checklist row. Drives "1 of 3 done" and the drag-reorderable checklist rows. */
data class SubItem(
    val id: String,
    val itemId: String,
    val text: String,
    val isCompleted: Boolean,
    val sortOrder: Int,
)
