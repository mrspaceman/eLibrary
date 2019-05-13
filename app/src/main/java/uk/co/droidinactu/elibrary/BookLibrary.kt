package uk.co.droidinactu.elibrary

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Dialog
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.angads25.filepicker.model.DialogConfigs
import com.github.angads25.filepicker.model.DialogProperties
import com.github.angads25.filepicker.view.FilePickerDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import uk.co.droidinactu.ebooklib.MyDebug
import uk.co.droidinactu.ebooklib.library.FileObserverService
import uk.co.droidinactu.ebooklib.library.LibraryManager
import uk.co.droidinactu.ebooklib.library.RecursiveFileObserver.Companion.INTENT_EBOOK_MODIFIED
import uk.co.droidinactu.ebooklib.library.RecursiveFileObserver.Companion.INTENT_EBOOK_MODIFIED_PATH
import uk.co.droidinactu.ebooklib.room.EBook
import java.io.File
import java.util.*
import java.util.stream.Collectors

class BookLibrary : AppCompatActivity() {

    companion object {
        val mNotificationIdScanning = 14
        val mNotificationIdDbCheck = 15

        val NO_TAG_SELECTED = "<none selected>"
    }

    private val RC_SIGN_IN: Int = 8191
    //  private lateinit var mAuth :FirebaseAuth

    private var mNotificationManager: NotificationManager? = null

    private var libraryScanPndingIntnt: PendingIntent? = null
    private var libraryScanAlarmIntent: Intent? = null
    private var bookListCurrentReading: RecyclerView? = null
    private var bookListTag1: RecyclerView? = null
    private var toolBarBookLibrary: Toolbar? = null
    private var bookListTag1Title: Button? = null
    private var bookListTag1IncludeSubTags: Boolean = false
    private var bookListAdaptorCurrReading: BookListItemAdaptor? = null
    private var bookListAdaptorTag1: BookListItemAdaptor? = null

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
            progressBar?.progress = curr
            progressBar?.max = max
            progressBarLabel?.text = prgMsg
            updateInfoBar()
        }
    }

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
        val resultIntent = Intent(BookLibApplication.instance.applicationContext, BookLibrary::class.java)
        val resultPendingIntent = PendingIntent.getActivity(
            BookLibApplication.instance.applicationContext,
            0,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        var notificationBuilder = NotificationCompat.Builder(this, LibraryManager.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Scanning Library")
            .setContentText("Scan library $libname for ebooks")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(resultPendingIntent)
            .setProgress(0, 0, true)
        val notification = notificationBuilder?.build()

        mNotificationManager?.notify(mNotificationIdScanning, notification)
    }

    private fun removeScanningNotification() {
        mNotificationManager?.cancel(mNotificationIdScanning)
    }

    private fun displayDbCheckNotification() {
        var notificationBuilder = NotificationCompat.Builder(this, LibraryManager.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Checking Library for consistency")
            .setContentText("Checking Library for consistency")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setProgress(0, 0, true)
        val notification = notificationBuilder?.build()

        mNotificationManager?.notify(mNotificationIdDbCheck, notification)
    }

    private fun removeDbCheckNotification() {
        mNotificationManager?.cancel(mNotificationIdDbCheck)
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
                    updateInfoBar()
                    updateLists()
                    fab?.isEnabled = true
                }
            }
        }
    }
    private val ebkChngdListener = EBookChangedReceiver()

    inner class EBookChangedReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            MyDebug.LOG.debug("EBookChangedReceiver::onReceive()")
            MyDebug.debugIntent(intent)

            var ebkPath: String? = ""
            val b = intent.extras
            if (b != null) {
                ebkPath = b.getString(INTENT_EBOOK_MODIFIED_PATH, "")
            }
            if (ebkPath != null && ebkPath.isNotEmpty()) {
                BookLibApplication.instance.getLibManager().reReadEBookMetadata(ebkPath)
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
            MyDebug.LOG.debug("Logging Enabled")
            val appTitle = getString(R.string.app_title)
            try {
                val appVerName = BookLibApplication.instance.getAppVersionName()
                supportActionBar!!.title = "$appTitle ($appVerName)"
            } catch (e: Exception) {
                MyDebug.LOG.error("Exception getting app vername")
            }
        } else {
            supportActionBar!!.setTitle(getString(R.string.app_title))
        }
        toolBarBookLibrary?.setSubtitle(R.string.app_subtitle)
        toolBarBookLibrary?.setLogo(R.mipmap.ic_launcher)

        bookListCurrentReading = findViewById(R.id.dashboard_books_list_reading)
        val horizontalLayoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        bookListCurrentReading?.layoutManager = horizontalLayoutManager
        bookListCurrentReading?.setHasFixedSize(true)

        val gridLayoutManager = GridLayoutManager(this, 4)
        bookListTag1Title = findViewById(R.id.dashboard_books_list_tag1_title)
        bookListTag1 = findViewById(R.id.dashboard_books_list_tag1)
        bookListTag1?.layoutManager = gridLayoutManager
        bookListTag1?.setHasFixedSize(true)
        bookListTag1Title?.setOnClickListener {
            //pickTag2(1);
            pickTag(1)
        }

        progressBarContainer = findViewById(R.id.dashboard_books_list_progressbar_container)
        progressBarContainer?.visibility = View.GONE

        progressBar = findViewById(R.id.dashboard_books_list_progressbar)
        progressBar?.visibility = View.INVISIBLE
        progressBar?.scaleY = 4f
        progressBarLabel = findViewById(R.id.dashboard_books_list_progressbar_label)
        progressBarLabel?.text = ""

        fab = findViewById(R.id.fab)
        fab?.setOnClickListener { browseForLibrary() }
    }

    //#region Firebase
