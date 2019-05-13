package uk.co.droidinactu.ebooklib.room

import androidx.room.TypeConverter
import com.google.gson.Gson
import uk.co.droidinactu.ebooklib.MyDebug

class EBookMetadataConverter {
    @TypeConverter
    fun metadataToJson(value: HashMap<String, String>?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun jsonToMetadata(value: String): HashMap<String, String> {
        return try {
            val objs = Gson().fromJson(value, Map::class.java)
            val metadata = HashMap<String, String>()
            for (s in objs.keys) {
                metadata.put(s.toString(), objs.get(s).toString())
            }
            metadata
        } catch (e: Exception) {
            MyDebug.LOG.error("Exception converting metadata from json [" + value + "] " + e.message)
            HashMap()
        }
    }
}
