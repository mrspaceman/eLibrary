package uk.co.droidinactu.elibrary

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.jetbrains.anko.ctx
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import uk.co.droidinactu.ebooklib.files.FileHolder
import uk.co.droidinactu.ebooklib.files.FileUtils
import uk.co.droidinactu.ebooklib.files.MimeTypes
import uk.co.droidinactu.ebooklib.room.EBook
import java.io.File

class BookLibSearchActivity : AppCompatActivity() {

    //#region Open Book Handler
    private val openBookHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            val ebkFilename = (msg.obj as String).split(":").get(1)
            val ebk = BookLibApplication.instance.getLibManager().getBookFromFullFilename(ebkFilename)
            val ftypes = ebk?.filetypes
            if (ftypes != null && ftypes.size > 1) {
                showFileTypePickerDialog(ebk)
            } else {
                openBook(ebk, ftypes?.first().toString().toLowerCase())
            }
        }

        private fun showFileTypePickerDialog(ebk: EBook) {
            val dialog = Dialog(ctx)
            dialog.setContentView(R.layout.filetype_picker_dialog)
            dialog.setTitle("Pick an EBook Type to Open")

            val radioGroup = dialog.findViewById<View>(R.id.filetype_picker_dialog_group) as RadioGroup
            val filetypePickerButton = dialog.findViewById<View>(R.id.filetype_picker_dialog_btn) as Button
            filetypePickerButton.setOnClickListener {
                val selectedId = radioGroup.checkedRadioButtonId
                val btnSelctd = dialog.findViewById<View>(selectedId) as RadioButton
                val selectedFileType = btnSelctd.text.toString().toLowerCase()
                openBook(ebk, selectedFileType)
                dialog.hide()
            }
            dialog.show()
        }

        private fun openBook(ebk: EBook, selectedFileType: String) {
            doAsync {
                try {
                    ebk!!.addTag(EBook.TAG_CURRENTLY_READING)
                    BookLibApplication.instance.getLibManager().updateBook(ebk!!)
                } finally {
                }
                MimeTypes.initInstance(ctx)
                FileUtils.openFile(FileHolder(File(ebk!!.fullFileDirName + "." + selectedFileType), false), ctx)
            }
        }
    }
    //#endregion

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
                    bkListSearchAdaptor = BookListItemAdaptor(this@BookLibSearchActivity, bklist, openBookHandler)
                    bkListSearch?.adapter = bkListSearchAdaptor
                }
            }
        }
    }

}
