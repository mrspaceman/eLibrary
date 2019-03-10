package uk.co.droidinactu.elibrary.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TagDao {
    @Query("SELECT * FROM tags")
    fun getAll(): List<Tag>

    @Query("SELECT * FROM tags WHERE parentTagId IN (:tagids)")
    fun getAllByParentId(tagids: IntArray): List<Tag>

    @Query("SELECT * FROM tags WHERE tag = (:tagstr) LIMIT 1")
    fun getTag(tagstr: String): Tag

    @Insert
    fun insert(obj: Tag): Long

    @Insert
    fun insertAll(vararg objs: Tag)

    @Delete
    fun delete(obj: Tag)
}