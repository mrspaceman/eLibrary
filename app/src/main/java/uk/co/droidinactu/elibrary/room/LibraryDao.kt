package uk.co.droidinactu.elibrary.room


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LibraryDao {
    @Query("SELECT * FROM libraries")
    fun getAll(): List<Library>

    @Query("SELECT * FROM libraries WHERE libraryTitle = (:libname) LIMIT 1")
    fun getByName(libname: String): Library

    @Insert
    fun insert(obj: Library)

    @Delete
    fun delete(obj: Library)
}