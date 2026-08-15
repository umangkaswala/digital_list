package com.stackpointer.list.domain.repository

import com.stackpointer.list.domain.model.Template
import kotlinx.coroutines.flow.Flow

interface TemplateRepository {
    fun observeAll(): Flow<List<Template>>

    /** Seeds the six starter templates from screen 28 — a no-op after the first run. */
    suspend fun seedIfEmpty()
}
