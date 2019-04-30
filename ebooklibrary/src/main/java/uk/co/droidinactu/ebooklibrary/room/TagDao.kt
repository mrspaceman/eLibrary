package uk.co.droidinactu.ebooklibrary.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface TagDao {
    @Query("SELECT * FROM tags order by tag")
    fun getAll(): List<Tag>

    @Query(
        "with recursive tc( i ) " +
                "  as ( select id from tags where id = (:tagid)" +
                "        union select id from tags, tc" +
                "               where tags.parentTagId  = tc.i" +
                "     )" +
                "  select * from tags where id in tc order by tag;"
    )
    fun getAllByParentId(tagid: Int): List<Tag>

    @Query("SELECT * FROM tags WHERE tag = (:tagstr) LIMIT 1")
    fun getTag(tagstr: String): Tag

    @Insert
    fun insert(obj: Tag): Long

    /**
     * Update parentTagId by tag id
     */
    @Query("UPDATE tags SET parentTagId = :parentId WHERE id =:objId")
    fun update(objId: Int, parentId: Int)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(obj: Tag)

    @Insert
    fun insertAll(vararg objs: Tag)

    @Delete
    fun delete(obj: Tag)

    @Query("delete FROM tags")
    fun clear()

}
