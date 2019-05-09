package uk.co.droidinactu.elibrary.room

import androidx.room.Dao
import androidx.room.Query

@Dao
interface EBookDao : BaseDao<EBook> {
    @Query("SELECT *,rowid FROM ebooks")
    fun getAll(): MutableList<EBook>

    @Query("SELECT count(${BaseRoomObj.UNIQUE_ID_ROW_NAME}) FROM ebooks")
    fun getCount(): Int

    @Query("SELECT count(${BaseRoomObj.UNIQUE_ID_ROW_NAME}) FROM ebooks where inLibraryRowId = :libId")
    fun getCount(libId: Int): Int

    @Query("SELECT *,rowid FROM ebooks WHERE inLibraryRowId IN (:inLibraryId)")
    fun getAllInLibrary(inLibraryId: Int): MutableList<EBook>

    @Query("SELECT *,rowid FROM ebooks WHERE fullFileDirName like :filename || '%'")
    fun getBookFromFullFilename(filename: String): EBook

    @Query("SELECT *,rowid FROM ebooks WHERE fileName = :filename")
    fun getBookFromFilename(filename: String): EBook

    @Query("SELECT *,rowid FROM ebooks WHERE bookTitle like '%' || :titleStr  || '%'")
    fun getAllWithTitle(titleStr: String): MutableList<EBook>

    @Query("SELECT *,rowid FROM ebooks WHERE bookTitle = :titleStr  limit 1")
    fun getBookCalled(titleStr: String): EBook

    @Query(
        "select ebks.*,rowid from ebooks ebks where ebks.${BaseRoomObj.UNIQUE_ID_ROW_NAME} in " +
                "(select bktg.ebookId from ebooktaglink bktg where bktg.tagId = :tagId)"
    )
    fun getAllForTag(tagId: Int): MutableList<EBook>

//    @Query(
//        "SELECT ebooks.bookTitle, ebooks.fullFileDirName, authors.firstname, authors.lastname, tags.tag\n" +
//                " FROM ebooks\n" +
//                " join ebookauthorlink ea on ebooks.${BaseRoomObj.UNIQUE_ID_ROW_NAME} = ea.ebookId\n" +
//                " join authors  on ea.authorId = authors.${BaseRoomObj.UNIQUE_ID_ROW_NAME}\n" +
//                " join ebooktaglink et on ebooks.${BaseRoomObj.UNIQUE_ID_ROW_NAME} = et.ebookId\n" +
//                " join tags on et.tagId = tags.${BaseRoomObj.UNIQUE_ID_ROW_NAME}"
//    )
//    fun getBooksWithAuthorsAndTags(): MutableList<EBook>

    @Query("delete FROM ebooks")
    fun clear()

}
