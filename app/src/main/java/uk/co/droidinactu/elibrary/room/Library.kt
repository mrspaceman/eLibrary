package uk.co.droidinactu.elibrary.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull

/**
 * Created by aspela on 31/08/16.
 */
@Entity(tableName = "libraries")
class Library {

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    @ColumnInfo
    @NotNull
    var libraryTitle = ""

    @ColumnInfo
    @NotNull
    var libraryRootDir = ""

    @ColumnInfo
    var includeSubdirs = true

}
