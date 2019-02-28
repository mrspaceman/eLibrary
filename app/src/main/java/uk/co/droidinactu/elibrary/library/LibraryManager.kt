package uk.co.droidinactu.elibrary.library

import android.app.Application
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Handler
import androidx.core.app.NotificationCompat
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.error
import uk.co.droidinactu.elibrary.BookLibApplication
import uk.co.droidinactu.elibrary.BookLibrary
import uk.co.droidinactu.elibrary.MyDebug
import uk.co.droidinactu.elibrary.R
import uk.co.droidinactu.elibrary.room.*
import java.io.File


class LibraryManager : AnkoLogger {
    constructor(bookLibApplication: Application)

    private var scanLibTask: LibraryScanTask? = null
    private var scanningInProgress = false
    private var mNotificationManager: NotificationManager? = null
    private var mNotificationBuilder: NotificationCompat.Builder? = null
    private val mNotificationId = 1
    private var ctx: Context


    /** EBook CRUB */

    fun getOpenIntentForBook(pEbk: EBook, pSelectedFileType: String): Intent? {
        var intent: Intent? = null
        if (pSelectedFileType == "epub") {
            if (File(pEbk.fullFileDirName + ".epub").exists()) {
                val ebkURI = Uri.parse("file://" + pEbk.fullFileDirName + ".epub")
                //final Uri ebkURI = FileProvider.getUriForFile(ctx, ctx.getApplicationContext().getPackageName() + ".provider", new FileTreeNode(pEbk.getFull_file_dir_name() + ".epub"));
                intent = Intent()
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                intent.action = Intent.ACTION_VIEW
                intent.setDataAndType(ebkURI, "application/epub+zip")
            } else {
                error(LOG_TAG + "EBook FileTreeNode Not Found so remove from library [" + pEbk.fullFileDirName + "]")
                //FIXME:     BookLibApplication.getInstance().getLibManager().removeBook(pEbk)
            }
        } else {
            if (File(pEbk.fullFileDirName + ".pdf").exists()) {
                val ebkURI = Uri.parse("file://" + pEbk.fullFileDirName + ".pdf")
                //final Uri ebkURI = FileProvider.getUriForFile(ctx, ctx.getApplicationContext().getPackageName() + ".provider", new FileTreeNode(pEbk.getFull_file_dir_name() + ".pdf"));
                intent = Intent()
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                intent.action = Intent.ACTION_VIEW
                intent.setDataAndType(ebkURI, "application/pdf")
            } else {
                error(LOG_TAG + "EBook FileTreeNode Not Found so remove from library [" + pEbk.fullFileDirName + "]")
                //FIXME:  BookLibApplication.getInstance().getLibManager().removeBook(pEbk)
            }
        }
        return intent
    }

    fun getBooks(): List<EBook> {
        return ArrayList<EBook>()
    }

    fun getBookTags(): List<BookTag> {
        return ArrayList<BookTag>()
    }

    fun addTagToBook(currentlY_READING: String, ebk: EBook?) {

    }

    fun getFileTypesForBook(ebk: EBook?): List<FileType> {

    }

    fun searchBooksMatching(toString: String): MutableList<EBook> {

    }

    fun reReadEBookMetadata(ebkPath: String) {
        //FIXME :
    }


    /** Author CRUD */

    fun addAuthor(pFirstname: String, pLastname: String): Author? {
        return null
    }

    fun getAuthors(): List<Author> {
        return ArrayList<Author>()
    }


    /** Tag CRUD */


    public void addEbookTag(final EBookTags ebktag) {
        try {
            dbHelper.getEBookTagsDao().create(ebktag);
        } catch (java.sql.SQLException pE) {
            BookLibApplication.getInstance().e("Exception adding ebooktag " + ebktag, pE);
        }
    }

    public BookTag addTag(final String tagstr) {
        BookTag t = getTag(tagstr);
        if (t == null) {
            try {
                dbHelper.getTagDao().create(new BookTag(tagstr));
            } catch (java.sql.SQLException pE) {
                BookLibApplication.getInstance().e("Exception adding [" + tagstr + "] tag to db", pE);
            }
        }
        return getTag(tagstr);
    }

    public List<String> getTagList() {
        List<BookTag> bookTags = getTags();
        List<String> tagStrs = new ArrayList<String>();
        String[] tgLst = new String[bookTags.size()];
        for (BookTag t : bookTags) {
            tagStrs.add(t.getTag());
        }
        return tagStrs;
    }

    public TagTree getTagTree() {
        List<BookTag> bookTags = getTags();
        TagTree tagTree = new TagTree();
        String[] tgLst = new String[bookTags.size()];
        for (BookTag t : bookTags) {
            tagTree.add(t);
        }
        return tagTree;
    }

    public List<BookTag> getTags() {
        List<BookTag> result = new ArrayList<>();
        try {
            result = dbHelper.getTagDao().query(
                    dbHelper.getTagDao()
                            .queryBuilder()
                            .orderByRaw("tag COLLATE NOCASE")
                            .prepare());
        } catch (java.sql.SQLException pE) {
            BookLibApplication.getInstance().e("Exception reading bookTags", pE);
        }
        return result;
    }

    public List<BookTag> getTagsForBook(final EBook ebk) {
        List<BookTag> result = new ArrayList<>();
        try {
            result = lookupTagsForEBook(ebk);
        } catch (SQLException pE) {
            pE.printStackTrace();
        }
        return result;
    }

