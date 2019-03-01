package uk.co.droidinactu.elibrary.room


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AuthorDao {
    @Query("SELECT * FROM authors")
    fun getAll(): List<Author>

    @Query(
        "SELECT * FROM authors WHERE first_name LIKE :first AND " +
                "last_name LIKE :last LIMIT 1"
    )
    fun getByName(first: String, last: String): Author

    @Insert
    fun insert(obj: Author)

    @Insert
    fun insertAll(vararg objs: Author)

    @Delete
    fun delete(obj: Author)
}