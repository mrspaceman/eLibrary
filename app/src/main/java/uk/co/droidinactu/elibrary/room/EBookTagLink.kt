package uk.co.droidinactu.elibrary.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "ebooktaglink",
    primaryKeys = arrayOf("ebookId", "tagId"),
    foreignKeys = arrayOf(
        ForeignKey(
            entity = EBook::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("ebookId")
        ),
        ForeignKey(
            entity = Author::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("tagId")
        )
    ),
    indices = arrayOf(Index(value = ["ebookId", "tagId"]))
)
class EBookTagLink() {

    @ColumnInfo
    var ebookId: Long = 0

    @ColumnInfo
    var tagId: Long = 0

}