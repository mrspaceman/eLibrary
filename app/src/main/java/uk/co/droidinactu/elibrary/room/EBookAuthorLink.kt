package uk.co.droidinactu.elibrary.room

import androidx.room.Entity
import androidx.room.ForeignKey

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
    )
)
class EBookAuthorLink(ebookId: EBook, authorId: Author) {

}