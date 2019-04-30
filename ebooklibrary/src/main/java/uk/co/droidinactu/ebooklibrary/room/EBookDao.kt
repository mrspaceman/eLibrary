package uk.co.droidinactu.ebooklibrary.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface EBookDao {
    @Query("SELECT * FROM ebooks")
    fun getAll(): MutableList<EBook>

    @Query("SELECT count(id) FROM ebooks")
    fun getCount(): Int

    @Query("SELECT count(id) FROM ebooks where inLibraryId = :libId")
    fun getCount(libId: Long): Int

    @Query("SELECT * FROM ebooks WHERE inLibraryId IN (:inLibraryId)")
    fun getAllInLibrary(inLibraryId: Long): MutableList<EBook>

    @Query("SELECT * FROM ebooks WHERE fullFileDirName like :filename || '%'")
    fun getBookFromFullFilename(filename: String): EBook

    @Query("SELECT * FROM ebooks WHERE fileName = :filename")
    fun getBookFromFilename(filename: String): EBook

    @Query("SELECT * FROM ebooks WHERE bookTitle like '%' || :titleStr  || '%'")
    fun getAllWithTitle(titleStr: String): MutableList<EBook>

    @Query("SELECT * FROM ebooks WHERE bookTitle = :titleStr  limit 1")
    fun getBookCalled(titleStr: String): EBook

    @Query(
        "select ebks.* from ebooks ebks where ebks.id in " +
                "(select bktg.ebookId from ebooktaglink bktg where bktg.tagId = :tagId)"
    )
    fun getAllForTag(tagId: Long): MutableList<EBook>

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