    private List<BookTag> lookupTagsForEBook(final EBook ebk) throws SQLException {
        if (tagsForEBookQuery == null) {
            QueryBuilder<EBookTags, Long> dbQryBld = dbHelper.getEBookTagsDao().queryBuilder();

            // this time selecting for the user-id field
            dbQryBld.selectColumns(EBookTags.COLUMN_NAME_TAG_ID);
            SelectArg postSelectArg = new SelectArg();
            dbQryBld.where().eq(EBookTags.COLUMN_NAME_BOOK_ID, postSelectArg);

            // build our outer query
            QueryBuilder<BookTag, Long> userQb = dbHelper.getTagDao().queryBuilder();
            // where the user-id matches the inner query's user-id field
            userQb.where().in(BookTag.COLUMN_NAME_ID, dbQryBld);
            tagsForEBookQuery = userQb.prepare();
        }
        tagsForEBookQuery.setArgumentHolderValue(0, ebk);
        return dbHelper.getTagDao().query(tagsForEBookQuery);
    }



    fun open() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun close() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    fun clear() {
        debug(LOG_TAG + "clear() ()")
        for (libname in getLibraryList()) {
            val intent = Intent(
                (ctx.getApplicationContext() as BookLibApplication).applicationContext,
                FileObserverService::class.java
            )
            intent.putExtra("file_obs_action", "del")
            intent.putExtra("libname", libname)
            (ctx.getApplicationContext() as BookLibApplication).applicationContext.startService(intent)
        }
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
        var result: List<Library> = java.util.ArrayList<Library>()
        try {
            result = dbHelper.getLibraryDao().queryForAll()
        } catch (pE: java.sql.SQLException) {
            error("Exception reading libraries", pE)
        }

        return result
    }

    fun refreshLibraries(prgBrHandler: Handler, handler: Handler) {
        if (!scanningInProgress) {
            scanningInProgress = true
            val aList = getLibraries()
            for (lib in aList) {
                if (MyDebug.DEBUGGING) {
                    //                    BookLibApplication.d(LOG_TAG + "Scanning library [" + lib.getLibrary_title()
                    //                            + "] before has [" + getBooksForLibrary(lib.getLibrary_title()).size() + "] ebooks");
                }
                initiateScan(prgBrHandler, handler, lib)
            }
        }
    }

    fun initiateScan(prgBrHandler: Handler, handler: Handler, lib: Library) {
        if (MyDebug.DEBUGGING) {
            debug(LOG_TAG + "Scanning library [" + lib.libraryTitle + "]")
        }
        if (scanLibTask == null || scanLibTask.taskComplete) {
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
            dbHelper.getLibraryDao().create(l)
        } catch (pE: java.sql.SQLException) {
            error("Exception adding library", pE)
        }

        initiateScan(prgBrHandler, handler, l)
    }

    fun addBookToLibrary(libname: String, ebk: EBook) {
        ebk.inLibrary = libname
        try {
            dbHelper.getEBookDao().createOrUpdate(ebk)
            for (t in ebk.getBookTags()) {
                // FIXME : add bookTags
                t = this.getTag(t.getTag())
            }
            // FIXME : add authors
            // FIXME : add link objects
        } catch (pE: java.sql.SQLException) {
            error("Exception adding book to library", pE)
        }

    }


    companion object {
        val LOG_TAG = LibraryManager::class.java.simpleName + ":"
        val DB_NAME = "books-db"
        val DB_NAME_ENC = "books-db-encrypted"
        val CHANNEL_ID = "ScanningChannel"
    }


    private inner class LibraryScanTask : AsyncTask<Any, Void, Void>() {
        private lateinit var prgBrHandler: Handler
        private lateinit var msgHndlr: Handler
        private lateinit var libTitle: String
        private lateinit var libRootDir: String
        private val m_libraryScanner = LibraryScanner()
        var taskComplete: Boolean = false

        override fun doInBackground(vararg params: Any): Void? {
            taskComplete = false
            prgBrHandler = params[0] as Handler
            msgHndlr = params[1] as Handler
            libRootDir = params[2] as String
            libTitle = params[3] as String
            displayScanningNotification(libTitle)
            Thread.currentThread().name = "LibraryScanTask:" + libTitle!!
            m_libraryScanner.readFiles(ctx, prgBrHandler, libTitle, libRootDir)
            return null
        }

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun onPostExecute(aVoid: Void) {
            super.onPostExecute(aVoid)
            if (msgHndlr != null) {
                val completeMessage = msgHndlr!!.obtainMessage(64, libTitle)
                completeMessage.sendToTarget()
            }
            BookLibApplication.copyDbFileToSd(LibraryManager.DB_NAME)
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
        val resultIntent = Intent(ctx, BookLibrary::class.java)
        val resultPendingIntent = PendingIntent.getActivity(ctx, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        mNotificationBuilder = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Scanning Library")
            .setContentText("Scan library $libname for pdf and epub books")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(resultPendingIntent)
            .setProgress(0, 0, true)
        val notification = mNotificationBuilder?.build()

        mNotificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager?.notify(mNotificationId, notification)
    }

    private fun removeScanningNotification(libname: String) {
        mNotificationManager.cancel(mNotificationId)
    }


}
