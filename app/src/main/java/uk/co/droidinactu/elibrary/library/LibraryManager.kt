package uk.co.droidinactu.elibrary.library

import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Handler
import org.jetbrains.anko.AnkoLogger
import uk.co.droidinactu.elibrary.BookLibApplication
import uk.co.droidinactu.elibrary.room.Author
import uk.co.droidinactu.elibrary.room.BookTag
import uk.co.droidinactu.elibrary.room.EBook
import uk.co.droidinactu.elibrary.room.FileType
import java.io.File


class LibraryManager: AnkoLogger{

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


    /** Author CRUD */

    fun addAuthor(pFirstname: String, pLastname: String): Author? {
        return null
    }

    fun getAuthors(): List<Author> {
        return ArrayList<Author>()
    }


    companion object {
        private val LOG_TAG = LibraryManager::class.java.simpleName + ":"
    }


    private inner class LibraryScanTask : AsyncTask<Any, Void, Void>() {
        private var prgBrHandler: Handler? = null
        private var msgHndlr: Handler? = null
        private var libTitle: String? = null
        private var libRootDir: String? = null
        private val m_libraryScanner = LibraryScanner()
        private var taskComplete: Boolean = false

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
            BookLibApplication.getInstance().copyDbFileToSd(LibraryManager.DB_NAME)
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
                for (ft in ebk.getFileTypes()) {
                    val selectedFileType = ft.getFileType()
                    if (File(ebk.getFull_file_dir_name() + "." + selectedFileType).exists()) {
                        // file exists so we leave it in the library
                    } else {
                        error(LOG_TAG + "EBook FileTreeNode Not Found so remove from library [" + ebk.getFull_file_dir_name() + "]")
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


}