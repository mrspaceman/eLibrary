package uk.co.droidinactu.elibrary

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.angads25.filepicker.model.DialogConfigs
import com.github.angads25.filepicker.model.DialogProperties
import com.github.angads25.filepicker.view.FilePickerDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import uk.co.droidinactu.ebooklib.MyDebug
import uk.co.droidinactu.ebooklib.library.LibraryManager
import uk.co.droidinactu.ebooklib.room.EBook
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
class BookListFragment : Fragment() {
    // #region fragment communication
    internal var callback: OnBookActionListener? = null

    fun setOnBookActionListener(callback: OnBookActionListener) {
        this.callback = callback
    }

    // This interface can be implemented by the Activity, parent Fragment,
    // or a separate test implementation.
    interface OnBookActionListener {
        fun OnBookOpen(ebk: EBook)
        fun OnBookDetails(ebk: EBook)
    }
    // #endregion

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


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
            val selectedTag = msg.obj as String
            bookListTag1Title?.setText(selectedTag)
            updateBookList()
        }
    }

    //#region Library Scanning Notification
    private val mHandlerScanningNotification = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            val handlerCmd = (msg.obj as String).split(":".toRegex())
            if (mNotificationManager == null) {
                mNotificationManager = activity!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }
            if (handlerCmd[0].equals("startscanning")) {
                displayScanningNotification(handlerCmd[1])
            } else if (handlerCmd[0].equals("stopscanning")) {
                removeScanningNotification()
            } else if (handlerCmd[0].equals("startdbCheck")) {
                displayDbCheckNotification()
            } else if (handlerCmd[0].equals("stopdbcheck")) {
                removeDbCheckNotification()
            }
        }
    }

    private fun displayScanningNotification(libname: String) {
        val resultIntent = Intent(activity!!.applicationContext, BookLibrary::class.java)
        val resultPendingIntent = PendingIntent.getActivity(
            BookLibApplication.instance.applicationContext,
            0,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        var notificationBuilder = NotificationCompat.Builder(activity!!.applicationContext, LibraryManager.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Scanning Library")
            .setContentText("Scan library $libname for ebooks")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(resultPendingIntent)
            .setProgress(0, 0, true)
        val notification = notificationBuilder?.build()

        mNotificationManager?.notify(BookLibrary.mNotificationIdScanning, notification)
    }

    private fun removeScanningNotification() {
        mNotificationManager?.cancel(BookLibrary.mNotificationIdScanning)
    }

    private fun displayDbCheckNotification() {
        var notificationBuilder = NotificationCompat.Builder(activity!!.applicationContext, LibraryManager.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Checking Library for consistency")
            .setContentText("Checking Library for consistency")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setProgress(0, 0, true)
        val notification = notificationBuilder?.build()

        mNotificationManager?.notify(BookLibrary.mNotificationIdDbCheck, notification)
    }

    private fun removeDbCheckNotification() {
        mNotificationManager?.cancel(BookLibrary.mNotificationIdDbCheck)
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

    private var mNotificationManager: NotificationManager? = null

    private var libraryScanPndingIntnt: PendingIntent? = null
    private var libraryScanAlarmIntent: Intent? = null
    private var bookListCurrentReading: RecyclerView? = null
    private lateinit var tagList: RecyclerView
    private lateinit var bookList: RecyclerView
    private var libInfo: TextView? = null
    private var bookListTag1Title: Button? = null
    private var bookListTag1IncludeSubTags: Boolean = false
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

        fab?.setOnClickListener { browseForLibrary() }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnBookActionListener) {
            callback = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
        loadPreferences()
        updateLists()
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
        savePreferences()
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
                        val selectedFromList = tglist[position]
                        when (tagToSet) {
                            1 -> {
                                bookListTag1Title?.text = selectedFromList
                                savePreferences()
                                updateBookList()
                                updateTagList()
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

    private fun browseForLibrary() {
        MyDebug.LOG.debug("BookLibrary::browseForLibrary()")
        fab?.isEnabled = false
        val rootdir = "/storage/"

        val properties = DialogProperties()
        properties.selection_mode = DialogConfigs.SINGLE_MODE
        properties.selection_type = DialogConfigs.DIR_SELECT
        properties.root = File(DialogConfigs.DEFAULT_DIR)
        properties.root = File(rootdir)
        properties.extensions = null

        val dialog = FilePickerDialog(activity, properties)
        dialog.setDialogSelectionListener { files ->
            if (files.isNotEmpty()) {
                val fileBits = files[0].split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val libraryName = fileBits[fileBits.size - 1]
                progressBar?.max = 2500
                progressBar?.progress = 0
                progressBar?.visibility = View.VISIBLE
                progressBarContainer?.visibility = View.VISIBLE
                doAsync {
                    BookLibApplication.instance.getLibManager()
                        .addLibrary(prgBrHandler, mHandler, mHandlerScanningNotification, libraryName, files[0])
                }
            }
        }
        dialog.show()
    }

    private fun updateBookListCurrReading() {
        MyDebug.LOG.debug("BookLibrary::updateBookListCurrReading()")
        doAsync {
            val bklist = BookLibApplication.instance.getLibManager()
                .getBooksForTag(EBook.TAG_CURRENTLY_READING)
            bookListAdaptorCurrReading = BookListItemAdaptor(BookLibApplication.instance.applicationContext, bklist)
            uiThread {
                bookListCurrentReading?.adapter = bookListAdaptorCurrReading
            }
        }
    }

    private fun updateBookList() {
        MyDebug.LOG.debug("BookLibrary::updateBookList()")
        if (bookListTag1Title?.text.toString().compareTo(BookLibrary.NO_TAG_SELECTED) != 0) {
            doAsync {
                val bklist = BookLibApplication.instance.getLibManager()
                    .getBooksForTag(bookListTag1Title?.text.toString())
                bookListAdaptor = BookListItemAdaptor(BookLibApplication.instance.applicationContext, bklist)
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
        editor.putString("bookListTag1Title", bookListTag1Title?.text.toString())
        editor.apply()
    }

    private fun loadPreferences() {
        val settings = activity!!.getSharedPreferences("BookLibApplication", 0)
        bookListTag1Title?.text = settings.getString("bookListTag1Title", BookLibrary.NO_TAG_SELECTED)
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
