package uk.co.droidinactu.elibrary.library

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Handler
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.NotificationCompat
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import uk.co.droidinactu.elibrary.BookLibApplication
import uk.co.droidinactu.elibrary.BookLibApplication.Companion.LOG_TAG
import uk.co.droidinactu.elibrary.BookLibrary
import uk.co.droidinactu.elibrary.R
import uk.co.droidinactu.elibrary.room.*
import java.io.File

class LibraryManager {

    //region definitions
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
    //endregion

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
        ebookDao.clear()
        ebookAuthorDao.clear()
        ebookTagDao.clear()
        authorDao.clear()
        tagDao.clear()
        libraryDao.clear()
    }

    /** EBook CRUB */
    //region ebooks
    fun addBookToLibrary(libname: String, ebk: EBook) {
        ebk.inLibraryId = libraryDao.getByName(libname).id
        try {
            val newId = ebookDao.insert(ebk)
        } catch (pE: java.sql.SQLException) {
            Log.e(LOG_TAG, "Exception adding book to library", pE)
        }
    }

    fun getBook(fullFilename: String): EBook {
        return getBook(fullFilename, getLibraries().get(0).libraryTitle)
    }

    fun getBookFromFilename(fileName: String): EBook {
        return ebookDao.getBookFromFilename(fileName)
    }

    fun getBookFromFullFilename(fileName: String): EBook {
        return ebookDao.getBookFromFullFilename(fileName)
    }

    fun getBookCalled(bookTitle: String): EBook {
        return ebookDao.getBookCalled(bookTitle)
    }

    fun updateBook(ebk: EBook) {
        ebookDao.update(ebk)
    }

    fun getBook(fullFilename: String, libname: String): EBook {
        var ebk = ebookDao.getBookFromFullFilename(fullFilename)
        if (ebk == null) {
            ebk = EBook()
            ebk.fullFileDirName = fullFilename
            addBookToLibrary(libname, ebk)
            ebk = ebookDao.getBookFromFilename(fullFilename)
        }
        return ebk
    }


    fun getOpenIntentForBook(pEbk: EBook, pSelectedFileType: String): Intent? {
        var openBookIntent: Intent? = null
        if (File(pEbk.fullFileDirName + "." + pSelectedFileType).exists()) {
            openBookIntent = Intent(Intent.ACTION_VIEW)
            openBookIntent.setDataAndType(
                Uri.parse("file://" + pEbk.fullFileDirName + "." + pSelectedFileType),
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(pSelectedFileType)
            )
            openBookIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

        } else {
            Log.e(LOG_TAG, "EBook FileTreeNode Not Found so remove from library [" + pEbk.fullFileDirName + "]")
            //FIXME:     BookLibApplication.instance.getLibManager().removeBook(pEbk)
        }
        return openBookIntent
    }

    fun getBookCount(): Int {
        return ebookDao.getCount()
    }

    private fun getBookCount(lib: Library): Int {
        return ebookDao.getCount(lib.id)
    }

    fun getBooks(): MutableList<EBook> {
        return ebookDao.getAll()
    }

    private fun getBooksForLibrary(lib: Library): MutableList<EBook> {
        return ebookDao.getAllInLibrary(lib.id)
    }

    fun getBooksForTag(tagStr: String, subtags: Boolean): MutableList<EBook> {
        return ebookDao.getAllForTag(tagDao.getTag(tagStr).id)
    }

    fun searchBooksMatching(titleStr: String): MutableList<EBook> {
        return ebookDao.getAllWithTitle(titleStr)
    }

    fun reReadEBookMetadata(ebkPath: String) {
        //FIXME :
    }
    //endregion

    /** Author CRUD */
    // region authors
    fun addAuthor(pFirstname: String, pLastname: String): Author? {
        val t = authorDao.getByName(pFirstname, pLastname)
        if (t == null) {
            var a = Author()
            a.firstname = pFirstname
            a.lastname = pLastname
            val newId = authorDao.insert(a)
        }
        return authorDao.getByName(pFirstname, pLastname)
    }

    fun addAuthor(authr: Author): Author? {
        val t = authorDao.getByName(authr.firstname!!, authr.lastname!!)
        if (t == null) {
            val newId = authorDao.insert(authr)
        }
        return authorDao.getByName(authr.firstname!!, authr.lastname!!)
    }

    fun addEbookAuthorLink(ebkAuth: EBookAuthorLink) {
        val a = ebookTagDao.getBookTagLink(ebkAuth.ebookId, ebkAuth.authorId)
        if (a == null) {
            ebookAuthorDao.insert(ebkAuth)
        }
    }

    fun getAuthors(): List<Author> {
        return authorDao.getAll()
    }
    //endregion

    /** Tag CRUD */
    //region tags
    fun addTagToBook(newTag: String, ebk: EBook?) {
        if (ebk != null) {
            val t = addTag(newTag)
            var tagBookLink = getEbookTagLink(ebk.id, t.id)
            if (tagBookLink == null) {
                tagBookLink = EBookTagLink()
                tagBookLink.ebookId = ebk.id
                tagBookLink.tagId = t.id
                try {
                    val newId = ebookTagDao.insert(tagBookLink)
                } finally {
                }
            }
        }
    }

    fun addEbookTagLink(ebkTg: EBookTagLink) {
        val t = getEbookTagLink(ebkTg.ebookId, ebkTg.tagId)
        if (t == null) {
            ebookTagDao.insert(ebkTg)
        }
    }

    fun getEbookTagLink(ebkTg: EBookTagLink) {
        val t = getEbookTagLink(ebkTg.ebookId, ebkTg.tagId)
        if (t == null) {
            addEbookTagLink(ebkTg)
        }
    }

    fun getEbookTagLink(ebookId: Long, tagId: Long): EBookTagLink? {
        val t = ebookTagDao.getBookTagLink(ebookId, tagId)
        return t
    }

    fun addTag(tagstr: String): Tag {
        var t = tagDao.getTag(tagstr)
        if (t == null) {
            t = Tag()
            t.tag = tagstr
            val newId = tagDao.insert(t)
            t.id = newId
        }
        return t
    }

    fun getTagList(): List<String> {
        val bookTags = getTags()
        val tagStrs = mutableListOf<String>()
        for (t in bookTags) {
            tagStrs.add(t.tag)
        }
        return tagStrs
    }

    fun getTagTree(): TagTree {
        val bookTags = getTags()
        val tagTree = TagTree()
        for (t in bookTags) {
            tagTree.add(t)
        }
        return tagTree
    }

    fun getTags(): List<Tag> {
        return tagDao.getAll()
    }

    fun getTagsForBook(ebk: EBook): List<Tag> {
        return lookupTagsForEBook(ebk)
    }

    fun lookupTagsForEBook(ebk: EBook): List<Tag> {
        val result = mutableListOf<Tag>()

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
    //endregion

    /** Library CRUD */
    //region library
    fun getLibrary(): Library {
        return getLibraries().get(0)
    }

    fun getLibrary(libname: String): Library {
        return libraryDao.getByName(libname)
    }

    fun getLibraryList(): List<String> {
        val aList = getLibraries()
        val strLst = ArrayList<String>()
        aList.stream().forEach { t -> { strLst.add(t.libraryTitle) } }
        return strLst
    }

    fun getLibraries(): List<Library> {
        try {
            return libraryDao.getAll()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Exception getting libraries: ", e)
        }
        return mutableListOf<Library>()
    }

    fun refreshLibraries(prgBrHandler: Handler?, handler: Handler?) {
        Log.d(LOG_TAG, "BookLibrary::refreshLibraries()")
        if (!scanningInProgress) {
            scanningInProgress = true
            doAsync {
                val aList = getLibraries()
                if (aList.size == 0) {
                    Log.d(LOG_TAG, "BookLibrary::No Libraries to scan")
                    val completeMessage = handler!!.obtainMessage(64, "")
                    completeMessage.sendToTarget()
                    scanningInProgress = false

                    val text = "No Libraries to Scan!"
                    val duration = Toast.LENGTH_SHORT
                    uiThread {
                        val toast = Toast.makeText(BookLibApplication.instance.applicationContext, text, duration)
                        toast.show()
                    }
                } else {
                    for (lib in aList) {
                        Log.d(
                            LOG_TAG,
                            "Scanning library [${lib.libraryTitle}] before has [${getBookCount(lib)}] ebooks"
                        )
                        initiateScan(prgBrHandler, handler, lib)
                    }
                }
            }
        }
    }
    //endregion

    /** EBook Scanning */
    //region scanning
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
        doAsync {
            val l = Library()
            l.libraryTitle = libname.trim { it <= ' ' }
            l.libraryRootDir = rootDir.trim { it <= ' ' }
            try {
                libraryDao.insert(l)
            } catch (pE: java.sql.SQLException) {
                Log.e(LOG_TAG, "Exception adding library", pE)
            } catch (pE: Exception) {
                Log.e(LOG_TAG, "Exception adding library", pE)
            }
            initiateScan(prgBrHandler, handler, l)
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
            Thread.currentThread().name = "LibraryScanTask:" + libTitle
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
                val completeMessage = msgHndlr.obtainMessage(64, libTitle)
                completeMessage.sendToTarget()
            }
            //     BookLibApplication.instance.copyDbFileToSd(LibraryManager.DB_NAME)
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
                    if (File(ebk.fullFileDirName + "." + ft).exists()) {
                        // file exists so we leave it in the library
                    } else {
                        Log.e(
                            LOG_TAG,
                            "EBook FileTreeNode Not Found so removed from library [" + ebk.fullFileDirName + "]"
                        )
                        //FIXME : delete book from db
                    }
                }
            }
            return null
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
    //endregion

    companion object {
        val DB_NAME = "books-db.sqlite"
        val DB_NAME_ENC = "books-db-encrypted"
        val CHANNEL_ID = "ScanningChannel"
    }
}
