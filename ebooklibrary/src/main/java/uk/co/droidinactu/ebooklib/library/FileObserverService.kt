package uk.co.droidinactu.ebooklib.library

import android.app.IntentService
import android.app.Service
import android.content.Intent
import android.os.IBinder
import uk.co.droidinactu.ebooklib.MyDebug
import java.util.*

class FileObserverService : IntentService {

    constructor() : super("Booklib FileTreeNode Watcher")

    constructor(name: String) : super(name)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MyDebug.LOG.debug("onStartCommand()")
        onHandleIntent(intent)
        return Service.START_STICKY
    }

    override fun onDestroy() {
        MyDebug.LOG.debug("onDestroy()")
        super.onDestroy()
    }

    override fun onBind(pIntent: Intent): IBinder? {
        // TODO: Return the communication channel to the service.
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onHandleIntent(pIntent: Intent?) {
        MyDebug.LOG.debug("onHandleIntent()")
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
                    else -> MyDebug.LOG.debug("unknown action [$fileObsAction]")
                }
            }
        }
    }


    private val libraryWatcher = HashMap<String, RecursiveFileObserver>()

    private fun addFileWatcher(libname: String?, rootdir: String?) {
        if (libname != null && rootdir != null) {
            if (!libraryWatcher.containsKey(libname)) {
                MyDebug.LOG.debug("Adding file observer for [$libname] @ [$rootdir]")
                val fileOb = RecursiveFileObserver(rootdir, RecursiveFileObserver.CHANGES_ONLY)
                libraryWatcher[libname] = fileOb
                fileOb.startWatching()
            }
        } else {
            MyDebug.LOG.debug("Already watching for [$libname] @ [$rootdir]")
        }
    }

    private fun delFileWatcher(libname: String) {
        val fileOb = libraryWatcher[libname]
        fileOb?.stopWatching()
        libraryWatcher.remove(libname)
    }

}


