package uk.co.droidinactu.elibrary.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface EBookDao {
    @Query("SELECT * FROM ebooks")
    fun getAll(): List<EBook>

    @Query("SELECT * FROM ebooks WHERE inLibraryId IN (:inLibraryId)")
    fun getAllInLibrary(inLibraryId: Int): List<EBook>

    @Query("SELECT * FROM ebooks WHERE bookTitle like '%' || :titleStr  || '%'")
    fun getAllWithTitle(titleStr: String): List<EBook>

    @Insert
    fun insert(obj: EBook)

    @Insert
    fun insertAll(vararg objs: EBook)

    @Delete
    fun delete(obj: EBook)

}