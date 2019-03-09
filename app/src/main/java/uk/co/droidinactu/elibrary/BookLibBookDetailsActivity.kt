package uk.co.droidinactu.elibrary

import android.os.Bundle
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import uk.co.droidinactu.elibrary.badgedimageview.BadgedImageView
import uk.co.droidinactu.elibrary.room.EBook

class BookLibBookDetailsActivity : AppCompatActivity() {

    internal var dateAddedToLib: TextView? = null
    internal var dateLibEntryMod: TextView? = null
    private var mCover: BadgedImageView? = null
    private var mTitle: EditText? = null
    private var mFilename: EditText? = null
    private var mAuthor: EditText? = null
    private var tagList: ListView? = null
    private var ebk: EBook? = null
    private var bookFullFileDirName: String? = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_lib_book_details)

//        libMgr = LibraryManager()
//        try {
//            libMgr.open()
//        } catch (pE: SQLException) {
//            pE.printStackTrace()
//        }

    }

}
