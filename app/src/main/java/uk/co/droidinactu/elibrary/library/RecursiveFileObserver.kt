package uk.co.droidinactu.elibrary.library

import android.content.Intent
import android.os.FileObserver
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import uk.co.droidinactu.elibrary.BookLibApplication
import java.io.File
import java.util.*

/**
 * Enhanced FileObserver to support recursive directory monitoring basically.
 *
 * @author uestc.Mobius <mobius></mobius>@toraleap.com>
 * @version 2011.0121
 */
class RecursiveFileObserver @JvmOverloads constructor(
    private val mPath: String,
    private val mMask: Int = FileObserver.ALL_EVENTS
) : FileObserver(mPath, mMask), AnkoLogger {

    private var mObservers: MutableList<SingleFileObserver>? = null

    override fun startWatching() {
        if (mObservers != null) return

        mObservers = ArrayList()
        val stack = Stack<String>()
        stack.push(mPath)

        while (!stack.isEmpty()) {
            val parent = stack.pop()
            mObservers!!.add(SingleFileObserver(parent, mMask))
            val path = File(parent)
            val files = path.listFiles() ?: continue
            for (f in files) {
                if (f.isDirectory && f.name != "." && f.name != "src/main") {
                    stack.push(f.path)
                }
            }
        }

        for (sfo in mObservers!!) {
            sfo.startWatching()
            debug(LOG_TAG + "startWatching() : " + sfo.mPath)
        }
    }

    override fun stopWatching() {
        if (mObservers == null) return

        for (sfo in mObservers!!) {
            sfo.stopWatching()
        }
        mObservers!!.clear()
        mObservers = null
    }


    override fun onEvent(event: Int, path: String?) {
        val localIntent = Intent(INTENT_EBOOK_MODIFIED)
        localIntent.putExtra(INTENT_EBOOK_MODIFIED_PATH, path)
        when (event) {
            FileObserver.ACCESS -> debug(LOG_TAG + "onEvent() ACCESS: " + path)
            FileObserver.ATTRIB -> debug(LOG_TAG + "onEvent() ATTRIB: " + path)
            FileObserver.CLOSE_NOWRITE -> debug(LOG_TAG + "onEvent() CLOSE_NOWRITE: " + path)
            FileObserver.CLOSE_WRITE -> debug(LOG_TAG + "onEvent() CLOSE_WRITE: " + path)
            FileObserver.CREATE -> {
                debug(LOG_TAG + "onEvent() CREATE: " + path)
                BookLibApplication.sendBroadcast(localIntent)
            }
            FileObserver.DELETE -> {
                debug(LOG_TAG + "onEvent() DELETE: " + path)
                BookLibApplication.sendBroadcast(localIntent)
            }
            FileObserver.DELETE_SELF -> {
                debug(LOG_TAG + "onEvent() DELETE_SELF: " + path)
                BookLibApplication.sendBroadcast(localIntent)
            }
            FileObserver.MODIFY -> {
                debug(LOG_TAG + "onEvent() MODIFY: " + path)
                BookLibApplication.sendBroadcast(localIntent)
            }
            FileObserver.MOVE_SELF -> {
                debug(LOG_TAG + "onEvent() MOVE_SELF: " + path)
                BookLibApplication.sendBroadcast(localIntent)
            }
            FileObserver.MOVED_FROM -> {
                debug(LOG_TAG + "onEvent() MOVED_FROM: " + path)
                BookLibApplication.sendBroadcast(localIntent)
            }
            FileObserver.MOVED_TO -> {
                debug(LOG_TAG + "onEvent() MOVED_TO: " + path)
                BookLibApplication.sendBroadcast(localIntent)
            }
            FileObserver.OPEN -> debug(LOG_TAG + "onEvent() OPEN: " + path)
            else -> {
            }
        }//debug(LOG_TAG + "onEvent() DEFAULT(" + event + "): " + path);
    }

    /**
     * Monitor single directory and dispatch all events to its parent, with full path.
     *
     * @author uestc.Mobius <mobius></mobius>@toraleap.com>
     * @version 2011.0121
     */
    internal inner class SingleFileObserver(var mPath: String, mask: Int) : FileObserver(mPath, mask) {

        constructor(path: String) : this(
            path,
            CHANGES_ONLY
        ) {
            mPath = path
        }

        override fun onEvent(event: Int, path: String?) {
            val newPath = "$mPath/$path"
            this@RecursiveFileObserver.onEvent(event, newPath)
        }
    }

    companion object {
        val LOG_TAG = RecursiveFileObserver::class.java.simpleName + ":"
        val INTENT_EBOOK_MODIFIED = "uk.co.droidinactu.booklib.library.INTENT_EBOOK_MODIFIED"
        val INTENT_EBOOK_MODIFIED_PATH = "uk.co.droidinactu.booklib.library.INTENT_EBOOK_MODIFIED_PATH"
        /**
         * Only modification events
         */
        var CHANGES_ONLY =
            FileObserver.CREATE or FileObserver.DELETE or FileObserver.CLOSE_WRITE or FileObserver.MOVE_SELF or FileObserver.MOVED_FROM or FileObserver.MOVED_TO
    }
}

