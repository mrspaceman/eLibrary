package uk.co.droidinactu.elibrary.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "booktags")
class BookTag(tag: String) {

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    @ColumnInfo
    var tag: String = ""

    @ColumnInfo
    var children: List<BookTag> = ArrayList()

    companion object {
        public val CURRENTLY_READING = "currently_reading"
    }

}
