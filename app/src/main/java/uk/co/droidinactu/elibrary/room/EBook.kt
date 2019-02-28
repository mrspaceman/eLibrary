package uk.co.droidinactu.elibrary.room

import android.graphics.*
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.ByteArrayOutputStream
import java.util.*

/**
 * Created by aspela on 31/08/16.
 */
@Entity(tableName = "ebooks")
class EBook {
    var bookTags: MutableList<BookTag> = ArrayList()
    var filetypes: MutableList<FileType> = ArrayList()

    var authors: List<Author> = ArrayList()
    var tags: List<BookTag> = ArrayList()

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    @ColumnInfo
     var inLibrary = ""

    @ColumnInfo
     var fullFileDirName = ""

    @ColumnInfo
     var fileDir = ""

    @ColumnInfo
     var fileName = ""

    @ColumnInfo
    var rating = 0

    @ColumnInfo
    var addedToLibrary: Long = 0

    @ColumnInfo
     var lastModified: Long = 0

    @ColumnInfo
    var lastRefreshed: Long = 0

    @ColumnInfo
    var lastOpened: Long = 0

    @ColumnInfo
    var publicationDate: Long = 0

    @ColumnInfo
    var bookTitle = ""

    @ColumnInfo
    var bookSummary = ""

    @ColumnInfo
    var bookIsbn = ""

    @ColumnInfo
    var bookSeries = ""

    @ColumnInfo
    var bookSeriesIdx = -1

    @ColumnInfo
     var coverImage: ByteArray? = null


    val coverImageAsBitmap: Bitmap?
        get() = if (coverImage != null && coverImage!!.size > 0) {
            BitmapFactory.decodeByteArray(coverImage, 0, coverImage!!.size)
        } else null

    val authorString: String
        get() {
            var authorString = ""
            for (a in authors) {
                authorString += " " + a.firstname + " " + a.lastname + " "
                authorString.trim { it <= ' ' }
            }
            return authorString
        }


    /**
     * Copy constructor.
     */
    constructor(rhs: EBook) {
        inLibrary = rhs.inLibrary
        fullFileDirName = rhs.fullFileDirName
        fileDir = rhs.fileDir
        fileName = rhs.fileName
        rating = rhs.rating
        coverImage = rhs.coverImage
    }

    fun setCoverImageFromBitmap(coverImage: Bitmap) {
        val coverWidthMax = 600
        val coverHeightMax = 900
        val stream = ByteArrayOutputStream()
        if (coverImage.width > coverWidthMax || coverImage.height > coverHeightMax) {
            val scaledCvrImg = resizeBitmapFitXY(coverWidthMax, coverHeightMax, coverImage)
            scaledCvrImg.compress(Bitmap.CompressFormat.PNG, 90, stream)
        } else {
            coverImage.compress(Bitmap.CompressFormat.PNG, 90, stream)
        }
        this.coverImage = stream.toByteArray()
    }

    private fun resizeBitmapFitXY(width: Int, height: Int, bitmap: Bitmap): Bitmap {
        val background = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val originalWidth = bitmap.width.toFloat()
        val originalHeight = bitmap.height.toFloat()
        val canvas = Canvas(background)
        val scale: Float
        var xTranslation = 0.0f
        var yTranslation = 0.0f
        if (originalWidth > originalHeight) {
            scale = height / originalHeight
            xTranslation = (width - originalWidth * scale) / 2.0f
        } else {
            scale = width / originalWidth
            yTranslation = (height - originalHeight * scale) / 2.0f
        }
        val transformation = Matrix()
        transformation.postTranslate(xTranslation, yTranslation)
        transformation.preScale(scale, scale)
        val paint = Paint()
        paint.isFilterBitmap = true
        canvas.drawBitmap(bitmap, transformation, paint)
        return background
    }

    fun addTag(pTag: BookTag) {
        bookTags.add(pTag)
    }

    fun addTag(pTag: String) {
        bookTags.add(BookTag(pTag))
    }

    fun addFileType(pFiletype: FileType) {
        filetypes.add(pFiletype)
    }

    fun addAuthors(pAuthors: List<nl.siegmann.epublib.domain.Author>) {}

    fun addAuthor(pAuthor: Author) {}

    companion object {
        private val LOG_TAG = EBook::class.java.simpleName + ":"
        val COLUMN_NAME_FIRSTNAME = "firstname"
        val COLUMN_NAME_LASTNAME = "lastname"
        val COLUMN_NAME_WEBSITE = "website"
        val COLUMN_BOOK_TITLE = "book_title"
        val COLUMN_FULL_FILE_NAME = "full_file_dir_name"
    }
}
