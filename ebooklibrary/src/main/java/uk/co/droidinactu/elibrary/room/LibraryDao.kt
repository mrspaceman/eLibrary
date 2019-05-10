package uk.co.droidinactu.elibrary.room

import androidx.room.Dao
import androidx.room.Query

@Dao
interface LibraryDao : BaseDao<Library> {
    @Query("SELECT *,${BaseRoomObj.UNIQUE_ID_ROW_NAME} FROM libraries")
    fun getAll(): List<Library>

    @Query("SELECT *,${BaseRoomObj.UNIQUE_ID_ROW_NAME} FROM libraries WHERE libraryTitle = (:libname) LIMIT 1")
    fun getByName(libname: String): Library

    @Query("delete FROM libraries")
    fun clear()
}
