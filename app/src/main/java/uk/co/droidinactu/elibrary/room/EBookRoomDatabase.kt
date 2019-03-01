package uk.co.droidinactu.elibrary.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = arrayOf(EBook::class, Author::class, BookTag::class), version = 1)
abstract class EBookRoomDatabase : RoomDatabase() {
    abstract fun ebookDao(): EBookDao
    abstract fun authorDao(): AuthorDao
    abstract fun tagDao(): TagDao
    abstract fun libraryDao(): LibraryDao
}