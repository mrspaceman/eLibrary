package uk.co.droidinactu.elibrary.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import org.jetbrains.annotations.NotNull

/**
 * Created by aspela on 31/08/16.
 */
@Entity(tableName = "libraries")
class Library : BaseRoomObj() {

    @ColumnInfo
    @NotNull
    var libraryTitle = ""

    @ColumnInfo
    @NotNull
    var libraryRootDir = ""

    @ColumnInfo
    var includeSubdirs = true

}