//    private fun checkFirebaseUser() {
//        mAuth = FirebaseAuth.getInstance()
//        val currentUser = mAuth.getCurrentUser()
//        if (currentUser == null) {
//            firebaseSignin()
//        } else {
//            // Name, email address, and profile photo Url
//            var name = currentUser.getDisplayName()
//            var email = currentUser.getEmail()
//            var photoUrl = currentUser.getPhotoUrl()
//
//            // Check if user's email is verified
//            var emailVerified = currentUser.isEmailVerified()
//
//            // The user's ID, unique to the Firebase project. Do NOT use this value to
//            // authenticate with your backend server, if you have one. Use
//            // FirebaseUser.getIdToken() instead.
//            var uid = currentUser.getUid()
//        }
//    }
//
//    fun firebaseSignin() {
//        // Choose authentication providers
//        val providers = arrayListOf(
//            AuthUI.IdpConfig.EmailBuilder().build(),
//            AuthUI.IdpConfig.GoogleBuilder().build()
//            //    AuthUI.IdpConfig.FacebookBuilder().build(),
//            //    AuthUI.IdpConfig.TwitterBuilder().build()
//        )
//
//        // Create and launch sign-in intent
//        startActivityForResult(
//            AuthUI.getInstance()
//                .createSignInIntentBuilder()
//                .setAvailableProviders(providers)
//                .build(),
//            RC_SIGN_IN
//        )
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == RC_SIGN_IN) {
//            val response = IdpResponse.fromResultIntent(data)
//
//            if (resultCode == Activity.RESULT_OK) {
//                // Successfully signed in
//                val user = FirebaseAuth.getInstance().currentUser
//                // ...
//            } else {
//                // Sign in failed. If response is null the user canceled the
//                // sign-in flow using the back button. Otherwise check
//                // response.getError().getErrorCode() and handle the error.
//                // ...
//            }
//        }
//    }
    //#endregion


    private fun pickTag(tagToSet: Int) {
        MyDebug.LOG.debug("BookLibrary::pickTag($tagToSet) started")
        val ctx = this
        doAsync {
            val tglist =
                BookLibApplication.instance.getLibManager().getTags().stream().sorted().collect(Collectors.toList())
            //tglist.remove(EBook.CURRENTLY_READING)

            uiThread {
                val dialog = Dialog(ctx)
                dialog.setContentView(R.layout.tag_list_picker_dialog)
                dialog.setTitle("Pick an EBook tag")
                val tagLstPickerList = dialog.findViewById<View>(R.id.tag_list_picker_list) as ListView
                val adapter = ArrayAdapter(ctx, android.R.layout.simple_list_item_1, android.R.id.text1, tglist)

                // set the custom dialog components - text, image and button
                tagLstPickerList.adapter = adapter

                tagLstPickerList.onItemClickListener =
                    AdapterView.OnItemClickListener { parent, view, position, id ->
                        val selectedFromList = tglist[position]
                        when (tagToSet) {
                            1 -> {
                                bookListTag1Title?.text = selectedFromList
                                savePreferences()
                                updateBookListTag1()
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

        val dialog = FilePickerDialog(this@BookLibrary, properties)
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

    private fun savePreferences() {
        val settings = getSharedPreferences("BookLibApplication", 0)
        val editor = settings.edit()
        editor.putString("bookListTag1Title", bookListTag1Title?.text.toString())
        editor.putBoolean("bookListTag1IncludeSubTags", bookListTag1IncludeSubTags)
        editor.apply()
    }

    private fun updateBookListCurrReading() {
        MyDebug.LOG.debug("BookLibrary::updateBookListCurrReading()")
        doAsync {
            val bklist = BookLibApplication.instance.getLibManager()
                .getBooksForTag(EBook.TAG_CURRENTLY_READING)
            bookListAdaptorCurrReading = BookListItemAdaptor(bklist)
            uiThread {
                bookListCurrentReading?.adapter = bookListAdaptorCurrReading
                setupShortcuts()
            }
        }
    }

    private fun updateBookListTag1() {
        MyDebug.LOG.debug("BookLibrary::updateBookListTag1()")
        if (bookListTag1Title?.text.toString().compareTo(NO_TAG_SELECTED) != 0) {
            doAsync {
                val bklist = BookLibApplication.instance.getLibManager()
                    .getBooksForTag(bookListTag1Title?.text.toString())
                bookListAdaptorTag1 = BookListItemAdaptor(bklist)
                uiThread {
                    bookListTag1?.adapter = bookListAdaptorTag1
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        MyDebug.LOG.debug("onStart()")
        // Check if user is signed in (non-null) and update UI accordingly.
//        checkFirebaseUser()

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
                doAsync {
                    BookLibApplication.instance.getLibManager().checkDb(
                        mHandler,
                        mHandlerScanningNotification
                    )
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

    override fun onPause() {
        super.onPause()
        MyDebug.LOG.debug("onPause()")
    }

    override fun onResume() {
        super.onResume()
        MyDebug.LOG.debug("onResume()")
        val settings = getSharedPreferences("BookLibApplication", 0)
        bookListTag1Title?.text = settings.getString("bookListTag1Title", NO_TAG_SELECTED)
        bookListTag1IncludeSubTags = settings.getBoolean("bookListTag1IncludeSubTags", true)
        updateLists()
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
            scheduleNextLibraryScan()
        }
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
        updateInfoBar()
        updateBookListCurrReading()
        updateBookListTag1()
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
            fab?.isEnabled = true
            progressBarContainer?.visibility = View.INVISIBLE
        }
    }

}
