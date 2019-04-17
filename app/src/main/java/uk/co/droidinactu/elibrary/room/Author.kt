package uk.co.droidinactu.elibrary.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull

@Entity(
    tableName = "authors",
    indices = arrayOf(Index(value = arrayOf("id"), unique = true))
)
class Author {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

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

    //@Ignore
    val fullName: String
        get() {
            var authorString = ""
            authorString += " $firstname $lastname "
            authorString.trim { it <= ' ' }
            return authorString
        }

}
