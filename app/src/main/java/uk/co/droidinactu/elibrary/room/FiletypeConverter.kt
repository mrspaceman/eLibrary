package uk.co.droidinactu.elibrary.room

import androidx.room.TypeConverter
import com.google.gson.Gson

class FiletypeConverter {
    @TypeConverter
    fun listToJson(value: MutableList<FileType>?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun jsonToList(value: String): MutableList<FileType> {
        try {
            val objects = Gson().fromJson(value, Array<FileType>::class.java) as MutableList<FileType>
            return objects
        } catch (e: ClassCastException) {
            return mutableListOf<FileType>()
        }
    }
}
