package uk.co.droidinactu.ebooklib.room

import androidx.room.Dao
import androidx.room.Query

@Dao
interface EBookAuthorLinkDao : BaseDao<EBookAuthorLink> {
    @Query("SELECT * FROM ebookauthorlink")
    fun getAll(): List<EBookAuthorLink>

    @Query("SELECT *,ebooks.${BaseRoomObj.UNIQUE_ID_ROW_NAME} FROM ebooks INNER JOIN ebookauthorlink ON ebooks.${BaseRoomObj.UNIQUE_ID_ROW_NAME} = ebookauthorlink.ebookId WHERE ebookauthorlink.authorId=:authorId")
    fun getBooksForAuthor(authorId: Int): MutableList<EBookAuthorLink>

    @Query(
        "SELECT *,authors.${BaseRoomObj.UNIQUE_ID_ROW_NAME} FROM authors INNER JOIN ebookauthorlink ON authors.${BaseRoomObj.UNIQUE_ID_ROW_NAME} = ebookauthorlink.authorId WHERE ebookauthorlink.ebookId=:ebookId"
    )
    fun getAuthorsForBook(ebookId: Int): MutableList<Author>


    @Query("SELECT *,${BaseRoomObj.UNIQUE_ID_ROW_NAME} FROM ebookauthorlink WHERE ebookId=:bookId and authorId=:authorId")
    fun getBookAuthorLink(bookId: Int, authorId: Int): EBookAuthorLink

    @Query("delete FROM ebookauthorlink")
    fun clear()

}
