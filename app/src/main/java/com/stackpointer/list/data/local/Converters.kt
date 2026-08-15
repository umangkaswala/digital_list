package com.stackpointer.list.data.local

import androidx.room.TypeConverter
import com.stackpointer.list.domain.model.AlertType
import com.stackpointer.list.domain.model.PlaceTrigger
import com.stackpointer.list.domain.model.PlaceWindow
import com.stackpointer.list.domain.model.RecurrenceEndType
import com.stackpointer.list.domain.model.RecurrenceFreq
import com.stackpointer.list.domain.model.TriggerType
import java.time.Instant

class Converters {

    @TypeConverter
    fun fromInstant(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    fun toInstant(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)

    @TypeConverter
    fun fromTriggerType(value: TriggerType): String = value.name

    @TypeConverter
    fun toTriggerType(value: String): TriggerType = TriggerType.valueOf(value)

    @TypeConverter
    fun fromAlertType(value: AlertType): String = value.name

    @TypeConverter
    fun toAlertType(value: String): AlertType = AlertType.valueOf(value)

    @TypeConverter
    fun fromRecurrenceFreq(value: RecurrenceFreq): String = value.name

    @TypeConverter
    fun toRecurrenceFreq(value: String): RecurrenceFreq = RecurrenceFreq.valueOf(value)

    @TypeConverter
    fun fromRecurrenceFreqOrNull(value: RecurrenceFreq?): String? = value?.name

    @TypeConverter
    fun toRecurrenceFreqOrNull(value: String?): RecurrenceFreq? = value?.let(RecurrenceFreq::valueOf)

    @TypeConverter
    fun fromRecurrenceEndType(value: RecurrenceEndType): String = value.name

    @TypeConverter
    fun toRecurrenceEndType(value: String): RecurrenceEndType = RecurrenceEndType.valueOf(value)

    @TypeConverter
    fun fromPlaceTrigger(value: PlaceTrigger?): String? = value?.name

    @TypeConverter
    fun toPlaceTrigger(value: String?): PlaceTrigger? = value?.let(PlaceTrigger::valueOf)

    @TypeConverter
    fun fromPlaceWindow(value: PlaceWindow?): String? = value?.name

    @TypeConverter
    fun toPlaceWindow(value: String?): PlaceWindow? = value?.let(PlaceWindow::valueOf)
}
