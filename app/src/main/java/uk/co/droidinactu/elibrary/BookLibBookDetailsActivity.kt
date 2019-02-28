package uk.co.droidinactu.elibrary

import android.os.Bundle
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import uk.co.droidinactu.elibrary.badgedimageview.BadgedImageView
import uk.co.droidinactu.elibrary.room.EBook

class BookLibBookDetailsActivity : AppCompatActivity() {

    internal var dateAddedToLib: TextView
    internal var dateLibEntryMod: TextView
    private var mCover: BadgedImageView? = null
    private var mTitle: EditText? = null
    private var mFilename: EditText? = null
    private var mAuthor: EditText? = null
    private var tagList: ListView? = null
    private var ebk: EBook? = null
    private var book_full_file_dir_name: String? = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_lib_book_details)

    }

    companion object {
        private val LOG_TAG = BookLibBookDetailsActivity::class.java.simpleName + ":"

    }

}
