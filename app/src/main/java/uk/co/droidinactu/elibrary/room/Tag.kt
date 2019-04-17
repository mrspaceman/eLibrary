package uk.co.droidinactu.elibrary.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tags",
    indices = arrayOf(Index(value = arrayOf("id"), unique = true))
)
class Tag {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    @ColumnInfo
    var tag: String = ""

    @ColumnInfo
    var parentTagId: Long? = null

    override fun toString(): String {
        return tag
    }

    companion object {
        const val UNCLASSIFIED = "Unclassified"
        const val CURRENTLY_READING = "Currently Reading"
    }
}