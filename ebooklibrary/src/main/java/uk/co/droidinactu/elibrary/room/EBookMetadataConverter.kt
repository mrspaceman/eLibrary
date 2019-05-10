package uk.co.droidinactu.elibrary.room

import androidx.room.TypeConverter
import com.google.gson.Gson
import uk.co.droidinactu.elibrary.MyDebug

class EBookMetadataConverter {
    @TypeConverter
    fun metadataToJson(value: HashMap<String, String>?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun jsonToMetadata(value: String): HashMap<String, String> {
        return try {
            //  var obj=Gson().fromJson(value, Object::class.java)
            val objs = Gson().fromJson(value, Map::class.java)
            val metadata = HashMap<String, String>()
//            for (s in objs) {
//                metadata.put(FileType.valueOf(s))
//            }
            metadata
        } catch (e: Exception) {
            MyDebug.LOG.error("Exception converting metadata from json [" + value + "] " + e.message)
            HashMap()
        }
    }
}
