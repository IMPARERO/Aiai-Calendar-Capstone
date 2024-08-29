package com.miguel.apps.aiaialpha.data.converter

import androidx.room.TypeConverter
import java.util.*

class Converters {
    @TypeConverter
    fun fromTimeStamp(value: Long?) : Date? {
        return value?.let{ Date(it) }
    }

    @TypeConverter
    fun dateToTimeStamp(date: Date?) : Long? {
        return date?.time
    }
}