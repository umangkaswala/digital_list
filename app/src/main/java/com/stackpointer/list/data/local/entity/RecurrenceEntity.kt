package com.stackpointer.list.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.stackpointer.list.domain.model.RecurrenceEndType
import com.stackpointer.list.domain.model.RecurrenceFreq
import java.time.DayOfWeek
import java.time.Instant

@Entity(tableName = "recurrences")
data class RecurrenceEntity(
    @PrimaryKey val id: String,
    val freq: RecurrenceFreq,
    val interval: Int,
    /** Bitmask, one bit per [DayOfWeek.getValue] (1=Monday .. 7=Sunday). WEEKLY only. */
    val weekdaysMask: Int,
    val monthDay: Int?,
    val endType: RecurrenceEndType,
    val endDate: Instant?,
    val endCount: Int?,
)
