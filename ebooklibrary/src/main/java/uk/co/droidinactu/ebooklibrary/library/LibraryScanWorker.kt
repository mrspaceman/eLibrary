package uk.co.droidinactu.ebooklibrary.library

import android.content.Context
import android.os.Handler
import androidx.work.Worker
import androidx.work.WorkerParameters
import uk.co.droidinactu.ebooklibrary.MyDebug

class LibraryScanWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    private lateinit var prgBrHandler: Handler
    private lateinit var msgHndlr: Handler
    private lateinit var libTitle: String
    private lateinit var libRootDir: String
    private val libraryScanner = LibraryScanner()
    var taskComplete: Boolean = false

    override fun doWork(): Result {
        MyDebug.LOG.debug("LibraryScanWorker::doWork() started")
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
