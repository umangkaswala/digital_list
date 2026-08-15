package com.stackpointer.list.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.stackpointer.list.data.local.dao.CollectionDao
import com.stackpointer.list.data.local.dao.ItemDao
import com.stackpointer.list.data.local.dao.RecurrenceDao
import com.stackpointer.list.data.local.dao.TemplateDao
import com.stackpointer.list.data.local.entity.CollectionEntity
import com.stackpointer.list.data.local.entity.ItemCollectionCrossRef
import com.stackpointer.list.data.local.entity.ItemEntity
import com.stackpointer.list.data.local.entity.PlaceEntity
import com.stackpointer.list.data.local.entity.RecurrenceEntity
import com.stackpointer.list.data.local.entity.SubItemEntity
import com.stackpointer.list.data.local.entity.TemplateEntity

@Database(
    entities = [
        ItemEntity::class,
        SubItemEntity::class,
        CollectionEntity::class,
        ItemCollectionCrossRef::class,
        RecurrenceEntity::class,
        TemplateEntity::class,
        PlaceEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class DigitalListDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun collectionDao(): CollectionDao
    abstract fun templateDao(): TemplateDao
    abstract fun recurrenceDao(): RecurrenceDao
}
