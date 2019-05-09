package uk.co.droidinactu.elibrary.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "ebooktaglink",
    primaryKeys = ["ebookId", "tagId"],
    foreignKeys = arrayOf(
        ForeignKey(
            entity = EBook::class,
            parentColumns = arrayOf(BaseRoomObj.UNIQUE_ID_ROW_NAME),
            childColumns = arrayOf("ebookId")
        ),
        ForeignKey(
            entity = Tag::class,
            parentColumns = arrayOf(BaseRoomObj.UNIQUE_ID_ROW_NAME),
            childColumns = arrayOf("tagId")
        )
    )
)
class EBookTagLink {

    @ColumnInfo
    var ebookId: Int = 0

    @ColumnInfo
    var tagId: Int = 0

}
