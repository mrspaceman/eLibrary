package uk.co.droidinactu.elibrary.room

import androidx.room.TypeConverter
import com.google.gson.Gson

class FiletypeConverter {
    @TypeConverter
    fun listToJson(value: List<FileType>?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun jsonToList(value: String): List<FileType>? {
        val objects = Gson().fromJson(value, Array<FileType>::class.java) as Array<FileType>
        return objects.toList()
    }

}