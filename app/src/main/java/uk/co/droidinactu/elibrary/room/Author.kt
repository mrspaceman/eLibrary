package uk.co.droidinactu.elibrary.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import org.jetbrains.annotations.NotNull

@Entity(tableName = "authors", primaryKeys = arrayOf("firstname", "lastname"))
class Author(firstname: String, lastname: String) {

    @ColumnInfo
    var id: Int = 0

    @ColumnInfo
    @NotNull
    var firstname: String? = null

    @ColumnInfo
    @NotNull
    var lastname: String? = null

    @ColumnInfo
    var website: String? = null

    @ColumnInfo
    var facebookId: String? = null

    @ColumnInfo
    var twitterId: String? = null

}
