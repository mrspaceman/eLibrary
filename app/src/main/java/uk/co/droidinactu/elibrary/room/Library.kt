package uk.co.droidinactu.elibrary.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by aspela on 31/08/16.
 */
@Entity(tableName = "library")
class Library {

    @PrimaryKey
    var libraryTitle = ""
    var libraryRootDir = ""
    var includeSubdirs = true

}
