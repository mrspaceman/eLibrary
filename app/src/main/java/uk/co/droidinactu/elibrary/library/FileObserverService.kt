package uk.co.droidinactu.elibrary.library

import android.app.IntentService
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import uk.co.droidinactu.elibrary.library.LibraryManager.Companion.LOG_TAG
import java.util.*

class FileObserverService : IntentService {

    constructor() : super("Booklib FileTreeNode Watcher")

    constructor(name: String) : super(name)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(LOG_TAG, "onStartCommand()")
        onHandleIntent(intent)
        return Service.START_STICKY
    }

    override fun onDestroy() {
        Log.d(LOG_TAG, "onDestroy()")
        super.onDestroy()
    }

    override fun onBind(pIntent: Intent): IBinder? {
        // TODO: Return the communication channel to the service.
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onHandleIntent(pIntent: Intent?) {
        Log.d(LOG_TAG, "onHandleIntent()")
        if (pIntent != null) {
            // Gets data from the incoming Intent
            val fileObsAction = pIntent.getStringExtra("file_obs_action")
            val libname = pIntent.getStringExtra("libname")
            val rootdir = pIntent.getStringExtra("rootdir")

            if (fileObsAction != null) {
                when {
                    fileObsAction.equals("ADD", ignoreCase = true) -> addFileWatcher(
                        libname,
                        rootdir
                    )
                    //BookLibApplication.getInstance().addFileWatcher(libname, rootdir);
                    fileObsAction.equals("DEL", ignoreCase = true) -> delFileWatcher(
                        libname
                    )
                    //BookLibApplication.getInstance().delFileWatcher(libname);
                    else -> Log.d(LOG_TAG, "unknown action [$fileObsAction]")
                }
            }
        }
    }


    private val libraryWatcher = HashMap<String, RecursiveFileObserver>()

    fun addFileWatcher(libname: String?, rootdir: String?) {
        if (libname != null && rootdir != null) {
            if (!libraryWatcher.containsKey(libname)) {
                Log.d(LOG_TAG, "Adding file observer for [$libname] @ [$rootdir]")
                val fileOb = RecursiveFileObserver(rootdir, RecursiveFileObserver.CHANGES_ONLY)
                libraryWatcher[libname] = fileOb
                fileOb.startWatching()
            }
        } else {
            Log.d(LOG_TAG, "Already watching for [$libname] @ [$rootdir]")
        }
    }

    fun delFileWatcher(libname: String) {
        val fileOb = libraryWatcher[libname]
        fileOb?.stopWatching()
        libraryWatcher.remove(libname)
    }

}


