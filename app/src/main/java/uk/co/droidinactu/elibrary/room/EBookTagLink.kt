package uk.co.droidinactu.elibrary.room

import androidx.room.Entity
import androidx.room.ForeignKey

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
    )
)
class EBookTagLink(ebookId: EBook, tagId: Tag) {
    constructor() {}
}