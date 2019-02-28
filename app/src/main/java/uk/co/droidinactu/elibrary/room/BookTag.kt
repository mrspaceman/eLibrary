package uk.co.droidinactu.elibrary.room

import androidx.room.Entity
import java.util.*

@Entity(tableName = "booktag", primaryKeys = arrayOf("firstname", "lastname"))
class BookTag(tag: String) {

    var tag: String = ""
    var children: List<BookTag> = ArrayList()

    companion object {
        public val CURRENTLY_READING = "currently_reading"
    }

}
