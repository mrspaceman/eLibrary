package uk.co.droidinactu.elibrary.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface EBookTagLinkDao :BaseDao<EBookTagLink>{
    @Query("SELECT *,${BaseRoomObj.UNIQUE_ID_ROW_NAME} FROM ebooktaglink")
    fun getAll(): List<EBookTagLink>

    @Query("SELECT *,${BaseRoomObj.UNIQUE_ID_ROW_NAME} FROM ebooktaglink WHERE ebookId=:bookId and tagId=:tagId")
    fun getBookTagLink(bookId: Int, tagId: Int): EBookTagLink

    @Query(
        "SELECT *,tags.${BaseRoomObj.UNIQUE_ID_ROW_NAME} FROM tags INNER JOIN ebooktaglink ON tags.${BaseRoomObj.UNIQUE_ID_ROW_NAME} = ebooktaglink.ebookId WHERE ebooktaglink.ebookId =:ebookId"
    )
    fun getTagsForBook(ebookId: Int): MutableList<Tag>

    @Query("delete FROM ebooktaglink")
    fun clear()

}
