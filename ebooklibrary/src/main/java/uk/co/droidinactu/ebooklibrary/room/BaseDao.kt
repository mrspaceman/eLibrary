package uk.co.droidinactu.ebooklibrary.room

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

interface BaseDao<T> {
    @Insert
    fun insert(vararg obj: T): List<Long>

    @Delete
    fun delete(obj: T): Int

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(obj: T): Int

}