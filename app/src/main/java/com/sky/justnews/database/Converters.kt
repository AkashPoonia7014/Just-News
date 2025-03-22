package com.sky.justnews.database

import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.sky.justnews.models.Source

class Converters {

    @TypeConverter
    fun fromSource(source: Source): String? {
        return source.name
    }

    @TypeConverter
    fun toSource(name: String):Source {
        return Source(name, name)
    }
}