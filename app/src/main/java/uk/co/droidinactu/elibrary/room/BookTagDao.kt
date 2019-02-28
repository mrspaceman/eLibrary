package uk.co.droidinactu.elibrary.room


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface BookTagDao {
    @Query("SELECT * FROM booktag")
    fun getAll(): List<BookTag>

//    @Query("SELECT * FROM ebooks WHERE  IN (:userIds)")
//    fun getAllByTag(tags: IntArray): List<BookTag>

//    @Query("SELECT * FROM  WHERE first_name LIKE :first AND " +
//            "last_name LIKE :last LIMIT 1")
//    fun findByName(first: String, last: String): BookTag

    @Insert
    fun insert(obj: BookTag)

    @Insert
    fun insertAll(vararg objs: BookTag)

    @Delete
    fun delete(obj: BookTag)
}