package uk.co.droidinactu.ebooklibrary.room

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

abstract class BaseRoomObj {
    companion object {
        const val UNIQUE_ID_ROW_NAME = "rowid"
    }

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = UNIQUE_ID_ROW_NAME)
    var id: Int = 0

    fun getUniqueId(): Int {
        return id
    }

    fun setUniqueId(newId: Int) {
        id = newId
    }
    fun setUniqueId(newId: Long) {
        id = newId as Int
    }
}
