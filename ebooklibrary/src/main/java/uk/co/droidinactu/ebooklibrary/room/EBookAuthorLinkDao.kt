package uk.co.droidinactu.ebooklibrary.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EBookAuthorLinkDao {
    @Query("SELECT * FROM ebookauthorlink")
    fun getAll(): List<EBookAuthorLink>

    @Query("SELECT * FROM ebooks INNER JOIN ebookauthorlink ON ebooks.id=ebookauthorlink.ebookId WHERE ebookauthorlink.authorId=:authorId")
    fun getBooksForAuthor(authorId: Long): MutableList<EBookAuthorLink>

    @Query(
        "SELECT * FROM authors INNER JOIN ebookauthorlink ON authors.id = ebookauthorlink.authorId WHERE ebookauthorlink.ebookId =:ebookId"
    )
    fun getAuthorsForBook(ebookId: Long): MutableList<Author>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(obj: EBookAuthorLink)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg objs: EBookAuthorLink)

    @Delete
    fun delete(obj: EBookAuthorLink)

    @Query("delete FROM ebookauthorlink")
    fun clear()

}
