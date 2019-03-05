package uk.co.droidinactu.elibrary.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface EBookAuthorLinkDao {
    @Query("SELECT * FROM ebookauthorlink")
    fun getAll(): MutableList<EBookAuthorLink>

//    @Query("SELECT * FROM ebooks INNER JOIN ebookauthorlink ON ebooks.id=ebookauthorlink.ebookId WHERE ebookauthorlink.authorId=:authorId")
//    fun getBooksForAuthor(authorId: Int): MutableList<EBookAuthorLink>
//
//    @Query(
//        "SELECT * FROM authors INNER JOIN ebookauthorlink ON authors.id = ebookauthorlink.authorId WHERE ebookauthorlink.ebookId =:ebookId"
//    )
//    fun getAuthorsForBook(ebookId: Int): MutableList<EBookAuthorLink>

    @Insert
    fun insert(obj: EBookAuthorLink)

    @Insert
    fun insertAll(vararg objs: EBookAuthorLink)

    @Delete
    fun delete(obj: EBookAuthorLink)

}