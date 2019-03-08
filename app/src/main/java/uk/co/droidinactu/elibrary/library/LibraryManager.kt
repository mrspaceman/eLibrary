package uk.co.droidinactu.elibrary.library

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Handler
import android.util.Log
import androidx.core.app.NotificationCompat
import org.jetbrains.anko.doAsync
import uk.co.droidinactu.elibrary.BookLibApplication
import uk.co.droidinactu.elibrary.BookLibApplication.Companion.LOG_TAG
import uk.co.droidinactu.elibrary.BookLibrary
import uk.co.droidinactu.elibrary.MyDebug
import uk.co.droidinactu.elibrary.R
import uk.co.droidinactu.elibrary.room.*
import java.io.File

class LibraryManager {

    private var scanLibTask: LibraryScanTask? = null
    private var scanningInProgress = false
    private var mNotificationManager: NotificationManager? = null
    private var mNotificationBuilder: NotificationCompat.Builder? = null
    private val mNotificationId = 1

    private lateinit var ebookDao: EBookDao
    private lateinit var ebookAuthorDao: EBookAuthorLinkDao
    private lateinit var ebookTagDao: EBookTagLinkDao
    private lateinit var authorDao: AuthorDao
    private lateinit var tagDao: TagDao
    private lateinit var libraryDao: LibraryDao


    fun open() {
        val ctx = BookLibApplication.instance.applicationContext
        ebookDao = EBookRoomDatabase.getInstance(ctx)!!.ebookDao()
        ebookAuthorDao = EBookRoomDatabase.getInstance(ctx)!!.ebookAuthorLinkDao()
        ebookTagDao = EBookRoomDatabase.getInstance(ctx)!!.ebookTagLinkDao()
        authorDao = EBookRoomDatabase.getInstance(ctx)!!.authorDao()
        tagDao = EBookRoomDatabase.getInstance(ctx)!!.tagDao()
        libraryDao = EBookRoomDatabase.getInstance(ctx)!!.libraryDao()
    }

    fun close() {
        EBookRoomDatabase.getInstance(BookLibApplication.instance.applicationContext)!!.close()
    }

    fun clear() {
        Log.d(LOG_TAG, "clear()")
        for (libname in getLibraryList()) {
            val intent = Intent(
                BookLibApplication.instance.applicationContext,
                FileObserverService::class.java
            )
            intent.putExtra("file_obs_action", "del")
            intent.putExtra("libname", libname)
            BookLibApplication.instance.applicationContext.startService(intent)
        }
    }

    /** EBook CRUB */

    fun getOpenIntentForBook(pEbk: EBook, pSelectedFileType: String): Intent? {
        var intent: Intent? = null
        if (pSelectedFileType == "epub") {
            if (File(pEbk.fullFileDirName + ".epub").exists()) {
                val ebkURI = Uri.parse("file://" + pEbk.fullFileDirName + ".epub")
                //final Uri ebkURI = FileProvider.getUriForFile(BookLibApplication.instance.applicationContext, BookLibApplication.applicationContext.getApplicationContext().getPackageName() + ".provider", new FileTreeNode(pEbk.getFull_file_dir_name() + ".epub"));
                intent = Intent()
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                intent.action = Intent.ACTION_VIEW
                intent.setDataAndType(ebkURI, "application/epub+zip")
            } else {
                error(LOG_TAG + "EBook FileTreeNode Not Found so remove from library [" + pEbk.fullFileDirName + "]")
                //FIXME:     BookLibApplication.instance.getLibManager().removeBook(pEbk)
            }
        } else {
            if (File(pEbk.fullFileDirName + ".pdf").exists()) {
                val ebkURI = Uri.parse("file://" + pEbk.fullFileDirName + ".pdf")
                //final Uri ebkURI = FileProvider.getUriForFile(BookLibApplication.instance.applicationContext, BookLibApplication.applicationContext.getApplicationContext().getPackageName() + ".provider", new FileTreeNode(pEbk.getFull_file_dir_name() + ".pdf"));
                intent = Intent()
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                intent.action = Intent.ACTION_VIEW
                intent.setDataAndType(ebkURI, "application/pdf")
            } else {
                error(LOG_TAG + "EBook FileTreeNode Not Found so remove from library [" + pEbk.fullFileDirName + "]")
                //FIXME:  BookLibApplication.instance.getLibManager().removeBook(pEbk)
            }
        }
        return intent
    }

    fun getBooks(): MutableList<EBook> {
        return ebookDao.getAll()
    }

    private fun getBooksForLibrary(lib: Library): MutableList<EBook> {
        return ebookDao.getAllInLibrary(lib.id)
    }

    fun getBooksForTag(tagStr: String, subtags: Boolean): MutableList<EBook> {
        return ebookDao.getAllForTag(tagStr)
    }


