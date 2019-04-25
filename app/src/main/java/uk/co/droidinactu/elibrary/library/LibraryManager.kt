package uk.co.droidinactu.elibrary.library

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
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
    private lateinit var ctx: Context
    //endregion

    fun open(contex: Context) {
        ctx = contex
        ebookDao = EBookRoomDatabase.getInstance(ctx)!!.ebookDao()
        ebookAuthorDao = EBookRoomDatabase.getInstance(ctx)!!.ebookAuthorLinkDao()
        ebookTagDao = EBookRoomDatabase.getInstance(ctx)!!.ebookTagLinkDao()
        authorDao = EBookRoomDatabase.getInstance(ctx)!!.authorDao()
        tagDao = EBookRoomDatabase.getInstance(ctx)!!.tagDao()
        libraryDao = EBookRoomDatabase.getInstance(ctx)!!.libraryDao()
    }

    fun close() {
        EBookRoomDatabase.getInstance(ctx)!!.close()
    }

    fun clear() {
        Log.d(LOG_TAG, "clear()")
        for (libname in getLibraryList()) {
            val intent = Intent(
                ctx,
                FileObserverService::class.java
            )
            intent.putExtra("file_obs_action", "del")
            intent.putExtra("libname", libname)
            ctx.startService(intent)
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
        return getBook(fullFilename, getLibraries()[0].libraryTitle)
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

    private fun getBook(fullFilename: String, libname: String): EBook {
        var ebk = ebookDao.getBookFromFullFilename(fullFilename)
        if (ebk == null) {
            ebk = EBook()
            ebk.fullFileDirName = fullFilename
            addBookToLibrary(libname, ebk)
            ebk = ebookDao.getBookFromFilename(fullFilename)
        }
        return ebk
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

    fun getBooksForTag(tag: Tag, subtags: Boolean): MutableList<EBook> {
        return ebookDao.getAllForTag(tag.id)
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
            val a = Author()
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

    fun getAuthorsForBook(ebk: EBook): List<Author> {
        return ebookAuthorDao.getAuthorsForBook(ebk.id)
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

    private fun getEbookTagLink(ebookId: Long, tagId: Long): EBookTagLink? {
        return ebookTagDao.getBookTagLink(ebookId, tagId)
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

    fun updateTag(t: Tag) {
        tagDao.update(t)
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

    fun deleteTag(tag: Tag) {
        tagDao.delete(tag)
    }

    fun getTagsForBook(ebk: EBook): List<Tag> {
        return ebookTagDao.getTagsForBook(ebk.id)
    }

    //endregion

    /** Library CRUD */
    //region library
    fun getLibrary(): Library {
        return getLibraries()[0]
    }

    fun getLibrary(libname: String): Library {
        return libraryDao.getByName(libname)
    }

    private fun getLibraryList(): List<String> {
        val aList = getLibraries()
        val strLst = ArrayList<String>()
        aList.stream().forEach { t -> { strLst.add(t.libraryTitle) } }
        return strLst
    }

    private fun getLibraries(): List<Library> {
        try {
            return libraryDao.getAll()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Exception getting libraries: ", e)
        }
        return mutableListOf()
    }

    fun refreshLibraries(prgBrHandler: Handler?, handler: Handler?) {
        Log.d(LOG_TAG, "BookLibrary::refreshLibraries()")
        if (!scanningInProgress) {
            scanningInProgress = true
            doAsync {
                val aList = getLibraries()
                if (aList.isEmpty()) {
                    Log.d(LOG_TAG, "BookLibrary::No Libraries to scan")
                    val completeMessage = handler!!.obtainMessage(64, "")
                    completeMessage.sendToTarget()
                    scanningInProgress = false

                    val text = "No Libraries to Scan!"
                    val duration = Toast.LENGTH_SHORT
                    uiThread {
                        val toast = Toast.makeText(ctx, text, duration)
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
    private fun initiateScan(prgBrHandler: Handler?, handler: Handler?, lib: Library) {
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

    @SuppressLint("StaticFieldLeak")
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
            Thread.currentThread().name = "LibraryScanTask:$libTitle"
            libraryScanner.readFiles(
                ctx,
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

    @SuppressLint("StaticFieldLeak")
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

            val allBooks = getBooks()

            for (ebk in allBooks) {
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

            val allTags = getTags()
            for (t in allTags) {
                val booksForTag = getBooksForTag(t, true)
                if (booksForTag.size < 3) {
                    deleteTag(t)
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
//        val resultIntent = Intent(ctx, BookLibrary::class.java)
//        val resultPendingIntent = PendingIntent.getActivity(
//            ctx,
//            0,
//            resultIntent,
//            PendingIntent.FLAG_UPDATE_CURRENT
//        )

        mNotificationBuilder = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Scanning Library")
            .setContentText("Scan library $libname for pdf and epub books")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            //   .setContentIntent(resultPendingIntent)
            .setProgress(0, 0, true)
        val notification = mNotificationBuilder?.build()

        mNotificationManager =
            ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager?.notify(mNotificationId, notification)
    }

    private fun removeScanningNotification(libname: String) {
        mNotificationManager?.cancel(mNotificationId)
    }

    //endregion

    companion object {
        const val LOG_TAG = "BookLibApplication"
        const val DB_NAME = "books-db.sqlite"
        const val DB_NAME_ENC = "books-db-encrypted"
        const val CHANNEL_ID = "ScanningChannel"
    }
}
