package uk.co.droidinactu.elibrary.room

import androidx.room.Dao
import androidx.room.Query

@Dao
interface AuthorDao : BaseDao<Author> {
    @Query("SELECT *,${BaseRoomObj.UNIQUE_ID_ROW_NAME} FROM authors order by lastname,firstname")
    fun getAll(): List<Author>

    @Query("SELECT *,${BaseRoomObj.UNIQUE_ID_ROW_NAME} FROM authors WHERE firstname LIKE :first AND lastname LIKE :last LIMIT 1")
    fun getByName(first: String, last: String): Author

    @Query("delete FROM authors")
    fun clear()

}
