package uk.co.droidinactu.ebooklibrary.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface EBookTagLinkDao {
    @Query("SELECT * FROM ebooktaglink")
    fun getAll(): List<EBookTagLink>

    @Query("SELECT * FROM ebooktaglink WHERE ebookId=:bookId and tagId=:tagId")
    fun getBookTagLink(bookId: Long, tagId: Long): EBookTagLink

    @Query(
        "SELECT * FROM tags INNER JOIN ebooktaglink ON tags.id = ebooktaglink.ebookId WHERE ebooktaglink.ebookId =:ebookId"
    )
    fun getTagsForBook(ebookId: Long): MutableList<Tag>

    @Insert
    fun insert(obj: EBookTagLink)

    @Insert
    fun insertAll(vararg objs: EBookTagLink)

    @Delete
    fun delete(obj: EBookTagLink)

    @Query("delete FROM ebooktaglink")
    fun clear()

}
