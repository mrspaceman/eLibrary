package uk.co.droidinactu.elibrary.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "ebookauthorlink",
    primaryKeys = arrayOf("ebookId", "authorId"),
    foreignKeys = arrayOf(
        ForeignKey(
            entity = EBook::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("ebookId")
        ),
        ForeignKey(
            entity = Author::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("authorId")
        )
    ),
    indices = arrayOf(Index(value = ["ebookId", "authorId"]))
)
class EBookAuthorLink(ebookId: Int, authorId: Int) {

    @ColumnInfo
    var ebookId: Int = 0

    @ColumnInfo
    var authorId: Int = 0

}