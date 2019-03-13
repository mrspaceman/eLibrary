package uk.co.droidinactu.elibrary.room

import androidx.room.*

@Dao
interface EBookDao {
    @Query("SELECT * FROM ebooks")
    fun getAll(): MutableList<EBook>

    @Query("SELECT * FROM ebooks WHERE inLibraryId IN (:inLibraryId)")
    fun getAllInLibrary(inLibraryId: Long): MutableList<EBook>

    @Query("SELECT * FROM ebooks WHERE fullFileDirName like :filename || '%'")
    fun getBookFromFilname(filename: String): EBook

    @Query("SELECT * FROM ebooks WHERE bookTitle like '%' || :titleStr  || '%'")
    fun getAllWithTitle(titleStr: String): MutableList<EBook>

    @Query("SELECT * FROM ebooks WHERE bookTitle like '%' || :titleStr  || '%'")
    fun getAllForTag(titleStr: String): MutableList<EBook>

    @Update
    fun update(obj: EBook)

    @Insert
    fun insert(obj: EBook)

    @Insert
    fun insertAll(vararg objs: EBook)

    @Delete
    fun delete(obj: EBook)

    @Query("delete FROM ebooks")
    fun clear()

}