package uk.co.droidinactu.elibrary

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.joda.time.DateTime
import uk.co.droidinactu.ebooklib.room.EBook
import uk.co.droidinactu.ebooklib.room.FileType
import uk.co.droidinactu.elibrary.badgedimageview.BadgedImageView

class BookLibBookDetailsActivity : AppCompatActivity() {

    private lateinit var mFilepath: TextView
    private lateinit var dateAddedToLib: TextView
    private lateinit var dateLibEntryMod: TextView
    private lateinit var mCover: BadgedImageView
    private lateinit var mTitle: EditText
    private lateinit var mFilename: EditText
    private lateinit var mAuthor: EditText
    private lateinit var mTagList: ListView
    private lateinit var bookFullFileDirName: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_lib_book_details)

        val b = intent.extras
        if (b != null) {
            bookFullFileDirName = b.getString("book_full_file_dir_name")
        }

        mCover = findViewById(R.id.book_details_cover)
        mTitle = findViewById(R.id.book_details_title)
        mFilename = findViewById(R.id.book_details_filename)
        mAuthor = findViewById(R.id.book_details_author)
        mTagList = findViewById(R.id.book_details_tags)
        mFilepath = findViewById(R.id.book_details_filepath)

        dateAddedToLib = findViewById(R.id.book_details_added_to_lib)
        dateLibEntryMod = findViewById(R.id.book_details_lib_entry_modified)

        mTitle.isEnabled = false
        mAuthor.isEnabled = false

        updateBookDetails()
    }

    private fun updateBookDetails() {
        val libMgr = BookLibApplication.instance.getLibManager()
        doAsync {
            val ebk = libMgr.getBook(bookFullFileDirName)
            uiThread {
                updateBookDetails(ebk)
            }
        }
    }

    private fun updateBookDetails(ebk: EBook) {
        var cvrBmp = ebk.coverImageAsBitmap
        if (cvrBmp == null) {
            cvrBmp = BitmapFactory.decodeResource(resources, R.drawable.generic_book_cover)
        }

        var someDate = DateTime(ebk.addedToLibrary)
        dateAddedToLib.text = someDate.toString(BookLibApplication.sdf)

        someDate = DateTime(ebk.lastRefreshed)
        dateLibEntryMod.text = someDate.toString(BookLibApplication.sdf)

        mTitle.setText(ebk.bookTitle)
        doAsync {
            val auths = BookLibApplication.instance.getLibManager().getAuthorsForBook(ebk)
            uiThread {
                if (auths.isNotEmpty()) {
                    mAuthor.setText(auths[0].fullName)
                }

                val tagStrs = mutableListOf<String>()
                for (t in ebk.tags) {
                    tagStrs.add(t)
                }
                val tagListAdaptor = ArrayAdapter(
                    BookLibApplication.instance.applicationContext,
                    android.R.layout.simple_list_item_1,
                    android.R.id.text1,
                    tagStrs
                )
                mTagList.adapter = tagListAdaptor
            }
        }

        mFilepath.setText(ebk.fileDir)
        mFilename.setText(ebk.fileName)
        mCover.setImageBitmap(cvrBmp)
        val ftypes = ebk.filetypes
        if (ftypes.size == 1) {
            mCover.showBadge(true)
            mCover.setBadgeText(ftypes.first().toString())
        } else {
            mCover.showBadge(true)
            mCover.setBadgeText("${FileType.EPUB}/${FileType.PDF}")
        }
    }
}
