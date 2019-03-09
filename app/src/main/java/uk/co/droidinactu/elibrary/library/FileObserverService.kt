package uk.co.droidinactu.elibrary.library

import android.app.IntentService
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import uk.co.droidinactu.elibrary.BookLibApplication
import uk.co.droidinactu.elibrary.BookLibApplication.Companion.LOG_TAG

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
            val file_obs_action = pIntent.getStringExtra("file_obs_action")
            val libname = pIntent.getStringExtra("libname")
            val rootdir = pIntent.getStringExtra("rootdir")

            if (file_obs_action != null) {
                when {
                    file_obs_action.equals("ADD", ignoreCase = true) -> BookLibApplication.instance.addFileWatcher(
                        libname,
                        rootdir
                    )
                    //BookLibApplication.getInstance().addFileWatcher(libname, rootdir);
                    file_obs_action.equals("DEL", ignoreCase = true) -> BookLibApplication.instance.delFileWatcher(
                        libname
                    )
                    //BookLibApplication.getInstance().delFileWatcher(libname);
                    else -> Log.d(LOG_TAG, "unknown action [" + file_obs_action + "]")
                }
            }
        }
    }

}


