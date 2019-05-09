package uk.co.droidinactu.elibrary.room

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "tags"
)
class Tag() : BaseRoomObj() {
    constructor(tag: String) : this() {
        this.tag = tag
    }

    @ColumnInfo
    var tag: String = ""

    @ColumnInfo
    var parentTagId: Int? = null

    override fun toString(): String {
        return tag
    }

    companion object {
        const val UNCLASSIFIED = "Unclassified"
        const val CURRENTLY_READING = "Currently Reading"
    }
}
