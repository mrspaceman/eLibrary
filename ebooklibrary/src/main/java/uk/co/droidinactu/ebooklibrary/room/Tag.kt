package uk.co.droidinactu.ebooklibrary.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4

@Entity(
    tableName = "tags"
)
@Fts4
class Tag : BaseRoomObj() {

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
