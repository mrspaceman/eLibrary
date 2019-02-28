package uk.co.droidinactu.elibrary.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "author", primaryKeys = arrayOf("firstname", "lastname", "website"))
class Author(firstname: String,lastname: String) {

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    @ColumnInfo
    var firstname: String? = null

    @ColumnInfo
    var lastname: String? = null

    @ColumnInfo
    var website: String? = null

    @ColumnInfo
    var facebookId: String? = null

    @ColumnInfo
    var twitterId: String? = null

}
