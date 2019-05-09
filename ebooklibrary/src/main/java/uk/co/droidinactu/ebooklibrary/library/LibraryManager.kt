package uk.co.droidinactu.elibrary.library

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Handler
import android.widget.Toast
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import uk.co.droidinactu.elibrary.MyDebug
import uk.co.droidinactu.elibrary.room.*
import java.io.File
import java.util.*

class LibraryManager {

    //region definitions
    private var scanningInProgress = false
    private var scanLibTask: LibraryScanTask? = null
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

    fun checkDb(msgHndlr: Handler, scanNotificationHndlr: Handler) {
        MyDebug.LOG.debug("checkDb()")
        var completeMessage = scanNotificationHndlr.obtainMessage(64, "startdbcheck")
        completeMessage.sendToTarget()
        doAsync {
            runBlocking {
                val job1 = GlobalScope.launch { checkDbTagDeDupe() }
                val job2 = GlobalScope.launch { checkDbRemoveMissingBooks() }

                job1.join()
                job2.join()

                completeMessage = scanNotificationHndlr.obtainMessage(64, "stopdbcheck")
                completeMessage.sendToTarget()
            }
        }
    }

    fun clear() {
        MyDebug.LOG.debug("clear()")
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

    fun getAsJson(): String {

        // get books
        // add tags to books
        // add authors to books
        // export books as json

        return "{}"
    }

    /** EBook CRUB */
    //region ebooks
    fun addBookToLibrary(libname: String, ebk: EBook): EBook {
        ebk.inLibraryRowId = libraryDao.getByName(libname).getUniqueId()
        try {
            val newId = ebookDao.insert(ebk)
        } catch (pE: java.sql.SQLException) {
            MyDebug.LOG.error("Exception adding book to library", pE)
        }
        return ebookDao.getBookFromFullFilename(ebk.fullFileDirName)
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
        return ebookDao.getCount(lib.getUniqueId())
    }

    fun getBooks(): MutableList<EBook> {
        return ebookDao.getAll()
    }

    private fun getBooksForLibrary(lib: Library): MutableList<EBook> {
        return ebookDao.getAllInLibrary(lib.getUniqueId())
    }

    fun getBooksForTag(tagStr: String): MutableList<EBook> {
        return ebookDao.getAllForTag(tagDao.getTag(tagStr).getUniqueId())
    }

    fun getBooksForTag(tag: Tag): MutableList<EBook> {
        return ebookDao.getAllForTag(tag.getUniqueId())
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
        return ebookAuthorDao.getAuthorsForBook(ebk.getUniqueId())
    }
    //endregion

    /** Tag CRUD */
    //region tags
    fun addTagToBook(newTag: String, ebk: EBook?) {
        if (ebk != null) {
            val t = addTag(newTag, null)
            var tagBookLink = getEbookTagLink(ebk.getUniqueId(), t.getUniqueId())
            if (tagBookLink == null) {
                tagBookLink = EBookTagLink()
                tagBookLink.ebookId = ebk.getUniqueId()
                tagBookLink.tagId = t.getUniqueId()
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
            try {
                val retVal = ebookTagDao.insert(ebkTg)
                //        MyDebug.LOG.debug("LibraryManager::addEbookTagLink() :" + retVal)
            } catch (e: Exception) {
                //         MyDebug.LOG.error("LibraryManager::addEbookTagLink() :", e)
            }
        }
    }

    fun getEbookTagLink(ebkTg: EBookTagLink) {
        val t = getEbookTagLink(ebkTg.ebookId, ebkTg.tagId)
        if (t == null) {
            addEbookTagLink(ebkTg)
        }
    }

    private fun getEbookTagLink(ebookId: Int, tagId: Int): EBookTagLink? {
        return ebookTagDao.getBookTagLink(ebookId, tagId)
    }

    fun addTag(tagstr: String, parentTag: Tag?): Tag {
        var t: Tag?
        if (parentTag != null) {
            t = tagDao.getTag(tagstr, parentTag.id)
        }
        t = tagDao.getTag(tagstr)
        if (t == null) {
            t = Tag(tagstr)
            t.parentTagId= parentTag?.id
            val newId = tagDao.insert(t)
            t.setUniqueId(newId)
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

    fun getTagsUnique(): List<Tag> {
        return tagDao.getAllUnique()
    }

    fun deleteTag(tag: Tag) {
        tagDao.delete(tag)
    }

    fun getTagsForBook(ebk: EBook): List<Tag> {
        return ebookTagDao.getTagsForBook(ebk.getUniqueId())
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
        aList.stream().forEach { t -> strLst.add(t.libraryTitle) }
        return strLst
    }

    private fun getLibraries(): List<Library> {
        try {
            return libraryDao.getAll()
        } catch (e: Exception) {
            MyDebug.LOG.error("Exception getting libraries: ", e)
        }
        return mutableListOf()
    }

    fun refreshLibraries(prgBrHandler: Handler?, handler: Handler?, scanNotificationHndlr: Handler) {
        MyDebug.LOG.debug("BookLibrary::refreshLibraries()")
        if (!scanningInProgress) {
            scanningInProgress = true
            doAsync {
                val aList = getLibraries()
                if (aList.isEmpty()) {
                    MyDebug.LOG.debug("BookLibrary::No Libraries to scan")
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
                        MyDebug.LOG.debug(
                            LOG_TAG,
                            "Scanning library [${lib.libraryTitle}] before has [${getBookCount(lib)}] ebooks"
                        )
                        initiateScan(prgBrHandler, handler, scanNotificationHndlr, lib)
                    }
                }
            }
        }
    }
    //endregion

    /** EBook Scanning */
    //region scanning
    private fun initiateScan(
        prgBrHandler: Handler?,
        msgHndlr: Handler?,
        scanNotificationHndlr: Handler,
        lib: Library
    ) {
        if (scanLibTask == null || scanLibTask?.taskComplete == true) {
            scanLibTask = LibraryScanTask()
            scanLibTask?.execute(prgBrHandler, msgHndlr, scanNotificationHndlr, lib.libraryRootDir, lib.libraryTitle)
        }
    }

    private fun isScanningInProgress(): Boolean {
        return scanningInProgress
    }

    private fun setScanningInProgress(scanningInProgress: Boolean) {
        this.scanningInProgress = scanningInProgress
    }

    fun addLibrary(
        prgBrHandler: Handler,
        handler: Handler,
        scanNotificationHndlr: Handler,
        libname: String,
        rootDir: String
    ) {
        doAsync {
            val l = Library()
            l.libraryTitle = libname.trim { it <= ' ' }
            l.libraryRootDir = rootDir.trim { it <= ' ' }
            try {
                libraryDao.insert(l)
            } catch (pE: java.sql.SQLException) {
                MyDebug.LOG.error("Exception adding library", pE)
            } catch (pE: Exception) {
                MyDebug.LOG.error("Exception adding library", pE)
            }
            initiateScan(prgBrHandler, handler, scanNotificationHndlr, l)
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class LibraryScanTask : AsyncTask<Any, Void, Void>() {
        private lateinit var prgBrHandler: Handler
        private lateinit var msgHndlr: Handler
        private lateinit var scanNotificationHndlr: Handler
        private lateinit var libTitle: String
        private lateinit var libRootDir: String
        private val libraryScanner = LibraryScanner()
        var taskComplete: Boolean = false

        override fun doInBackground(vararg params: Any): Void? {
            MyDebug.LOG.debug("LibraryScanTask::doInBackground(" + params[3] as String + ") started")
            taskComplete = false
            prgBrHandler = params[0] as Handler
            msgHndlr = params[1] as Handler
            scanNotificationHndlr = params[2] as Handler
            libRootDir = params[3] as String
            libTitle = params[4] as String
            Thread.currentThread().name = "LibraryScanTask:$libTitle"
            val completeMessage = scanNotificationHndlr.obtainMessage(64, "startscanning:$libTitle")
            completeMessage.sendToTarget()
            libraryScanner.scanLibraryForEbooks(
                ctx,
                prgBrHandler,
                libTitle,
                libRootDir
            )
            return null
        }

        override fun onPostExecute(aVoid: Void) {
            MyDebug.LOG.debug("LibraryScanTask::onPostExecute() started")
            super.onPostExecute(aVoid)
            if (msgHndlr != null) {
                val completeMessage = msgHndlr.obtainMessage(64, libTitle)
                completeMessage.sendToTarget()
            }
            //     BookLibApplication.instance.copyDbFileToSd(LibraryManager.DB_NAME)
            taskComplete = true
            val completeMessage = scanNotificationHndlr.obtainMessage(64, "stopscanning")
            completeMessage.sendToTarget()
            checkDb(msgHndlr, scanNotificationHndlr)
        }
    }

    private fun checkDbTagDeDupe() {
        MyDebug.LOG.debug("checkDbTagDeDupe()")
        // Get all tags for library

        // for each tag (t1)
        //fetch all duplicate named tags

        // for each duplicate tag (t2)
        // get books for tag (t2)
        // change link (t2) to point to tag (t1)
        // remove duplicate tag (t2)

        var allTags = getTagsUnique()


        allTags = getTags()
        for (t in allTags) {
            val booksForTag = getBooksForTag(t)
            if (booksForTag.size < 3) {
                deleteTag(t)
            }
        }
    }

    private fun checkDbRemoveMissingBooks() {
        MyDebug.LOG.debug("checkDbRemoveMissingBooks()")
        //      val completeMessage = scanNotificationHndlr.obtainMessage(64, "startdbcheck")
        //      completeMessage.sendToTarget()

        val allBooks = getBooks()
        for (ebk in allBooks) {
            for (ft in ebk.filetypes) {
                if (File(ebk.fullFileDirName + "." + ft).exists()) {
                    // file exists so we leave it in the library
                } else {
                    MyDebug.LOG.error(
                        LOG_TAG,
                        "EBook FileTreeNode Not Found so removed from library [" + ebk.fullFileDirName + "]"
                    )
                    //FIXME : delete book author links
                    //FIXME : delete book tag links
                    //FIXME : delete book from db
                }
            }
        }
    }

    //endregion

    companion object {
        const val LOG_TAG = "BookLibApplication"
        const val DB_NAME = "books-db.sqlite"
        const val DB_NAME_ENC = "books-db-encrypted"
        const val CHANNEL_ID = "ScanningChannel"
    }
}
