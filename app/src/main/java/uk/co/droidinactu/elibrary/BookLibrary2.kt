package uk.co.droidinactu.elibrary

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.github.angads25.filepicker.model.DialogConfigs
import com.github.angads25.filepicker.model.DialogProperties
import com.github.angads25.filepicker.view.FilePickerDialog
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import uk.co.droidinactu.ebooklib.MyDebug
import uk.co.droidinactu.ebooklib.library.LibraryManager
import uk.co.droidinactu.ebooklib.room.EBook
import uk.co.droidinactu.elibrary.data.InfoBarViewModel
import uk.co.droidinactu.elibrary.data.LibraryInfo
import java.io.File

class BookLibrary2 : AppCompatActivity(), BookListFragment.OnBookActionListener {

    //#region fragment communication
    override fun onBookOpen(ebk: EBook) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, BookSearchFragment.newInstance("test1", "test2"))
            .commitNow()
    }

    override fun onBookDetails(ebk: EBook) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, BookDetailsFragment.newInstance("test1", "test2"))
            .commitNow()
    }

    override fun onBrowseForLibrary() {
        browseForLibrary()
    }
    //#endregion

    //#region Library Scanning Notification
    private val mHandlerScanningNotification = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            val handlerCmd = (msg.obj as String).split(":".toRegex())
            if (mNotificationManager == null) {
                mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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
        val resultIntent = Intent(applicationContext, BookLibrary::class.java)
        val resultPendingIntent = PendingIntent.getActivity(
            BookLibApplication.instance.applicationContext,
            0,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        var notificationBuilder = NotificationCompat.Builder(applicationContext, LibraryManager.CHANNEL_ID)
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
        var notificationBuilder = NotificationCompat.Builder(applicationContext, LibraryManager.CHANNEL_ID)
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

    private var mNotificationManager: NotificationManager? = null

    private var currentFragment: Fragment? = null

    private lateinit var infoBarModel: InfoBarViewModel
    private var toolBarBookLibrary: Toolbar? = null
    private var progressBar: ProgressBar? = null
    private var progressBarLabel: TextView? = null
    private var progressBarContainer: RelativeLayout? = null
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
            updateInfoBar()
        }
    }


    // Defines a Handler object that's attached to the UI thread
    private val mHandler = object : Handler(Looper.getMainLooper()) {
        /*
         * handleMessage() defines the operations to perform when
         * the Handler receives a new Message to process.
         */
        override fun handleMessage(msg: Message) {
            val libTitle = msg.obj as String
            doAsync {
                val bkCount = BookLibApplication.instance.getLibManager().getBookCount()
                uiThread {
                    MyDebug.LOG.debug("Library [" + libTitle + "] Updated. Now contains [" + bkCount + "] ebooks")
                    progressBarContainer?.visibility = View.GONE
                    updateInfoBar()
                    fab?.isEnabled = true
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.book_library_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, BookListFragment.newInstance("test1", "test2"))
                .commitNow()
        } else {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, BookListFragment.newInstance("test1", "test2"))
                .commitNow()
        }
        progressBarContainer = findViewById(R.id.frag_bk_list_progressbar_container)
        progressBar = findViewById(R.id.frag_bk_list_progressbar)
        progressBarLabel = findViewById(R.id.frag_bk_list_progressbar_label)
        progressBar?.visibility = View.INVISIBLE
        progressBar?.scaleY = 4f
        progressBarLabel?.text = ""
        toolBarBookLibrary = findViewById(R.id.toolbar)
        if (toolBarBookLibrary != null) {
            setSupportActionBar(toolBarBookLibrary)
            toolBarBookLibrary?.setSubtitle(R.string.app_subtitle)
            toolBarBookLibrary?.setLogo(R.mipmap.ic_launcher)
        }

        // Create a ViewModel the first time the system calls an activity's onCreate() method.
        // Re-created activities receive the same MyViewModel instance created by the first activity.
        infoBarModel = ViewModelProviders.of(this).get(InfoBarViewModel::class.java)
        infoBarModel.getInfo().observe(this, Observer<LibraryInfo> { _ ->
            updateInfoBar()
        })
    }

    override fun onAttachFragment(fragment: Fragment) {
        if (fragment is BookListFragment) {
            fragment.setOnBookActionListener(this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_booklib, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        MyDebug.LOG.debug("onOptionsItemSelected()")
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
            R.id.action_checkDb -> {
                doAsync {
                    //                    BookLibApplication.instance.getLibManager().checkDb(
//                        mHandler,
//                        mHandlerScanningNotification
//                    )
                }
                return true
            }
            R.id.action_clear_db -> {
                doAsync {
                    BookLibApplication.instance.getLibManager().clear()
                }
                Toast.makeText(this, "Database cleared", Toast.LENGTH_LONG).show()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
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

        val dialog = FilePickerDialog(this@BookLibrary2, properties)
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

    private fun rescanLibraries() {
        MyDebug.LOG.debug("BookLibrary::rescanLibraries()")
        fab?.isEnabled = false
        doAsync {
            val nbrBooks = BookLibApplication.instance.getLibManager().getBookCount()
            uiThread {
                progressBar?.max = nbrBooks
                progressBar?.progress = 0
                progressBar?.visibility = View.VISIBLE
                progressBarContainer?.visibility = View.VISIBLE
            }
            BookLibApplication.instance.getLibManager()
                .refreshLibraries(prgBrHandler, mHandler, mHandlerScanningNotification)
        }
    }

    private fun showSettings() {
//        val i = Intent(this, SettingsActivity::class.java)
//        val b = Bundle()
//        b.putInt("key", 1) //Your id
//        i.putExtras(b) //Put your id to your next Intent
//        startActivity(i)
    }

    private fun updateUI() {
        if (currentFragment != null && currentFragment is DroidInActuFragment) {
            (currentFragment as DroidInActuFragment).updateUI()
        }
    }

    private fun updateInfoBar() {
        doAsync {
            val libInfo = findViewById<TextView>(R.id.dashboard_library_info)
            val libInfoData = infoBarModel.getInfo().value
            if (libInfoData != null) {
                val text = String.format(
                    getResources().getQuantityString(R.plurals.library_contains_x_books, libInfoData.nbrBooks),
                    libInfoData.libTitle,
                    libInfoData.nbrBooks
                )
                uiThread {
                    libInfo.text = text
                }
            }
        }
    }

}
