package uk.co.droidinactu.elibrary.library

import android.content.Intent
import android.os.FileObserver
import android.util.Log
import uk.co.droidinactu.elibrary.library.LibraryManager.Companion.LOG_TAG
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
) : FileObserver(mPath, mMask) {

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
            Log.d(LOG_TAG, "startWatching() : " + sfo.mPath)
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
            FileObserver.ACCESS -> Log.d(LOG_TAG, "onEvent() ACCESS: $path")
            FileObserver.ATTRIB -> Log.d(LOG_TAG, "onEvent() ATTRIB: $path")
            FileObserver.CLOSE_NOWRITE -> Log.d(LOG_TAG, "onEvent() CLOSE_NOWRITE: $path")
            FileObserver.CLOSE_WRITE -> Log.d(LOG_TAG, "onEvent() CLOSE_WRITE: $path")
//            FileObserver.CREATE -> {
//                Log.d(LOG_TAG, "onEvent() CREATE: $path")
//                BookLibApplication.instance.sendBroadcast(localIntent)
//            }
//            FileObserver.DELETE -> {
//                Log.d(LOG_TAG, "onEvent() DELETE: $path")
//                BookLibApplication.instance.sendBroadcast(localIntent)
//            }
//            FileObserver.DELETE_SELF -> {
//                Log.d(LOG_TAG, "onEvent() DELETE_SELF: $path")
//                BookLibApplication.instance.sendBroadcast(localIntent)
//            }
//            FileObserver.MODIFY -> {
//                Log.d(LOG_TAG, "onEvent() MODIFY: $path")
//                BookLibApplication.instance.sendBroadcast(localIntent)
//            }
//            FileObserver.MOVE_SELF -> {
//                Log.d(LOG_TAG, "onEvent() MOVE_SELF: $path")
//                BookLibApplication.instance.sendBroadcast(localIntent)
//            }
//            FileObserver.MOVED_FROM -> {
//                Log.d(LOG_TAG, "onEvent() MOVED_FROM: $path")
//                BookLibApplication.instance.sendBroadcast(localIntent)
//            }
//            FileObserver.MOVED_TO -> {
//                Log.d(LOG_TAG, "onEvent() MOVED_TO: $path")
//                BookLibApplication.instance.sendBroadcast(localIntent)
//            }
            FileObserver.OPEN -> Log.d(LOG_TAG, "onEvent() OPEN: $path")
            else -> {
            }
        }//Log.d(LOG_TAG,"onEvent() DEFAULT(" + event + "): " + path);
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
        const val INTENT_EBOOK_MODIFIED = "uk.co.droidinactu.booklib.library.INTENT_EBOOK_MODIFIED"
        const val INTENT_EBOOK_MODIFIED_PATH = "uk.co.droidinactu.booklib.library.INTENT_EBOOK_MODIFIED_PATH"
        /**
         * Only modification events
         */
        var CHANGES_ONLY =
            FileObserver.CREATE or FileObserver.DELETE or FileObserver.CLOSE_WRITE or FileObserver.MOVE_SELF or FileObserver.MOVED_FROM or FileObserver.MOVED_TO
    }
}

