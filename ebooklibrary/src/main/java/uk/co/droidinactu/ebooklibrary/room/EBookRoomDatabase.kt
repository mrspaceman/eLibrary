package uk.co.droidinactu.ebooklibrary.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import uk.co.droidinactu.ebooklibrary.library.LibraryManager

@Database(
    entities = [EBook::class, Author::class, Tag::class, Library::class, EBookAuthorLink::class, EBookTagLink::class],
    version = 1
)
@TypeConverters(FiletypeConverter::class)
abstract class EBookRoomDatabase : RoomDatabase() {

    abstract fun ebookDao(): EBookDao
    abstract fun ebookAuthorLinkDao(): EBookAuthorLinkDao
    abstract fun ebookTagLinkDao(): EBookTagLinkDao
    abstract fun authorDao(): AuthorDao
    abstract fun tagDao(): TagDao
    abstract fun libraryDao(): LibraryDao


    companion object {
        private var INSTANCE: EBookRoomDatabase? = null

        fun getInstance(context: Context): EBookRoomDatabase? {
            if (INSTANCE == null) {
                synchronized(EBookRoomDatabase::class) {
                    INSTANCE = Room.databaseBuilder(
                        context,
                        EBookRoomDatabase::class.java, LibraryManager.DB_NAME
                    )
                        .build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
