package uk.co.droidinactu.ebooklibrary.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import org.jetbrains.annotations.NotNull

@Entity(
    tableName = "authors"
)
class Author : BaseRoomObj() {

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
