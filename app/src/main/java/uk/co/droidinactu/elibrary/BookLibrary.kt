package uk.co.droidinactu.elibrary

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.angads25.filepicker.model.DialogConfigs
import com.github.angads25.filepicker.model.DialogProperties
import com.github.angads25.filepicker.view.FilePickerDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import uk.co.droidinactu.elibrary.BookLibApplication.Companion.LOG_TAG
import uk.co.droidinactu.elibrary.library.FileObserverService
import uk.co.droidinactu.elibrary.library.LibraryManager
import uk.co.droidinactu.elibrary.library.RecursiveFileObserver.Companion.INTENT_EBOOK_MODIFIED
import uk.co.droidinactu.elibrary.library.RecursiveFileObserver.Companion.INTENT_EBOOK_MODIFIED_PATH
import uk.co.droidinactu.elibrary.room.Tag
import java.io.File
import java.util.*

class BookLibrary : AppCompatActivity() {

    private val NO_TAG_SELECTED = "<none selected>"
    private var libraryScanPndingIntnt: PendingIntent? = null
    private var libraryScanAlarmIntent: Intent? = null
    private var bookListCurrentReading: RecyclerView? = null
    private var bookListTag1: RecyclerView? = null
    private var toolBarBookLibrary: Toolbar? = null
    private var bookListTag1Title: Button? = null
    private var bookListTag1IncludeSubTags: Boolean = false
    private var bookListAdaptorCurrReading: BookListItemAdaptor? = null
    private var bookListAdaptorTag1: BookListItemAdaptor? = null
    private val mLayoutManager: RecyclerView.LayoutManager? = null
    private var fab: FloatingActionButton? = null
    private var progressBar: ProgressBar? = null
    private var progressBarLabel: TextView? = null
    private var progressBarContainer: RelativeLayout? = null

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
            progressBar?.setProgress(curr)
            progressBar?.setMax(max)
            progressBarLabel?.setText(prgMsg)
        }
    }


    // Defines a Handler object that's attached to the UI thread
    private val mHandler = object : Handler(Looper.getMainLooper()) {
        /*
         * handleMessage() defines the operations to perform when
         * the Handler receives a new Message to process.
         */
        override fun handleMessage(inputMessage: Message) {
            val libTitle = inputMessage.obj as String
            val bklist = BookLibApplication.instance.getLibManager()!!.getBooks()
            Log.d(LOG_TAG, "Library [" + libTitle + "] Updated. Now contains [" + bklist.size + "] ebooks")
            progressBarContainer?.setVisibility(View.GONE)
            updateLists()
            fab?.setEnabled(true)
        }
    }
    private val ebkChngdListener = EBookChangedReceiver()

    inner class EBookChangedReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(LOG_TAG, "EBookChangedReceiver::onReceive()")
            MyDebug.debugIntent(intent)

            var ebkPath: String? = ""
            val b = intent.extras
            if (b != null) {
                ebkPath = b.getString(INTENT_EBOOK_MODIFIED_PATH, "")
            }
            if (ebkPath != null && ebkPath.length > 0) {
                BookLibApplication.instance.getLibManager()!!.reReadEBookMetadata(ebkPath)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolBarBookLibrary = findViewById(R.id.toolbar)
        if (toolBarBookLibrary != null) {
            setSupportActionBar(toolBarBookLibrary)
        }

        if (BuildConfig.DEBUG) {
            val appTitle = getString(R.string.app_title)
            val appVerName = ""
            try {
                val appVerName = BookLibApplication.instance.getAppVersionName(
                    "uk.co.droidinactu.elibrary"
                )
            } catch (e: Exception) {
                error(LOG_TAG + "Exception getting app vername")
            }
            supportActionBar!!.setTitle(
                appTitle + " (" + appVerName + ")"
            )
        } else {
            supportActionBar!!.setTitle(getString(R.string.app_title))
        }
        toolBarBookLibrary?.setSubtitle(R.string.app_subtitle)
        toolBarBookLibrary?.setLogo(R.mipmap.ic_launcher)

        bookListCurrentReading = findViewById(R.id.dashboard_books_list_reading) as RecyclerView
        val horizontalLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        bookListCurrentReading?.setLayoutManager(horizontalLayoutManager)
        bookListCurrentReading?.setHasFixedSize(true)

        val gridLayoutManager = GridLayoutManager(this, 4)
        bookListTag1Title = findViewById(R.id.dashboard_books_list_tag1_title) as Button
        bookListTag1 = findViewById(R.id.dashboard_books_list_tag1) as RecyclerView
        bookListTag1?.setLayoutManager(gridLayoutManager)
        bookListTag1?.setHasFixedSize(true)
        bookListTag1Title?.setOnClickListener(View.OnClickListener { pickTag(1) })

        progressBarContainer = findViewById(R.id.dashboard_books_list_progressbar_container) as RelativeLayout
        progressBarContainer?.setVisibility(View.GONE)

        progressBar = findViewById(R.id.dashboard_books_list_progressbar) as ProgressBar
        progressBar?.setVisibility(View.INVISIBLE)
        progressBar?.setScaleY(3f)
        progressBarLabel = findViewById(R.id.dashboard_books_list_progressbar_label) as TextView
        progressBarLabel?.setText("")

        fab = findViewById(R.id.fab) as FloatingActionButton
        fab?.setOnClickListener(View.OnClickListener { browseForLibrary() })
    }

    private fun pickTag(tagToSet: Int) {
        val ctx = this
        doAsync {
            val tglist = BookLibApplication.instance.getLibManager().getTagList()
            // val tgTree = BookLibApplication.getLibManager().getTagTree()
            //    tglist.remove(BookTag.CURRENTLY_READING)

            uiThread {
                val dialog = Dialog(ctx)
                dialog.setContentView(R.layout.tag_list_picker_dialog)
                dialog.setTitle("Pick an EBook tag")

                // set the custom dialog components - text, image and button
                val tagLstIncludeSubTags = dialog.findViewById<View>(R.id.tag_list_picker_include_subtags) as CheckBox
                tagLstIncludeSubTags.isChecked = true
                val tagLstPickerList = dialog.findViewById<View>(R.id.tag_list_picker_list) as ListView
                val adapter = ArrayAdapter(ctx, android.R.layout.simple_list_item_1, android.R.id.text1, tglist)
                tagLstPickerList.adapter = adapter

                tagLstPickerList.onItemClickListener =
                    AdapterView.OnItemClickListener { parent, view, position, id ->
                        val selectedFromList = tglist[position]
                        when (tagToSet) {
                            1 -> {
                                bookListTag1Title?.setText(selectedFromList)
                                bookListTag1IncludeSubTags = tagLstIncludeSubTags.isChecked
                                savePreferences()
                                updateBookListTag1(bookListTag1IncludeSubTags)
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
        Log.d(LOG_TAG, "browseForLibrary()")
        fab?.setEnabled(false)
        val rootdir = "/storage/"

        val properties = DialogProperties()
        properties.selection_mode = DialogConfigs.SINGLE_MODE
        properties.selection_type = DialogConfigs.DIR_SELECT
        properties.root = File(DialogConfigs.DEFAULT_DIR)
        properties.root = File(rootdir)
        properties.extensions = null

        val dialog = FilePickerDialog(this@BookLibrary, properties)
        dialog.setDialogSelectionListener { files ->
            if (files.size > 0) {
                val fileBits = files[0].split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val libraryName = fileBits[fileBits.size - 1]
                progressBar?.setMax(2500)
                progressBar?.setProgress(0)
                progressBar?.setVisibility(View.VISIBLE)
                progressBarContainer?.setVisibility(View.VISIBLE)
                BookLibApplication.instance.getLibManager()!!
                    .addLibrary(prgBrHandler, mHandler, libraryName, files[0])
            }
        }
        dialog.show()
    }

    private fun savePreferences() {
        val settings = getSharedPreferences(BookLibApplication.LOG_TAG, 0)
        val editor = settings.edit()
        editor.putString("bookListTag1Title", bookListTag1Title?.getText().toString())
        editor.putBoolean("bookListTag1IncludeSubTags", bookListTag1IncludeSubTags)
        editor.apply()
    }

    private fun updateBookListTag1(includeSubTags: Boolean) {
        if (bookListTag1Title?.getText().toString().compareTo(NO_TAG_SELECTED) != 0) {
            val bklist = BookLibApplication.instance.getLibManager()
                .getBooksForTag(bookListTag1Title?.getText().toString(), includeSubTags)
            bookListAdaptorTag1 = BookListItemAdaptor(bklist)
            bookListTag1?.setAdapter(bookListAdaptorTag1)
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(LOG_TAG, "onStart()")
//        if (BookLibApplication.instance.getLibManager().getLibraries().size === 0) {
//            browseForLibrary()
//        }
        BookLibApplication.instance.registerReceiver(
            ebkChngdListener,
            IntentFilter(INTENT_EBOOK_MODIFIED)
        )

        val intent =
            Intent(BookLibApplication.instance.applicationContext, FileObserverService::class.java)
        BookLibApplication.instance.applicationContext.startService(intent)
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_booklib, menu)
        return true
    }

    private lateinit var libMgr: LibraryManager
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(LOG_TAG, "onOptionsItemSelected()")
        // Handle item selection
        when (item.itemId) {
            R.id.action_search -> {
                val i = Intent(this, BookLibSearchActivity::class.java)
                val b = Bundle()
                b.putInt("key", 1) //Your id
                i.putExtras(b) //Put your id to your next Intent
                startActivity(i)
                return true
            }
            R.id.action_refresh -> {
                rescanLibraries()
                return true
            }
            R.id.action_settings -> {
                showSettings()
                return true
            }
            R.id.action_clear_db -> {
                doAsync {
                    BookLibApplication.instance.getLibManager()!!.clear()
                }
                Toast.makeText(this, "Database cleared", Toast.LENGTH_LONG).show()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(LOG_TAG, "onPause()")
    }

    override fun onResume() {
        super.onResume()
        Log.d(LOG_TAG, "onResume()")
        val settings = getSharedPreferences(BookLibApplication.LOG_TAG, 0)
        bookListTag1Title?.setText(settings.getString("bookListTag1Title", NO_TAG_SELECTED))
        bookListTag1IncludeSubTags = settings.getBoolean("bookListTag1IncludeSubTags", true)
        updateLists()
    }

    private fun rescanLibraries() {
        fab?.setEnabled(false)
        doAsync {
            val nbrBooks = BookLibApplication.instance.getLibManager().getBooks().size
            uiThread {
                progressBar?.setMax(nbrBooks)
            }
        }
        progressBar?.setProgress(0)
        progressBar?.setVisibility(View.VISIBLE)
        progressBarContainer?.setVisibility(View.VISIBLE)
        BookLibApplication.instance.getLibManager().refreshLibraries(prgBrHandler, mHandler)
        scheduleNextLibraryScan()
    }

    private fun showSettings() {
//        val i = Intent(this, SettingsActivity::class.java)
//        val b = Bundle()
//        b.putInt("key", 1) //Your id
//        i.putExtras(b) //Put your id to your next Intent
//        startActivity(i)
    }

    private fun scheduleNextLibraryScan() {
        // time at which alarm will be scheduled here alarm is scheduled at 1 day from current time,
        // we fetch  the current time in milliseconds and added 1 day time
        // i.e. 24*60*60*1000= 86,400,000   milliseconds in a day
        val hours = 6
        val time = GregorianCalendar().timeInMillis + hours * (60 * 60 * 1000)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // create an Intent and set the class which will execute when Alarm triggers, here we have
        // given LibraryScanAlarmReciever in the Intent, the onRecieve() method of this class will
        // execute when alarm triggers and we will write the code to send SMS inside onRecieve()
        // method pf LibraryScanAlarmReciever class
        if (libraryScanAlarmIntent == null) {
            libraryScanAlarmIntent = Intent(this, LibraryScanAlarmReceiver::class.java)
        }
        if (libraryScanPndingIntnt == null) {
            //set the alarm for particular time
            libraryScanPndingIntnt =
                PendingIntent.getBroadcast(this, 1, libraryScanAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            alarmManager.cancel(libraryScanPndingIntnt)
        }
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, libraryScanPndingIntnt)
        Toast.makeText(this, "Alarm Scheduled for [$hours] hours", Toast.LENGTH_LONG).show()
    }

    private fun updateLists() {
        updateBookListCurrReading()
        updateBookListTag1(bookListTag1IncludeSubTags)
    }

    private fun updateBookListCurrReading() {
        doAsync {
            val bklist =
                BookLibApplication.instance.getLibManager().getBooksForTag(
                    Tag.CURRENTLY_READING,
                    false
                )
            uiThread {
                bookListAdaptorCurrReading = BookListItemAdaptor(bklist)
                bookListCurrentReading?.setAdapter(bookListAdaptorCurrReading)
                setupShortcuts()
            }
        }
    }

    private fun setupShortcuts() {
        //        ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
        //        shortcutManager.removeAllDynamicShortcuts();
        //
        //        final List<EBook> bklist = ((BookLibApplication)getApplicationContext()).getLibManager().getBooksForTag(BookTag.CURRENTLY_READING, false);
        //        for (EBook ebk : bklist) {
        //            ShortcutInfo shortcut = new ShortcutInfo.Builder(this, ebk.getFull_file_dir_name())
        //                    .setShortLabel(ebk.getBook_title())
        //                    .setLongLabel(ebk.getAuthorString())
        //                    .setIcon(Icon.createWithResource(this, R.drawable.generic_book_cover))
        //                    .setIntent(ebk.getOpenIntent(this))
        //                    .build();
        //            shortcutManager.addDynamicShortcuts(Arrays.asList(shortcut));
        //        }
    }

    inner class LibraryScanFinishedReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            fab?.setEnabled(true)
            progressBarContainer?.setVisibility(View.INVISIBLE)
        }
    }

}