    fun searchBooksMatching(titleStr: String): MutableList<EBook> {
        return ebookDao.getAllWithTitle(titleStr)
    }

    fun reReadEBookMetadata(ebkPath: String) {
        //FIXME :
    }


    /** Author CRUD */

    fun addAuthor(pFirstname: String, pLastname: String): Author? {
        var t = authorDao.getByName(pFirstname, pLastname)
        if (t == null) {
            authorDao.insert(Author(pFirstname, pLastname))
        }
        return authorDao.getByName(pFirstname, pLastname)
    }

    fun getAuthors(): List<Author> {
        return authorDao.getAll()
    }


    /** Tag CRUD */

    fun addTagToBook(newTag: String, ebk: EBook?) {
        if (ebk != null) {
            var t = getTag(newTag)
            var tagBookLink = EBookTagLink(ebk.id, t.id)
            ebookTagDao.insert(tagBookLink)
        }
    }

    private fun addTag(tagstr: String): Tag {
        var t = tagDao.getTag(tagstr)
        if (t == null) {
            tagDao.insert(Tag(tagstr))
        }
        return tagDao.getTag(tagstr)
    }

    fun getTag(tagname: String): Tag {
        var t = tagDao.getTag(tagname)
        if (t == null) {
            t = addTag(tagname)
        }
        return t
    }

    fun getTagList(): List<String> {
        var bookTags = getTags()
        var tagStrs = ArrayList<String>()
        for (t in bookTags) {
            tagStrs.add(t.tag)
        }
        return tagStrs
    }

//    fun getTagTree(): TagTree {
//        var bookTags = getTags()
//        var tagTree = TagTree()
//        String[] tgLst = new String[bookTags.size()]
//        for (BookTag t : bookTags) {
//            tagTree.add(t)
//        }
//        return tagTree
//    }

    fun getTags(): List<Tag> {
        return tagDao.getAll()
    }

    fun getTagsForBook(ebk: EBook): List<Tag> {
        var result = lookupTagsForEBook(ebk)
        return result
    }

    fun lookupTagsForEBook(ebk: EBook): List<Tag> {
        var result = ArrayList<Tag>();

//        if (tagsForEBookQuery == null) {
//            QueryBuilder<EBookTags, Long> dbQryBld = dbHelper . getEBookTagsDao ().queryBuilder();
//
//            // this time selecting for the user-id field
//            dbQryBld.selectColumns(EBookTags.COLUMN_NAME_TAG_ID)
//            var postSelectArg = new SelectArg ()
//            dbQryBld.where().eq(EBookTags.COLUMN_NAME_BOOK_ID, postSelectArg)
//
//            // build our outer query
//            QueryBuilder<BookTag, Long> userQb = dbHelper . getTagDao ().queryBuilder()
//            // where the user-id matches the inner query's user-id field
//            userQb.where().in(BookTag.COLUMN_NAME_ID, dbQryBld)
//            tagsForEBookQuery = userQb.prepare()
//        }
//        tagsForEBookQuery.setArgumentHolderValue(0, ebk);
//        return dbHelper.getTagDao().query(tagsForEBookQuery)

        return result
    }


    /** Library CRUD */

    fun getLibrary(libname: String): Library {
        var l: Library = libraryDao.getByName(libname)
        return l
    }

    fun getLibraryList(): List<String> {
        val aList = getLibraries()
        val strLst = java.util.ArrayList<String>()
        for (t in aList) {
            strLst.add(t.libraryTitle)
        }
        return strLst
    }

    fun getLibraries(): List<Library> {
        try {
            return libraryDao.getAll()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Exception getting libraries: ", e)
        }
        return ArrayList<Library>()
    }

    fun refreshLibraries(prgBrHandler: Handler?, handler: Handler?) {
        Log.d(LOG_TAG, "BookLibrary::refreshLibraries()")
        if (!scanningInProgress) {
            scanningInProgress = true
            doAsync {
                val aList = getLibraries()
                if (aList.size == 0) {
                    val completeMessage = handler!!.obtainMessage(64, "")
                    completeMessage.sendToTarget()
                } else {
                    for (lib in aList) {
                        if (MyDebug.DEBUGGING) {
                            Log.d(
                                LOG_TAG, "Scanning library [" + lib.libraryTitle
                                        + "] before has ["
                                        + getBooksForLibrary(lib).size
                                        + "] ebooks"
                            )
                        }
                        initiateScan(prgBrHandler, handler, lib)
                    }
                }
            }
        }
    }

    fun initiateScan(prgBrHandler: Handler?, handler: Handler?, lib: Library) {
        Log.d(LOG_TAG, "LibraryManager::initiateScan() Scanning library [" + lib.libraryTitle + "]")
        if (scanLibTask == null || scanLibTask?.taskComplete == true) {
            scanLibTask = LibraryScanTask()
            scanLibTask?.execute(prgBrHandler, handler, lib.libraryRootDir, lib.libraryTitle)
        }
    }

