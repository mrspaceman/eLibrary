package uk.co.droidinactu.elibrary.room

import android.graphics.*
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import org.jetbrains.annotations.NotNull
import java.io.ByteArrayOutputStream
import java.util.*

/**
 * Created by aspela on 31/08/16.
 */
@Entity(
    tableName = "ebooks"
)
class EBook() : BaseRoomObj() {

    var filetypes = mutableSetOf<FileType>()

    @Ignore
    var authors = mutableListOf<Author>()

    @Ignore
    var tags = mutableListOf<Tag>()

    @ColumnInfo
    var inLibraryRowId: Int = -1

    @ColumnInfo
    @NotNull
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
        get() = if (coverImage != null && coverImage!!.isNotEmpty()) {
            BitmapFactory.decodeByteArray(coverImage, 0, coverImage!!.size)
        } else null


    /**
     * Copy constructor.
     */
    constructor(rhs: EBook) : this() {
        inLibraryRowId = rhs.inLibraryRowId
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

    fun addTag(pTag: Tag) {
        if (!tags.contains(pTag)) {
            this.tags.add(pTag)
        }
    }

    fun removeArrayDuplicates(duplicates: Array<FileType>): Array<FileType> {
        return Arrays.asList(*duplicates).toSet().toTypedArray()
    }

    fun addFileType(pFiletype: FileType) {
        if (!filetypes.contains(pFiletype)) {
            filetypes.add(pFiletype)
        }
    }

    fun addFileTypes(pFiletype: MutableSet<FileType>) {
        filetypes.addAll(pFiletype)
    }

    fun addFileType(pFiletype: String) {
        filetypes.add(FileType.valueOf(pFiletype))
    }

    fun addAuthors(pAuthors: List<nl.siegmann.epublib.domain.Author>) {
        for (anAuth in pAuthors) {
            val a = Author()
            a.firstname = anAuth.firstname
            a.lastname = anAuth.lastname
            addAuthor(a)
        }
    }

    private fun addAuthor(author: Author) {
        authors.add(author)
    }

    companion object {
        const val COLUMN_NAME_FIRSTNAME = "firstname"
        const val COLUMN_NAME_LASTNAME = "lastname"
        const val COLUMN_NAME_WEBSITE = "website"
        const val COLUMN_BOOK_TITLE = "book_title"
        const val COLUMN_FULL_FILE_NAME = "full_file_dir_name"
    }
}
