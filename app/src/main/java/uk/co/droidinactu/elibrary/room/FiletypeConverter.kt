package uk.co.droidinactu.elibrary.room

import androidx.room.TypeConverter
import com.google.gson.Gson

class FiletypeConverter {
    @TypeConverter
    fun listToJson(value: MutableList<FileType>?): String {
        val jsonStr = Gson().toJson(value)
        return jsonStr
    }

    @TypeConverter
    fun jsonToList(value: String): MutableList<FileType> {
        return try {
            var objs = Gson().fromJson(value, Array<String>::class.java)
            //   objs = (objs as MutableList<String>).toTypedArray()
            val fileTypes = mutableListOf<FileType>()
            for (s in objs) {
                fileTypes.add(FileType.valueOf(s))
            }
            fileTypes
        } catch (e: ClassCastException) {
            mutableListOf<FileType>()
        }
    }
}
