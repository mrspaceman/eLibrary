package uk.co.droidinactu.elibrary.room


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AuthorDao {
    @Query("SELECT * FROM author")
    fun getAll(): List<Author>

//    @Query("SELECT * FROM ebooks WHERE  IN (:userIds)")
//    fun getAllByTag(tags: IntArray): List<EBook>

//    @Query("SELECT * FROM  WHERE first_name LIKE :first AND " +
//            "last_name LIKE :last LIMIT 1")
//    fun findByName(first: String, last: String): EBook

    @Insert
    fun insert(obj: Author)

    @Insert
    fun insertAll(vararg objs: Author)

    @Delete
    fun delete(obj: Author)
}