package uk.co.droidinactu.elibrary

import android.app.Application
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import uk.co.droidinactu.elibrary.library.RecursiveFileObserver.Companion.CHANGES_ONLY
import uk.co.droidinactu.elibrary.library.RecursiveFileObserver.Companion.LOG_TAG
import uk.co.droidinactu.elibrary.library.LibraryManager
import uk.co.droidinactu.elibrary.library.RecursiveFileObserver
import java.util.*

object BookLibApplication : Application(), AnkoLogger {
    fun getLibManager(): LibraryManager? {
        return libMgr
    }

    fun addFileWatcher(libname: String?, rootdir: String?) {
        if (libname != null && rootdir != null) {
            if (!libraryWatcher.containsKey(libname)) {
                info(LOG_TAG + "Adding file observer for [" + libname + "] @ [" + rootdir + "]")
                val fileOb = RecursiveFileObserver(rootdir, CHANGES_ONLY)
                libraryWatcher[libname] = fileOb
                fileOb.startWatching()
            }
        } else {
            info(LOG_TAG + "Already watching for [" + libname + "] @ [" + rootdir + "]")
        }
    }

    fun delFileWatcher(libname: String) {
        val fileOb = libraryWatcher[libname]
        fileOb?.stopWatching()
        libraryWatcher.remove(libname)
    }


    private var libMgr: LibraryManager? = null

    private val libraryWatcher = HashMap<String, RecursiveFileObserver>()


}