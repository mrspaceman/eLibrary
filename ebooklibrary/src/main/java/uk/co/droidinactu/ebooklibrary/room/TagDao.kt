package uk.co.droidinactu.ebooklibrary.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TagDao : BaseDao<Tag> {
    @Query("SELECT *,${BaseRoomObj.UNIQUE_ID_ROW_NAME} FROM tags order by tag")
    fun getAll(): List<Tag>

    @Query("SELECT DISTINCT *,${BaseRoomObj.UNIQUE_ID_ROW_NAME} FROM tags order by tag")
    fun getAllUnique(): List<Tag>

    @Query(
        "with recursive tc( i ) " +
                "  as ( select tags.${BaseRoomObj.UNIQUE_ID_ROW_NAME} from tags where tags.${BaseRoomObj.UNIQUE_ID_ROW_NAME} = (:tagid)" +
                "        union select tags.${BaseRoomObj.UNIQUE_ID_ROW_NAME} from tags, tc" +
                "               where tags.parentTagId  = tc.i" +
                "     )" +
                "  select *,${BaseRoomObj.UNIQUE_ID_ROW_NAME} from tags where tags.${BaseRoomObj.UNIQUE_ID_ROW_NAME} in tc order by tag;"
    )
    fun getAllByParentId(tagid: Int): List<Tag>

    @Query("SELECT *,${BaseRoomObj.UNIQUE_ID_ROW_NAME} FROM tags WHERE tag = (:tagstr) LIMIT 1")
    fun getTag(tagstr: String): Tag

    @Insert
    fun insert(obj: Tag): Long

    /**
     * Update parentTagId by tag id
     */
    @Query("UPDATE tags SET parentTagId = :parentId WHERE ${BaseRoomObj.UNIQUE_ID_ROW_NAME} =:objId")
    fun update(objId: Int, parentId: Int)

    @Query("delete FROM tags")
    fun clear()

}
