package uk.co.droidinactu.ebooklib.room

import androidx.room.TypeConverter
import com.google.gson.Gson

class TagConverter {
    @TypeConverter
    fun tagsToJson(value: MutableSet<String>?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun jsonToTags(value: String): MutableSet<String> {
        return try {
            val objs = Gson().fromJson(value, Array<String>::class.java)
            val tags = mutableSetOf<String>()
            for (s in objs) {
                tags.add(s.toString())
            }
            tags
        } catch (e: ClassCastException) {
            mutableSetOf()
        }
    }
}
