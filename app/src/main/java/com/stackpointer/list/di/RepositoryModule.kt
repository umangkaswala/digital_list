package com.stackpointer.list.di

import com.stackpointer.list.data.prefs.SearchHistoryRepositoryImpl
import com.stackpointer.list.data.prefs.SettingsRepositoryImpl
import com.stackpointer.list.data.repository.CollectionRepositoryImpl
import com.stackpointer.list.data.repository.ItemRepositoryImpl
import com.stackpointer.list.data.repository.TemplateRepositoryImpl
import com.stackpointer.list.domain.repository.CollectionRepository
import com.stackpointer.list.domain.repository.ItemRepository
import com.stackpointer.list.domain.repository.SearchHistoryRepository
import com.stackpointer.list.domain.repository.SettingsRepository
import com.stackpointer.list.domain.repository.TemplateRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindItemRepository(impl: ItemRepositoryImpl): ItemRepository

    @Binds
    abstract fun bindCollectionRepository(impl: CollectionRepositoryImpl): CollectionRepository

    @Binds
    abstract fun bindTemplateRepository(impl: TemplateRepositoryImpl): TemplateRepository

    @Binds
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    abstract fun bindSearchHistoryRepository(impl: SearchHistoryRepositoryImpl): SearchHistoryRepository
}