    fun isScanningInProgress(): Boolean {
        return scanningInProgress
    }

    fun setScanningInProgress(scanningInProgress: Boolean) {
        this.scanningInProgress = scanningInProgress
    }

    fun addLibrary(prgBrHandler: Handler, handler: Handler, libname: String, rootDir: String) {
        val l = Library()
        l.libraryTitle = libname.trim { it <= ' ' }
        l.libraryRootDir = rootDir.trim { it <= ' ' }
        try {
            libraryDao.insert(l)
        } catch (pE: java.sql.SQLException) {
            Log.e(LOG_TAG, "Exception adding library", pE)
        }
        initiateScan(prgBrHandler, handler, l)
    }

    fun addBookToLibrary(libname: String, ebk: EBook) {
        ebk.inLibraryId = libraryDao.getByName(libname).id
        try {
            ebookDao.insert(ebk)
            // FIXME : add bookTags
            // FIXME : add authors
            // FIXME : add link objects
        } catch (pE: java.sql.SQLException) {
            Log.e(LOG_TAG, "Exception adding book to library", pE)
        }
    }

    private inner class LibraryScanTask : AsyncTask<Any, Void, Void>() {
        private lateinit var prgBrHandler: Handler
        private lateinit var msgHndlr: Handler
        private lateinit var libTitle: String
        private lateinit var libRootDir: String
        private val libraryScanner = LibraryScanner()
        var taskComplete: Boolean = false

        override fun doInBackground(vararg params: Any): Void? {
            Log.d(LOG_TAG, "LibraryScanTask::doInBackground(" + params[3] as String + ") started")
            taskComplete = false
            prgBrHandler = params[0] as Handler
            msgHndlr = params[1] as Handler
            libRootDir = params[2] as String
            libTitle = params[3] as String
            displayScanningNotification(libTitle)
            Thread.currentThread().name = "LibraryScanTask:" + libTitle!!
            libraryScanner.readFiles(
                BookLibApplication.instance.applicationContext,
                prgBrHandler,
                libTitle,
                libRootDir
            )
            return null
        }

        override fun onPostExecute(aVoid: Void) {
            Log.d(LOG_TAG, "LibraryScanTask::onPostExecute() started")
            super.onPostExecute(aVoid)
            if (msgHndlr != null) {
                val completeMessage = msgHndlr!!.obtainMessage(64, libTitle)
                completeMessage.sendToTarget()
            }
            BookLibApplication.instance.copyDbFileToSd(LibraryManager.DB_NAME)
            taskComplete = true
            LibraryCheckLinksTask().execute(msgHndlr, libRootDir)
        }

    }

    private inner class LibraryCheckLinksTask : AsyncTask<Any, Void, Void>() {
        private var msgHndlr: Handler? = null
        private var libRootDir: String? = null
        private val ls = LibraryScanner()
        private var taskComplete: Boolean = false

        override fun doInBackground(vararg params: Any): Void? {
            taskComplete = false
            msgHndlr = params[0] as Handler
            libRootDir = params[1] as String
            Thread.currentThread().name = "LibraryCheckLinksTask:"

            val result = getBooks()

            for (ebk in result) {
                for (ft in ebk.filetypes) {
                    val selectedFileType = ft
                    if (File(ebk.fullFileDirName + "." + selectedFileType).exists()) {
                        // file exists so we leave it in the library
                    } else {
                        error(LOG_TAG + "EBook FileTreeNode Not Found so remove from library [" + ebk.fullFileDirName + "]")
                        //FIXME : delete book from db
                    }
                }
            }
            return null
        }

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun onPostExecute(aVoid: Void) {
            super.onPostExecute(aVoid)
            if (msgHndlr != null) {
                val completeMessage = msgHndlr!!.obtainMessage(64)
                completeMessage.sendToTarget()
            }
            taskComplete = true
            scanningInProgress = false
            removeScanningNotification("")
        }

        override fun onProgressUpdate(vararg values: Void) {
            super.onProgressUpdate(*values)
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

        mNotificationBuilder = NotificationCompat.Builder(BookLibApplication.instance.applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Scanning Library")
            .setContentText("Scan library $libname for pdf and epub books")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(resultPendingIntent)
            .setProgress(0, 0, true)
        val notification = mNotificationBuilder?.build()

        mNotificationManager =
            BookLibApplication.instance.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager?.notify(mNotificationId, notification)
    }

    private fun removeScanningNotification(libname: String) {
        mNotificationManager?.cancel(mNotificationId)
    }


    companion object {
        val DB_NAME = "books-db"
        val DB_NAME_ENC = "books-db-encrypted"
        val CHANNEL_ID = "ScanningChannel"
    }
}
