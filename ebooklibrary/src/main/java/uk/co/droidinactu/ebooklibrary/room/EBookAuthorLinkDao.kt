package uk.co.droidinactu.elibrary.room

import androidx.room.*

@Dao
interface EBookAuthorLinkDao  :BaseDao<EBookAuthorLink>{
    @Query("SELECT * FROM ebookauthorlink")
    fun getAll(): List<EBookAuthorLink>

    @Query("SELECT *,ebooks.${BaseRoomObj.UNIQUE_ID_ROW_NAME} FROM ebooks INNER JOIN ebookauthorlink ON ebooks.${BaseRoomObj.UNIQUE_ID_ROW_NAME} = ebookauthorlink.ebookId WHERE ebookauthorlink.authorId=:authorId")
    fun getBooksForAuthor(authorId: Int): MutableList<EBookAuthorLink>

    @Query(
        "SELECT *,authors.${BaseRoomObj.UNIQUE_ID_ROW_NAME} FROM authors INNER JOIN ebookauthorlink ON authors.${BaseRoomObj.UNIQUE_ID_ROW_NAME} = ebookauthorlink.authorId WHERE ebookauthorlink.ebookId=:ebookId"
    )
    fun getAuthorsForBook(ebookId: Int): MutableList<Author>

    @Query("delete FROM ebookauthorlink")
    fun clear()

}
