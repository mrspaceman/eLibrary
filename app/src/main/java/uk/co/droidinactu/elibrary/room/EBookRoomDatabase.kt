package uk.co.droidinactu.elibrary.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = arrayOf(EBook::class, Author::class, Tag::class, Library::class), version = 1)
@TypeConverters(FiletypeConverter::class)
abstract class EBookRoomDatabase : RoomDatabase() {
    abstract fun ebookDao(): EBookDao
    abstract fun authorDao(): AuthorDao
    abstract fun tagDao(): TagDao
    abstract fun bookAuthorLinkDao(): EBookAuthorLinkDao
    abstract fun bookTagLinkDao(): EBookTagLinkDao
    abstract fun libraryDao(): LibraryDao
}