package uk.co.droidinactu.elibrary.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface EBookTagLinkDao {
    @Query("SELECT * FROM ebooktaglink")
    fun getAll(): MutableList<EBookTagLink>

//    @Query("SELECT * FROM ebooks INNER JOIN ebooktaglink ON ebooks.id=ebooktaglink.ebookId WHERE ebooktaglink.tagId=:tagId")
//    fun getBooksForTag(tagId: Int): MutableList<EBook>
//
//    @Query(
//        "SELECT * FROM tags INNER JOIN ebooktaglink ON tags.id = ebooktaglink.tagId WHERE ebooktaglink.ebookId =:ebookId"
//    )
//    fun getTagsForBook(ebookId: Int): MutableList<Tag>

    @Insert
    fun insert(obj: EBookTagLink)

    @Insert
    fun insertAll(vararg objs: EBookTagLink)

    @Delete
    fun delete(obj: EBookTagLink)

}