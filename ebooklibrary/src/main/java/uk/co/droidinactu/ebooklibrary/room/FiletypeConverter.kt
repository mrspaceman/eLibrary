package uk.co.droidinactu.ebooklibrary.room

import androidx.room.TypeConverter
import com.google.gson.Gson

class FiletypeConverter {
    @TypeConverter
    fun listToJson(value: MutableSet<FileType>?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun jsonToList(value: String): MutableSet<FileType> {
        return try {
            val objs = Gson().fromJson(value, Array<String>::class.java)
            val fileTypes = mutableSetOf<FileType>()
            for (s in objs) {
                fileTypes.add(FileType.valueOf(s))
            }
            fileTypes
        } catch (e: ClassCastException) {
            mutableSetOf()
        }
    }
}
