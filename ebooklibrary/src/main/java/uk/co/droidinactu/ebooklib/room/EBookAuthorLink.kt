package uk.co.droidinactu.ebooklib.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "ebookauthorlink",
    primaryKeys = arrayOf("ebookId", "authorId"),
    foreignKeys = arrayOf(
        ForeignKey(
            entity = EBook::class,
            parentColumns = arrayOf(BaseRoomObj.UNIQUE_ID_ROW_NAME),
            childColumns = arrayOf("ebookId")
        ),
        ForeignKey(
            entity = Author::class,
            parentColumns = arrayOf(BaseRoomObj.UNIQUE_ID_ROW_NAME),
            childColumns = arrayOf("authorId")
        )
    )
)
class EBookAuthorLink {

    @ColumnInfo
    var ebookId: Int = 0

    @ColumnInfo
    var authorId: Int = 0

}
