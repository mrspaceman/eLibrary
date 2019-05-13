package uk.co.droidinactu.ebooklib.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import uk.co.droidinactu.ebooklib.library.LibraryManager

@Database(
    entities = [EBook::class, Author::class, Library::class, EBookAuthorLink::class],
    version = 1
)
abstract class EBookRoomDatabase : RoomDatabase() {

    abstract fun ebookDao(): EBookDao
    abstract fun ebookAuthorLinkDao(): EBookAuthorLinkDao
    abstract fun authorDao(): AuthorDao
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
