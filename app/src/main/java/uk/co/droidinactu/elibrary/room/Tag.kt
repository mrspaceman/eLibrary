package uk.co.droidinactu.elibrary.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tags")
class Tag(tag: String) {

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    @ColumnInfo
    var tag: String = ""

    @ColumnInfo
    var parentTagId: Int? = null

    companion object {
        val UNCLASSIFIED = "Unclassified"
        val CURRENTLY_READING = "Currently Reading"
    }
}