package com.stackpointer.list.di

import android.content.Context
import androidx.room.Room
import com.stackpointer.list.data.local.DigitalListDatabase
import com.stackpointer.list.data.local.dao.CollectionDao
import com.stackpointer.list.data.local.dao.ItemDao
import com.stackpointer.list.data.local.dao.RecurrenceDao
import com.stackpointer.list.data.local.dao.TemplateDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DigitalListDatabase =
        Room.databaseBuilder(context, DigitalListDatabase::class.java, "digital-list.db").build()

    @Provides
    fun provideItemDao(database: DigitalListDatabase): ItemDao = database.itemDao()

    @Provides
    fun provideCollectionDao(database: DigitalListDatabase): CollectionDao = database.collectionDao()

    @Provides
    fun provideTemplateDao(database: DigitalListDatabase): TemplateDao = database.templateDao()

    @Provides
    fun provideRecurrenceDao(database: DigitalListDatabase): RecurrenceDao = database.recurrenceDao()
}
