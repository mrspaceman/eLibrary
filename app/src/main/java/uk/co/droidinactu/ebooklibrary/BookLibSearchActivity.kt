package uk.co.droidinactu.ebooklibrary

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class BookLibSearchActivity : AppCompatActivity() {

    private var txtSearchText: EditText? = null
    private var btnSearch: Button? = null
    private var cbTags: CheckBox? = null
    private var cbTitle: CheckBox? = null
    private var cbDirs: CheckBox? = null
    private var bkListSearch: RecyclerView? = null
    private var bkListSearchAdaptor: BookListItemAdaptor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_lib_search)

        val b = intent.extras
        var value = -1 // or other values
        if (b != null) {
            value = b.getInt("key")
        }

        txtSearchText = findViewById<View>(R.id.book_lib_search_text) as EditText
        btnSearch = findViewById<View>(R.id.book_lib_search_button) as Button
        cbTags = findViewById<View>(R.id.book_lib_search_checkBoxTags) as CheckBox
        cbTitle = findViewById<View>(R.id.book_lib_search_checkBoxTitle) as CheckBox
        cbDirs = findViewById<View>(R.id.book_lib_search_checkBoxDirs) as CheckBox
        bkListSearch = findViewById<View>(R.id.book_lib_search_results) as RecyclerView

        val gridLayoutManager = GridLayoutManager(this, 4)
        bkListSearch?.layoutManager = gridLayoutManager
        bkListSearch?.setHasFixedSize(true)

        cbTags?.isSelected = true
        cbTitle?.isSelected = true
        cbDirs?.isSelected = true

        btnSearch?.setOnClickListener {
            doAsync {
                val bklist = BookLibApplication.instance.getLibManager()
                    .searchBooksMatching(txtSearchText?.text.toString())
                uiThread {
                    bkListSearchAdaptor = BookListItemAdaptor(bklist)
                    bkListSearch?.adapter = bkListSearchAdaptor
                }
            }
        }
    }

}
