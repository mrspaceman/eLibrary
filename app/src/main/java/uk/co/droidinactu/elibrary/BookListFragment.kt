package uk.co.droidinactu.elibrary

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.uiThread
import uk.co.droidinactu.ebooklib.MyDebug
import uk.co.droidinactu.ebooklib.files.FileHolder
import uk.co.droidinactu.ebooklib.files.FileUtils
import uk.co.droidinactu.ebooklib.files.MimeTypes
import uk.co.droidinactu.ebooklib.room.EBook
import uk.co.droidinactu.elibrary.data.BookListViewModel
import java.io.File
import java.util.stream.Collectors

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [BookListFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [BookListFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class BookListFragment : DroidInActuFragment() {
    // #region fragment communication
    internal var callback: OnBookActionListener? = null
    private lateinit var model: BookListViewModel

    fun setOnBookActionListener(callback: OnBookActionListener) {
        this.callback = callback
    }

    // This interface can be implemented by the Activity, parent Fragment,
    // or a separate test implementation.
    interface OnBookActionListener {
        fun onBookOpen(ebk: EBook)
        fun onBookDetails(ebk: EBook)
        fun onBrowseForLibrary()
    }
    // #endregion

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var selectedTag = BookLibrary.NO_TAG_SELECTED

    /**
     *
     */
    private val prgBrHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            val prgrsMsg = (msg.obj as String).split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val libName = prgrsMsg[0]
            val curr = Integer.valueOf(prgrsMsg[1])
            val max = Integer.valueOf(prgrsMsg[2])
            val res = resources
            val prgMsg = String.format(res.getString(R.string.rescan_progress_msg), libName, curr, max)
            progressBar?.progress = curr
            progressBar?.max = max
            progressBarLabel?.text = prgMsg
        }
    }
    private val tagSelectedHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            selectedTag = msg.obj as String
            savePreferences()
            updateUI()
        }
    }

    //#region Open Book Handler
    private val openBookHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            val msgObjStr = msg.obj as String
            doAsync {
                val ebkFilename = msgObjStr.split(":").get(1)
                val ebk = BookLibApplication.instance.getLibManager().getBookFromFullFilename(ebkFilename)
                val ftypes = ebk?.filetypes
                uiThread {
                    if (ftypes != null && ftypes.size > 1) {
                        showFileTypePickerDialog(ebk)
                    } else {
                        openBook(ebk, ftypes?.first().toString().toLowerCase())
                    }
                }
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

    // Defines a Handler object that's attached to the UI thread
    private val mHandler = object : Handler(Looper.getMainLooper()) {
        /*
         * handleMessage() defines the operations to perform when
         * the Handler receives a new Message to process.
         */
        override fun handleMessage(msg: Message) {
            val libTitle = msg.obj as String
            doAsync {
                val bklist = BookLibApplication.instance.getLibManager().getBooks()
                uiThread {
                    MyDebug.LOG.debug("Library [" + libTitle + "] Updated. Now contains [" + bklist.size + "] ebooks")
                    progressBarContainer?.visibility = View.GONE
                    updateLists()
                    fab?.isEnabled = true
                }
            }
        }
    }

    private var bookListCurrentReading: RecyclerView? = null
    private lateinit var tagList: RecyclerView
    private lateinit var bookList: RecyclerView
    private var bookListTag1Title: Button? = null
    private var bookListAdaptorCurrReading: BookListItemAdaptor? = null
    private var bookListAdaptor: BookListItemAdaptor? = null
    private var tagListAdaptor: TagListItemAdaptor? = null

    private var fab: FloatingActionButton? = null
    private var progressBar: ProgressBar? = null
    private var progressBarLabel: TextView? = null
    private var progressBarContainer: RelativeLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_book_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bookListCurrentReading = view.findViewById(R.id.frag_bk_list_reading)
        bookListTag1Title = view.findViewById(R.id.frag_bk_list_books_list_tag)
        bookList = view.findViewById(R.id.frag_bk_list_books_list)
        tagList = view.findViewById(R.id.frag_bk_list_tags_list)
        progressBarContainer = view.findViewById(R.id.frag_bk_list_progressbar_container)
        progressBar = view.findViewById(R.id.frag_bk_list_progressbar)
        progressBarLabel = view.findViewById(R.id.frag_bk_list_progressbar_label)
        fab = view.findViewById(R.id.fab)
        updateUI()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val horizontalLayoutManager = LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)
        bookListCurrentReading?.layoutManager = horizontalLayoutManager
        bookListCurrentReading?.setHasFixedSize(true)

        //    val bkListLayoutManager = GridLayoutManager(this, 4)
        val bkListLayoutManager = LinearLayoutManager(activity)
        bookList.layoutManager = bkListLayoutManager
        bookList.setHasFixedSize(true)
        bookListTag1Title?.setOnClickListener {
            //pickTag2(1);
            pickTag(1)
        }

        val tagListLayoutManager = LinearLayoutManager(activity)
        tagList.layoutManager = tagListLayoutManager
        tagList.setHasFixedSize(true)
        tagList.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))

        progressBarContainer?.visibility = View.GONE

        progressBar?.visibility = View.INVISIBLE
        progressBar?.scaleY = 4f
        progressBarLabel?.text = ""

        fab?.setOnClickListener { callback!!.onBrowseForLibrary() }

        // Create a ViewModel the first time the system calls an activity's onCreate() method.
        // Re-created activities receive the same MyViewModel instance created by the first activity.
        model = ViewModelProviders.of(this).get(BookListViewModel::class.java)
        model.getBooks().observe(this, Observer<List<EBook>> { books ->
            updateUI()
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnBookActionListener) {
            callback = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
        loadPreferences()
        updateUI()
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
        savePreferences()
    }

    override fun updateUI() {
        updateLists()
        bookListTag1Title?.text = selectedTag
    }

    private fun pickTag(tagToSet: Int) {
        MyDebug.LOG.debug("BookLibrary::pickTag($tagToSet) started")
        val ctx = this
        doAsync {
            val tglist =
                BookLibApplication.instance.getLibManager().getTags().stream().sorted().collect(Collectors.toList())
            //tglist.remove(EBook.CURRENTLY_READING)

            uiThread {
                val dialog = Dialog(getActivity())
                dialog.setContentView(R.layout.tag_list_picker_dialog)
                dialog.setTitle("Pick an EBook tag")
                val tagLstPickerList = dialog.findViewById<View>(R.id.tag_list_picker_list) as ListView
                val adapter = ArrayAdapter(activity, android.R.layout.simple_list_item_1, android.R.id.text1, tglist)

                // set the custom dialog components - text, image and button
                tagLstPickerList.adapter = adapter

                tagLstPickerList.onItemClickListener =
                    AdapterView.OnItemClickListener { parent, view, position, id ->
                        selectedTag = tglist[position]
                        when (tagToSet) {
                            1 -> {
                                savePreferences()
                                updateUI()
                            }
                            2 -> {
                            }
                        }
                        dialog.dismiss()
                    }
                dialog.show()
            }
        }
    }

    private fun updateBookListCurrReading() {
        MyDebug.LOG.debug("BookLibrary::updateBookListCurrReading()")
        doAsync {
            val bklist = BookLibApplication.instance.getLibManager()
                .getBooksForTag(EBook.TAG_CURRENTLY_READING)
            bookListAdaptorCurrReading =
                BookListItemAdaptor(BookLibApplication.instance.applicationContext, bklist, openBookHandler)
            uiThread {
                bookListCurrentReading?.adapter = bookListAdaptorCurrReading
            }
        }
    }

    private fun updateBookList() {
        MyDebug.LOG.debug("BookLibrary::updateBookList()")
        if (selectedTag.compareTo(BookLibrary.NO_TAG_SELECTED) != 0) {
            doAsync {
                val bklist = BookLibApplication.instance.getLibManager()
                    .getBooksForTag(selectedTag.toString())
                bookListAdaptor = BookListItemAdaptor(context!!.applicationContext, bklist, openBookHandler)
                uiThread {
                    bookList.adapter = bookListAdaptor
                }
            }
        }
    }

    private fun updateTagList() {
        MyDebug.LOG.debug("BookLibrary::updateTagList()")
        doAsync {
            val taglist = BookLibApplication.instance.getLibManager().getTags()
            tagListAdaptor = TagListItemAdaptor(taglist, tagSelectedHandler)
            uiThread {
                tagList.adapter = tagListAdaptor
            }
        }
    }

    private fun updateLists() {
        updateBookListCurrReading()
        updateBookList()
        updateTagList()
    }

    private fun savePreferences() {
        val settings = activity!!.getSharedPreferences("BookLibApplication", 0)
        val editor = settings.edit()
        editor.putString("bookListTag1Title", selectedTag.toString())
        editor.apply()
    }

    private fun loadPreferences() {
        val settings = activity!!.getSharedPreferences("BookLibApplication", 0)
        selectedTag = settings.getString("bookListTag1Title", BookLibrary.NO_TAG_SELECTED)
        bookListTag1Title?.text = selectedTag
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BookListFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BookListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

}
