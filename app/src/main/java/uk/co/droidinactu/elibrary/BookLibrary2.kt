package uk.co.droidinactu.elibrary

import android.annotation.SuppressLint
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
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import uk.co.droidinactu.ebooklib.MyDebug
import uk.co.droidinactu.ebooklib.room.EBook

class BookLibrary2 : AppCompatActivity(), BookListFragment.OnBookActionListener {
    // #region fragment communication
    override fun OnBookOpen(ebk: EBook) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, BookSearchFragment.newInstance("test1", "test2"))
            .commitNow()
    }

    override fun OnBookDetails(ebk: EBook) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, BookDetailsFragment.newInstance("test1", "test2"))
            .commitNow()
    }
// #endregion


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
                val bklist = BookLibApplication.instance.getLibManager().getBooks()
                uiThread {
                    MyDebug.LOG.debug("Library [" + libTitle + "] Updated. Now contains [" + bklist.size + "] ebooks")
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
        updateInfoBar()
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
            R.id.action_flutter -> {
//                var flutterView = Flutter.createView(
//                    this,
//                    getLifecycle(),
//                    "route1"
//                )
//                var layout = FrameLayout.LayoutParams(600, 800)
//                layout.leftMargin = 100
//                layout.topMargin = 200Ó
//                addContentView(flutterView, layout)
                return true
            }
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
//                doAsync {
//                    BookLibApplication.instance.getLibManager().checkDb(
//                        mHandler,
//                        mHandlerScanningNotification
//                    )
//                }
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

    private fun rescanLibraries() {
        MyDebug.LOG.debug("BookLibrary::rescanLibraries()")
        fab?.isEnabled = false
        doAsync {
            val nbrBooks = BookLibApplication.instance.getLibManager().getBookCount()
//            uiThread {
            progressBar?.max = nbrBooks
            progressBar?.progress = 0
            progressBar?.visibility = View.VISIBLE
            progressBarContainer?.visibility = View.VISIBLE
        }
//            BookLibApplication.instance.getLibManager()
//                .refreshLibraries(prgBrHandler, mHandler, mHandlerScanningNotification)
//        }
    }

    private fun showSettings() {
//        val i = Intent(this, SettingsActivity::class.java)
//        val b = Bundle()
//        b.putInt("key", 1) //Your id
//        i.putExtras(b) //Put your id to your next Intent
//        startActivity(i)
    }

    private fun updateInfoBar() {
        doAsync {
            val libInfo = findViewById<TextView>(R.id.dashboard_library_info)
            val libTitle = BookLibApplication.instance.getLibManager().getLibrary().libraryTitle
            val nbrBooks = BookLibApplication.instance.getLibManager().getBookCount()
            val text = String.format(
                getResources().getQuantityString(R.plurals.library_contains_x_books, nbrBooks),
                libTitle,
                nbrBooks
            )
            uiThread {
                libInfo.text = text
            }
        }
    }

}
