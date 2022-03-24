package app.christopher.newsapp.db

import androidx.room.TypeConverter
import app.christopher.newsapp.models.Source

/**
 * This class is responsible for TypeConverting the Source parameter in
 * Articles.kt to a recognizable data type (String), readable by Room.
 */
class Converters {

    @TypeConverter
    fun fromSource(source: Source): String {
        return source.name
    }

    @TypeConverter
    fun toSource(name: String) : Source {
        return Source(name, name)
    }
}